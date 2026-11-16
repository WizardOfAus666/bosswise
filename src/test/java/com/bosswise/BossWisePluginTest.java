package com.bosswise;

import java.io.File;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BossWisePluginTest {
    public static void main(String[] args) throws Exception {
        // Delete any duplicate sideloaded BossWise jar to avoid dual-instance conflict
        try {
            File sideloadDir = new File(System.getProperty("user.home"), ".runelite/sideloaded-plugins");
            if (sideloadDir.exists() && sideloadDir.isDirectory()) {
                File[] files = sideloadDir.listFiles((dir, name) -> name.toLowerCase().contains("bosswise"));
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        ExternalPluginManager.loadBuiltin(BossWisePlugin.class);
        RuneLite.main(args);
    }
}
