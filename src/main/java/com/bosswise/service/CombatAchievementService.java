package com.bosswise.service;

import com.bosswise.model.CombatAchievementTask;
import com.bosswise.model.CombatAchievementTask.CaTier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
public class CombatAchievementService {
    private final List<CombatAchievementTask> tasks = new ArrayList<>();
    private final Set<String> completedTaskKeys = new HashSet<>();

    @com.google.inject.Inject(optional = true)
    private ConfigManager configManager;

    @Inject
    public CombatAchievementService() {
        initTasks();
        loadSavedTasks();
    }

    public CombatAchievementService(ConfigManager configManager) {
        this.configManager = configManager;
        initTasks();
        loadSavedTasks();
    }

    private void initTasks() {
        // Vorkath
        add(CaTier.EASY, "Vorkath", "Zombie Destroyer", "Kill a zombified spawn with a single Crumble Undead cast.", "Cast Crumble Undead immediately as the spawn emerges from ice.");
        add(CaTier.MEDIUM, "Vorkath", "Dodging the Dragon", "Kill Vorkath without taking damage from any fireball, spawn, or acid pool.", "Turn on run and maintain continuous linear walking during acid phase.");
        add(CaTier.HARD, "Vorkath", "The Walk", "Kill Vorkath while performing the woox walk during the acid phase.", "Step back and forth on a clean line of tiles while firing crossbow on each return tick.");
        add(CaTier.ELITE, "Vorkath", "Faithless Encounter", "Kill Vorkath without using any protection prayers.", "Rely strictly on high defence armor (Justiciar/Bandos) and super antifire potion.");

        // Zulrah
        add(CaTier.MEDIUM, "Zulrah", "Snake Rebound", "Kill Zulrah without taking damage from Snakeling melee attacks.", "Pray Protect from Melee when snakelings spawn or recoil them instantly.");
        add(CaTier.HARD, "Zulrah", "Snake. Snake!? Snaaaake!", "Kill Zulrah in under 1 minute 15 seconds.", "Utilize high tier switches (Shadow, Bowfa, or Tbow) with Divine potions.");
        add(CaTier.MASTER, "Zulrah", "Perfect Zulrah", "Kill Zulrah without taking any avoidable cloud or misprayer damage.", "Flawless rotation memorization and pre-positioning.");

        // General Graardor
        add(CaTier.EASY, "General Graardor", "General Graardor Novice", "Kill General Graardor.", "Standard team or solo kill.");
        add(CaTier.MEDIUM, "General Graardor", "Ourg Killer", "Kill General Graardor 10 times in a single trip.", "Standard door-altar or altar-door ranged method with Bowfa.");
        add(CaTier.HARD, "General Graardor", "Feet of Fury", "Kill General Graardor without taking damage from his melee attack.", "Kite around the perimeter using red-click or freeze methods.");

        // Scurrius
        add(CaTier.EASY, "Scurrius", "Rat-ical Dodge", "Kill Scurrius without taking damage from falling debris.", "Step away from shadows as ceiling debris falls.");
        add(CaTier.MEDIUM, "Scurrius", "Scurrius Speed-Chaser", "Kill Scurrius in under 45 seconds.", "Equip bone mace / bone shortbow for max hit multiplier.");

        // Phantom Muspah
        add(CaTier.MEDIUM, "Phantom Muspah", "Phantom Muspah Novice", "Kill Phantom Muspah.", "Defeat the boss in either normal or quest variant.");
        add(CaTier.HARD, "Phantom Muspah", "Space is Limited", "Kill Phantom Muspah without letting spikes cover >25% of the room.", "Lure Muspah to smash spikes or DPS boss before spikes saturate the arena.");
        add(CaTier.MASTER, "Phantom Muspah", "Can't Touch This", "Kill Phantom Muspah without taking damage from his melee form.", "Step back kite technique or cast Ice Barrage to freeze melee phase.");

        // Alchemical Hydra
        add(CaTier.HARD, "Alchemical Hydra", "Alchemical Veteran", "Kill Alchemical Hydra 10 times in a single trip.", "Maintain prayer switching every 3 attacks.");
        add(CaTier.ELITE, "Alchemical Hydra", "A Chilling Encounter", "Kill Hydra without taking damage from lightning attacks.", "Lure Hydra toward the wall during lightning phase.");
    }

    private void add(CaTier tier, String boss, String name, String desc, String tip) {
        tasks.add(new CombatAchievementTask(tier, boss, name, desc, tip, false));
    }

    public List<CombatAchievementTask> getAllTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public List<CombatAchievementTask> getTasksForBoss(String bossName) {
        List<CombatAchievementTask> res = new ArrayList<>();
        for (CombatAchievementTask t : tasks) {
            if (t.getBossName().equalsIgnoreCase(bossName)) {
                res.add(t);
            }
        }
        return res;
    }

    public void setTaskCompleted(String taskName, boolean completed) {
        for (CombatAchievementTask t : tasks) {
            if (t.getTaskName().equalsIgnoreCase(taskName)) {
                t.setCompleted(completed);
                if (completed) {
                    completedTaskKeys.add(taskName);
                } else {
                    completedTaskKeys.remove(taskName);
                }
                saveProgress();
                break;
            }
        }
    }

    public double getTierCompletionPercent(CaTier tier) {
        int total = 0;
        int comp = 0;
        for (CombatAchievementTask t : tasks) {
            if (t.getTier() == tier) {
                total++;
                if (t.isCompleted()) comp++;
            }
        }
        if (total == 0) return 0.0;
        return ((double) comp / (double) total) * 100.0;
    }

    public double getTotalCompletionPercent() {
        if (tasks.isEmpty()) return 0.0;
        int comp = 0;
        for (CombatAchievementTask t : tasks) {
            if (t.isCompleted()) comp++;
        }
        return ((double) comp / (double) tasks.size()) * 100.0;
    }

    private void saveProgress() {
        if (configManager != null) {
            configManager.setConfiguration("bosswise", "ca_tasks", String.join(",", completedTaskKeys));
        }
    }

    private void loadSavedTasks() {
        if (configManager != null) {
            String saved = configManager.getConfiguration("bosswise", "ca_tasks");
            if (saved != null && !saved.trim().isEmpty()) {
                String[] parts = saved.split(",");
                Collections.addAll(completedTaskKeys, parts);
                for (CombatAchievementTask t : tasks) {
                    if (completedTaskKeys.contains(t.getTaskName())) {
                        t.setCompleted(true);
                    }
                }
            }
        }
    }
}
