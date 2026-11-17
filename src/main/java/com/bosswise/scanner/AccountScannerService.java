package com.bosswise.scanner;

import com.bosswise.model.PlayerAccount;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AccountScannerService {
    private static final Logger log = LoggerFactory.getLogger(AccountScannerService.class);

    private final Client client;
    private final QuestScannerService questScannerService;
    private final List<Consumer<PlayerAccount>> listeners = new CopyOnWriteArrayList<>();

    private volatile PlayerAccount currentAccount = PlayerAccount.createDefault();

    /**
     * True while the account in use came from somewhere other than this client
     * session (a WikiSync lookup, or a test fixture). Client scans leave it alone
     * so a looked-up profile is not overwritten on the next tick.
     */
    private volatile boolean externalAccount = false;

    @Inject
    public AccountScannerService(Client client, QuestScannerService questScannerService) {
        this.client = client;
        this.questScannerService = questScannerService;
    }

    public void addAccountListener(Consumer<PlayerAccount> listener) {
        if (listener != null) {
            listeners.add(listener);
            // Immediately dispatch current state
            listener.accept(currentAccount);
        }
    }

    public void removeAccountListener(Consumer<PlayerAccount> listener) {
        listeners.remove(listener);
    }

    public PlayerAccount getCurrentAccount() {
        return currentAccount;
    }

    public synchronized void scanAccount() {
        if (externalAccount) {
            log.debug("Using a looked-up account; skipping client scan");
            return;
        }
        if (client == null || client.getGameState() != GameState.LOGGED_IN) {
            log.debug("Client not logged in; preserving existing account state");
            return;
        }

        Player player = client.getLocalPlayer();
        if (player == null || player.getName() == null || player.getName().trim().isEmpty()) {
            log.debug("Local player not yet ready; skipping scan until player loads");
            return;
        }

        String rawName = player.getName();
        String name = net.runelite.client.util.Text.sanitize(rawName);

        Map<Skill, Integer> realLevels = new EnumMap<>(Skill.class);
        Map<Skill, Integer> boostedLevels = new EnumMap<>(Skill.class);

        for (Skill skill : Skill.values()) {
            if (skill == Skill.OVERALL) continue;
            try {
                int real = client.getRealSkillLevel(skill);
                int boosted = client.getBoostedSkillLevel(skill);
                realLevels.put(skill, Math.max(1, real));
                boostedLevels.put(skill, Math.max(0, boosted));
            } catch (Exception e) {
                realLevels.put(skill, 1);
                boostedLevels.put(skill, 1);
            }
        }

        int attack = realLevels.getOrDefault(Skill.ATTACK, 1);
        int strength = realLevels.getOrDefault(Skill.STRENGTH, 1);
        int defence = realLevels.getOrDefault(Skill.DEFENCE, 1);
        int hp = realLevels.getOrDefault(Skill.HITPOINTS, 10);
        int prayer = realLevels.getOrDefault(Skill.PRAYER, 1);
        int ranged = realLevels.getOrDefault(Skill.RANGED, 1);
        int magic = realLevels.getOrDefault(Skill.MAGIC, 1);

        int combatLevel = (player != null && player.getCombatLevel() > 0)
                ? player.getCombatLevel()
                : Experience.getCombatLevel(attack, strength, defence, hp, prayer, ranged, magic);

        int totalLevel = client.getTotalLevel();

        // Scan quest states
        Map<Quest, QuestState> questStates = (questScannerService != null)
                ? questScannerService.scanQuests(client)
                : new HashMap<>();

        Set<String> manualCompleted = (questScannerService != null)
                ? questScannerService.getKnownCompletedQuestNames(questStates)
                : new HashSet<>();

        this.currentAccount = new PlayerAccount(
                name,
                combatLevel,
                totalLevel,
                realLevels,
                boostedLevels,
                questStates,
                manualCompleted,
                true
        );

        notifyListeners();
    }

    public void setCustomAccount(PlayerAccount account) {
        if (account != null) {
            this.currentAccount = account;
            this.externalAccount = true;
            notifyListeners();
        }
    }

    /** True when recommendations are based on a looked-up account rather than this client. */
    public boolean isUsingExternalAccount() {
        return externalAccount;
    }

    /** Drops a looked-up account and goes back to this client session. */
    public synchronized void clearCustomAccount() {
        this.externalAccount = false;
        if (client != null && client.getGameState() == GameState.LOGGED_IN) {
            scanAccount();
        } else {
            this.currentAccount = PlayerAccount.createDefault();
            notifyListeners();
        }
    }

    private void notifyListeners() {
        for (Consumer<PlayerAccount> listener : listeners) {
            try {
                listener.accept(currentAccount);
            } catch (Exception ex) {
                log.error("Error notifying account listener", ex);
            }
        }
    }
}
