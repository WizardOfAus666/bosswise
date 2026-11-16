package com.bosswise;

import com.bosswise.config.BossWiseConfig;
import com.bosswise.db.BossDatabase;
import com.bosswise.db.EquipmentDatabase;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.PlayerAccount;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.scanner.QuestScannerService;
import com.bosswise.service.BossRecommendationEngine;
import com.bosswise.service.GePriceService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.GearUpgradeAdvisor;
import com.bosswise.service.InventoryBuilderService;
import com.bosswise.service.ProgressionService;
import com.bosswise.ui.BossWisePluginPanel;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import javax.imageio.ImageIO;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import org.junit.Assert;
import org.junit.Test;

public class PluginStartupTest {

    @Test
    public void testIconResource() throws Exception {
        InputStream is = BossWisePlugin.class.getResourceAsStream("/bosswise/icon.png");
        Assert.assertNotNull("Icon resource /bosswise/icon.png must exist", is);
        BufferedImage img = ImageIO.read(is);
        Assert.assertNotNull("Icon image must decode cleanly", img);
        int nonZero = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = img.getRGB(x, y);
                if ((argb >>> 24) > 30) nonZero++;
            }
        }
        System.out.println("Decoded icon size: " + img.getWidth() + "x" + img.getHeight() + ", visible pixels: " + nonZero);
        Assert.assertTrue("Icon must have visible pixels", nonZero > 50);
        BufferedImage viaImageUtil = ImageUtil.loadImageResource(BossWisePlugin.class, "/bosswise/icon.png");
        Assert.assertNotNull("ImageUtil.loadImageResource must decode cleanly", viaImageUtil);
    }
}
