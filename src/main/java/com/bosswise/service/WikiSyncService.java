package com.bosswise.service;

import com.bosswise.model.PlayerAccount;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Experience;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Looks a player up by name through WikiSync, so BossWise can tailor its
 * recommendations without being logged in on that account.
 *
 * <p>WikiSync is the RuneLite plugin most people already run for the wiki's own
 * tools; it uploads levels, quest states and diaries to
 * {@code sync.runescape.wiki}. This reads that public profile:
 * {@code GET https://sync.runescape.wiki/runelite/player/<name>/STANDARD}.
 *
 * <p>Only two things are used: {@code levels} (24 skills, keyed by display name)
 * and {@code quests} (quest name to 0 = not started, 1 = in progress,
 * 2 = finished). Diaries, music, collection log and combat achievements are
 * ignored for now.
 *
 * <p>If the player has never used WikiSync the endpoint answers 404, which is
 * reported as "no WikiSync data" rather than an error — that is the normal case
 * for anyone who has not installed it.
 */
@Singleton
public class WikiSyncService {
    private static final Logger log = LoggerFactory.getLogger(WikiSyncService.class);

    private static final String ENDPOINT = "https://sync.runescape.wiki/runelite/player/";
    private static final String PROFILE = "/STANDARD";
    private static final String USER_AGENT =
            "BossWise-RuneLite-Plugin/1.0.0 (gear advisor; contact via plugin repository)";

    /** WikiSync quest encoding. */
    private static final int QUEST_IN_PROGRESS = 1;
    private static final int QUEST_FINISHED = 2;

    private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

    private final Map<String, CacheEntry> cache = new HashMap<>();

    /** RuneLite's shared client when injected; null only in offline unit tests. */
    private final OkHttpClient httpClient;

    @Inject
    public WikiSyncService(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /** Standalone / test constructor (parseProfile and input validation only). */
    public WikiSyncService() {
        this.httpClient = null;
    }

    /** Outcome of a lookup. {@link #account} is only set when {@link #success} is true. */
    public static class Result {
        private final boolean success;
        private final String message;
        private final PlayerAccount account;

        public Result(boolean success, String message, PlayerAccount account) {
            this.success = success;
            this.message = message;
            this.account = account;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public PlayerAccount getAccount() { return account; }
    }

    private static class CacheEntry {
        private final PlayerAccount account;
        private final long fetchedAt;

        private CacheEntry(PlayerAccount account, long fetchedAt) {
            this.account = account;
            this.fetchedAt = fetchedAt;
        }
    }

    /**
     * Fetches a profile off the client thread. The callback runs on OkHttp's thread,
     * so UI code must hop back to the Swing thread itself.
     */
    public void lookupAsync(String rawUsername, Consumer<Result> callback) {
        if (callback == null) return;

        final String username = normalise(rawUsername);
        if (username.isEmpty()) {
            callback.accept(new Result(false, "Enter a username first.", null));
            return;
        }
        if (username.length() > 12) {
            callback.accept(new Result(false, "That is longer than an OSRS name can be.", null));
            return;
        }

        CacheEntry cached = cache.get(username.toLowerCase());
        if (cached != null && System.currentTimeMillis() - cached.fetchedAt < CACHE_TTL_MS) {
            callback.accept(new Result(true, describe(cached.account) + " (cached)", cached.account));
            return;
        }

        if (httpClient == null) {
            callback.accept(new Result(false, "HTTP client unavailable.", null));
            return;
        }

        Request request = new Request.Builder()
                .url(ENDPOINT + URLEncoder.encode(username, StandardCharsets.UTF_8) + PROFILE)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .build();

        try {
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.debug("[BossWise] WikiSync lookup failed for {}: {}", username, e.getMessage());
                    callback.accept(new Result(false, "Could not reach sync.runescape.wiki. Check your connection.", null));
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        int code = response.code();
                        if (code == 404) {
                            callback.accept(new Result(false,
                                    "No WikiSync data for \"" + username + "\". That player needs the WikiSync "
                                            + "plugin enabled and has to log in once with it.", null));
                            return;
                        }
                        if (code < 200 || code >= 300 || response.body() == null) {
                            callback.accept(new Result(false, "WikiSync answered with HTTP " + code + ".", null));
                            return;
                        }

                        PlayerAccount account = parseProfile(response.body().string());
                        if (account == null) {
                            callback.accept(new Result(false, "WikiSync returned a profile BossWise could not read.", null));
                            return;
                        }
                        cache.put(username.toLowerCase(), new CacheEntry(account, System.currentTimeMillis()));
                        callback.accept(new Result(true, describe(account), account));
                    } catch (Exception ex) {
                        log.debug("[BossWise] WikiSync parse failed for {}: {}", username, ex.getMessage());
                        callback.accept(new Result(false, "Could not read that WikiSync profile.", null));
                    } finally {
                        try {
                            response.close();
                        } catch (Exception ignored) {
                            // nothing useful to do if closing fails
                        }
                    }
                }
            });
        } catch (Exception e) {
            log.debug("[BossWise] WikiSync request could not start: {}", e.getMessage());
            callback.accept(new Result(false, "Could not start the lookup.", null));
        }
    }

    /**
     * Turns a WikiSync profile into a {@link PlayerAccount}. Pure function, no
     * network, so it can be tested directly. Returns null if the JSON has no levels.
     */
    public PlayerAccount parseProfile(String json) {
        if (json == null || json.trim().isEmpty()) return null;

        JsonElement root;
        try {
            root = new JsonParser().parse(json);
        } catch (Exception e) {
            log.debug("[BossWise] WikiSync profile was not valid JSON");
            return null;
        }
        if (root == null || !root.isJsonObject()) return null;
        JsonObject obj = root.getAsJsonObject();

        JsonObject levels = obj.has("levels") && obj.get("levels").isJsonObject()
                ? obj.getAsJsonObject("levels") : null;
        if (levels == null) return null;

        Map<Skill, Integer> realLevels = new EnumMap<>(Skill.class);
        for (Skill skill : Skill.values()) {
            realLevels.put(skill, 1);
        }
        realLevels.put(Skill.HITPOINTS, 10);

        int totalLevel = 0;
        for (Map.Entry<String, JsonElement> entry : levels.entrySet()) {
            Skill skill = toSkill(entry.getKey());
            int level;
            try {
                level = entry.getValue().getAsInt();
            } catch (Exception e) {
                continue;
            }
            if (level > 0) totalLevel += level;
            // Skills this RuneLite build does not know (new releases) still count
            // toward the total, they just cannot be stored against a Skill.
            if (skill != null) {
                realLevels.put(skill, Math.max(1, level));
            }
        }

        Map<Quest, QuestState> questStates = new HashMap<>();
        Set<String> completedQuests = new HashSet<>();
        if (obj.has("quests") && obj.get("quests").isJsonObject()) {
            Map<String, Quest> byName = questsByName();
            for (Map.Entry<String, JsonElement> entry : obj.getAsJsonObject("quests").entrySet()) {
                String name = entry.getKey();
                int state;
                try {
                    state = entry.getValue().getAsInt();
                } catch (Exception e) {
                    continue;
                }
                if (state == QUEST_FINISHED) {
                    completedQuests.add(name);
                }
                Quest quest = byName.get(questKey(name));
                if (quest != null) {
                    questStates.put(quest, state == QUEST_FINISHED
                            ? QuestState.FINISHED
                            : (state == QUEST_IN_PROGRESS ? QuestState.IN_PROGRESS : QuestState.NOT_STARTED));
                }
            }
        }

        String username = obj.has("username") ? obj.get("username").getAsString() : "Player";

        int combatLevel = Experience.getCombatLevel(
                realLevels.getOrDefault(Skill.ATTACK, 1),
                realLevels.getOrDefault(Skill.STRENGTH, 1),
                realLevels.getOrDefault(Skill.DEFENCE, 1),
                realLevels.getOrDefault(Skill.HITPOINTS, 10),
                realLevels.getOrDefault(Skill.PRAYER, 1),
                realLevels.getOrDefault(Skill.RANGED, 1),
                realLevels.getOrDefault(Skill.MAGIC, 1));

        // No boosts are known for a looked-up account, so boosted == real.
        Map<Skill, Integer> boostedLevels = new EnumMap<>(realLevels);

        return new PlayerAccount(
                username,
                combatLevel,
                totalLevel,
                realLevels,
                boostedLevels,
                questStates,
                completedQuests,
                true);
    }

    /** One line for the UI: "Rogue One - cb 126, 99/99/99 melee, 215 quests done". */
    public static String describe(PlayerAccount account) {
        if (account == null) return "";
        return account.getUsername()
                + " - combat " + account.getCombatLevel()
                + ", " + account.getRealLevel(Skill.ATTACK) + "/" + account.getRealLevel(Skill.STRENGTH)
                + "/" + account.getRealLevel(Skill.DEFENCE) + " melee, "
                + account.getRealLevel(Skill.RANGED) + " ranged, " + account.getRealLevel(Skill.MAGIC) + " magic, "
                + account.getRealLevel(Skill.SLAYER) + " slayer, "
                + account.getCompletedQuestNames().size() + " quests done";
    }

    private static String normalise(String username) {
        if (username == null) return "";
        return username.trim().replace(' ', ' ').replace('_', ' ').replaceAll("\\s+", " ");
    }

    private static Skill toSkill(String displayName) {
        if (displayName == null) return null;
        try {
            return Skill.valueOf(displayName.trim().toUpperCase().replace(' ', '_'));
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, Quest> questsByName() {
        Map<String, Quest> byName = new HashMap<>();
        for (Quest quest : Quest.values()) {
            try {
                byName.put(questKey(quest.getName()), quest);
            } catch (Exception ignored) {
                // a Quest without a usable name is simply not matchable
            }
        }
        return byName;
    }

    /** Loose key so "Dragon Slayer II" and "dragon slayer ii" match. */
    private static String questKey(String name) {
        if (name == null) return "";
        return name.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
