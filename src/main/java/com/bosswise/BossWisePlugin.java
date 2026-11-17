package com.bosswise;

import com.bosswise.config.BossWiseConfig;
import com.bosswise.db.BossDatabase;
import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.PlayerAccount;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.scanner.CurrentGearScannerService;
import com.bosswise.scanner.QuestScannerService;
import com.bosswise.service.BankCheckerService;
import com.bosswise.service.BossRecommendationEngine;
import com.bosswise.service.CollectionLogService;
import com.bosswise.service.CombatAchievementService;
import com.bosswise.service.CombatCalculationService;
import com.bosswise.service.CombatStyleEvaluator;
import com.bosswise.service.DpsLabService;
import com.bosswise.service.DropRateCalculatorService;
import com.bosswise.service.ExportService;
import com.bosswise.service.GePriceService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.GearRoadmapService;
import com.bosswise.service.GearUpgradeAdvisor;
import com.bosswise.service.InventoryBuilderService;
import com.bosswise.service.LootTrackerService;
import com.bosswise.service.MonsterProfitService;
import com.bosswise.service.ProgressionService;
import com.bosswise.service.SlayerTaskService;
import com.bosswise.service.WikiCacheService;
import com.bosswise.service.WikiSyncService;
import com.bosswise.ui.BossWisePluginPanel;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
        name = "BossWise",
        internalName = "bosswise",
        description = "The ultimate OSRS bossing assistant: real account scanning, gear optimizer, budget calculator, boss recommendations, and strategy guides",
        tags = {"boss", "bossing", "pvm", "pve", "gear", "budget", "optimizer", "vorkath", "zulrah", "raids", "guides"},
        configName = "bosswise",
        enabledByDefault = true
)
public class BossWisePlugin extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(BossWisePlugin.class);

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ItemManager itemManager;

    @Inject
    private BossWiseConfig config;

    @Inject
    private AccountScannerService accountScanner;

    @Inject
    private QuestScannerService questScanner;

    @Inject
    private GePriceService priceService;

    @Inject
    private BossDatabase bossDatabase;

    @Inject
    private MonsterDatabase monsterDatabase;

    @Inject
    private EquipmentDatabase equipmentDatabase;

    @Inject
    private BossRecommendationEngine recommendationEngine;

    @Inject
    private GearOptimizerService gearOptimizer;

    @Inject
    private GearUpgradeAdvisor upgradeAdvisor;

    @Inject
    private InventoryBuilderService inventoryService;

    @Inject
    private ProgressionService progressionService;

    @Inject
    private CombatCalculationService combatCalculationService;

    @Inject
    private CombatStyleEvaluator combatStyleEvaluator;

    @Inject
    private CurrentGearScannerService currentGearScanner;

    @Inject
    private DpsLabService dpsLabService;

    @Inject
    private SlayerTaskService slayerTaskService;

    @Inject
    private LootTrackerService lootTrackerService;

    @Inject
    private MonsterProfitService monsterProfitService;

    @Inject
    private DropRateCalculatorService dropRateCalculatorService;

    @Inject
    private GearRoadmapService gearRoadmapService;

    @Inject
    private CombatAchievementService combatAchievementService;

    @Inject
    private CollectionLogService collectionLogService;

    @Inject
    private BankCheckerService bankChecker;

    @Inject
    private ExportService exportService;

    @Inject
    private WikiSyncService wikiSyncService;

    @Inject
    private WikiCacheService wikiCacheService;

    private BossWisePluginPanel panel;
    private NavigationButton navButton;

    @Provides
    BossWiseConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BossWiseConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        log.info("[BossWise] Plugin starting up...");

        try {
            wikiCacheService.setCacheDir(getPluginDirectory().joinSegment("wiki_cache"));
        } catch (Exception ex) {
            log.warn("[BossWise] Could not initialize wiki cache directory: {}", ex.getMessage());
        }

        panel = new BossWisePluginPanel(
                accountScanner,
                bossDatabase,
                monsterDatabase,
                recommendationEngine,
                gearOptimizer,
                upgradeAdvisor,
                inventoryService,
                progressionService,
                combatCalculationService,
                combatStyleEvaluator,
                currentGearScanner,
                dpsLabService,
                slayerTaskService,
                lootTrackerService,
                monsterProfitService,
                dropRateCalculatorService,
                gearRoadmapService,
                combatAchievementService,
                collectionLogService,
                bankChecker,
                exportService,
                wikiSyncService,
                config
        );

        // Auto-refresh all UI panels whenever player account is scanned
        accountScanner.addAccountListener(account -> {
            SwingUtilities.invokeLater(() -> {
                if (panel != null) {
                    panel.refreshAll();
                }
            });
        });

        BufferedImage icon = loadIcon();

        navButton = NavigationButton.builder()
                .tooltip("BossWise - Bossing Assistant")
                .icon(icon)
                .priority(3)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
        log.info("[BossWise] Navigation button successfully registered to client toolbar!");

        if (client != null && client.getGameState() == GameState.LOGGED_IN) {
            clientThread.invokeLater(() -> {
                accountScanner.scanAccount();
                if (currentGearScanner != null) {
                    currentGearScanner.scanEquippedGear();
                }
            });
        }
    }

    private BufferedImage loadIcon() {
        String[] candidatePaths = new String[]{
                "/bosswise/icon.png",
                "/com/bosswise/icon.png",
                "/icon.png",
                "bosswise/icon.png",
                "icon.png"
        };

        for (String path : candidatePaths) {
            try {
                BufferedImage img = ImageUtil.loadImageResource(BossWisePlugin.class, path);
                if (img != null) {
                    log.info("[BossWise] Loaded icon resource from: {}", path);
                    return img;
                }
            } catch (Exception ignored) {
            }
        }

        log.warn("[BossWise] Resource icon not found; creating high-visibility golden scale with skull icon");
        return createFallbackIcon();
    }

    private BufferedImage createFallbackIcon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // Dark circular backing for high contrast on RuneLite sidebar
        g.setColor(new java.awt.Color(20, 20, 22, 220));
        g.fillOval(1, 1, 30, 30);

        // Golden balance crossbeam
        g.setColor(new java.awt.Color(255, 190, 20));
        g.setStroke(new java.awt.BasicStroke(2.0f));
        g.drawLine(4, 11, 27, 11);

        // Golden central pillar & base pedestal
        g.setColor(new java.awt.Color(218, 165, 32));
        g.fillRect(15, 12, 2, 14);
        g.fillRoundRect(10, 26, 12, 3, 2, 2);

        // Left and right scale chains & hanging pans
        g.setColor(new java.awt.Color(230, 180, 40));
        g.setStroke(new java.awt.BasicStroke(1.0f));
        // Left pan
        g.drawLine(5, 12, 3, 20);
        g.drawLine(5, 12, 9, 20);
        g.drawArc(2, 19, 8, 4, 180, 180);
        // Right pan
        g.drawLine(26, 12, 22, 20);
        g.drawLine(26, 12, 28, 20);
        g.drawArc(21, 19, 8, 4, 180, 180);

        // Central Skull at the fulcrum
        // Cranium
        g.setColor(new java.awt.Color(235, 230, 220));
        g.fillOval(12, 4, 8, 7);
        // Jaw / teeth
        g.fillRect(13, 10, 6, 3);

        // Skull dark eyes and nose
        g.setColor(new java.awt.Color(25, 20, 15));
        g.fillRect(14, 6, 2, 2);
        g.fillRect(17, 6, 2, 2);
        g.fillRect(15, 9, 2, 1);

        // Glowing golden eye highlights
        g.setColor(new java.awt.Color(255, 215, 0));
        g.fillRect(14, 6, 1, 1);
        g.fillRect(17, 6, 1, 1);

        g.dispose();
        return img;
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("BossWise shutting down...");
        if (navButton != null) {
            clientToolbar.removeNavigation(navButton);
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN) {
            clientThread.invokeLater(() -> {
                accountScanner.scanAccount();
                if (currentGearScanner != null) {
                    currentGearScanner.scanEquippedGear();
                }
            });
        }
    }

    @Subscribe
    public void onNpcLootReceived(NpcLootReceived event) {
        if (lootTrackerService != null && event != null && event.getNpc() != null && event.getItems() != null) {
            lootTrackerService.onNpcLoot(event.getNpc(), event.getItems());
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (event == null) {
            return;
        }
        if (event.getContainerId() == InventoryID.EQUIPMENT.getId()) {
            if (currentGearScanner != null) {
                currentGearScanner.scanEquippedGear();
            }
        } else if (event.getContainerId() == InventoryID.BANK.getId()) {
            if (bankChecker != null) {
                bankChecker.scanBank();
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (client != null && client.getGameState() == GameState.LOGGED_IN) {
            Player player = client.getLocalPlayer();
            if (player != null && player.getName() != null && !player.getName().trim().isEmpty()) {
                PlayerAccount current = accountScanner.getCurrentAccount();
                String cleanName = Text.sanitize(player.getName());
                // If not yet scanned, or name mismatch, or not logged in state: scan immediately
                if (current == null || !cleanName.equalsIgnoreCase(current.getUsername()) || !current.isLoggedIn()) {
                    accountScanner.scanAccount();
                }
            }
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event) {
        if (config.autoRefreshOnStatChange() && client != null && client.getGameState() == GameState.LOGGED_IN) {
            clientThread.invokeLater(() -> accountScanner.scanAccount());
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if ("bosswise".equals(event.getGroup())) {
            SwingUtilities.invokeLater(() -> {
                if (panel != null) {
                    panel.refreshAll();
                }
            });
        }
    }

    public BossWisePluginPanel getPanel() {
        return panel;
    }
}
