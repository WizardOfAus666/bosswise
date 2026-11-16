package com.bosswise;

import com.bosswise.config.BossWiseConfig;
import com.bosswise.model.CombatStyle;
import com.bosswise.service.GearOptimizerService.PriorityMode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.NavigationButton;
import org.junit.Assert;
import org.junit.Test;

public class PluginGuiceStartupTest {

    @Test
    public void testGuiceInstantiationAndStartup() throws Exception {
        Client client = (Client) Proxy.newProxyInstance(
                Client.class.getClassLoader(),
                new Class<?>[]{Client.class},
                (proxy, method, args) -> null
        );

        ClientThread clientThread = new ClientThread();

        Constructor<ClientToolbar> tbCtor = ClientToolbar.class.getDeclaredConstructor(ClientUI.class);
        tbCtor.setAccessible(true);
        ClientToolbar clientToolbar = tbCtor.newInstance((ClientUI) null);

        Constructor<ItemManager> imCtor = ItemManager.class.getDeclaredConstructor(
                Client.class,
                java.util.concurrent.ScheduledExecutorService.class,
                net.runelite.client.callback.ClientThread.class,
                net.runelite.client.eventbus.EventBus.class,
                net.runelite.client.game.ItemClient.class,
                net.runelite.client.config.RuneLiteConfig.class
        );
        imCtor.setAccessible(true);
        java.util.concurrent.ScheduledExecutorService ses = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        net.runelite.client.eventbus.EventBus eventBus = new net.runelite.client.eventbus.EventBus();
        ItemManager itemManager = imCtor.newInstance(null, ses, null, eventBus, null, null);

        BossWiseConfig config = (BossWiseConfig) Proxy.newProxyInstance(
                BossWiseConfig.class.getClassLoader(),
                new Class<?>[]{BossWiseConfig.class},
                (proxy, method, args) -> {
                    if ("defaultBudget".equals(method.getName())) return 5_000_000;
                    if ("preferredCombatStyle".equals(method.getName())) return CombatStyle.MELEE;
                    if ("equipmentPriority".equals(method.getName())) return PriorityMode.BALANCED;
                    if ("showLockedBosses".equals(method.getName())) return true;
                    if ("showGroupBosses".equals(method.getName())) return true;
                    if ("compactLayout".equals(method.getName())) return false;
                    if ("autoRefreshOnStatChange".equals(method.getName())) return true;
                    return null;
                }
        );

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Client.class).toInstance(client);
                bind(ClientThread.class).toInstance(clientThread);
                bind(ClientToolbar.class).toInstance(clientToolbar);
                bind(ItemManager.class).toInstance(itemManager);
                bind(BossWiseConfig.class).toInstance(config);
            }
        });

        BossWisePlugin plugin = injector.getInstance(BossWisePlugin.class);
        Assert.assertNotNull("BossWisePlugin must be instantiated by Guice", plugin);

        plugin.startUp();
        System.out.println("[TEST SUCCESS] BossWisePlugin.startUp() ran cleanly!");

        Field navField = BossWisePlugin.class.getDeclaredField("navButton");
        navField.setAccessible(true);
        NavigationButton navButton = (NavigationButton) navField.get(plugin);
        Assert.assertNotNull("navButton must be initialized", navButton);
        Assert.assertEquals("BossWise - Bossing Assistant", navButton.getTooltip());
        System.out.println("[TEST SUCCESS] navButton tooltip: " + navButton.getTooltip());
    }
}
