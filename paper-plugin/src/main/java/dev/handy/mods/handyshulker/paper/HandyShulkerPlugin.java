package dev.handy.mods.handyshulker.paper;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

/** Paper-side implementation for HandyShulker's vanilla inventory packet flow. */
public final class HandyShulkerPlugin extends JavaPlugin {
    private final ShulkerInventoryListener inventoryListener = new ShulkerInventoryListener(this);
    private EnderChestSyncBridge enderChestSync;

    @Override
    public void onEnable() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "handyshulker:sync_ender_chest");
        enderChestSync = new EnderChestSyncBridge(this, inventoryListener);
        Bukkit.getPluginManager().registerEvents(inventoryListener, this);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                Bukkit.getScheduler().runTask(HandyShulkerPlugin.this,
                        () -> syncEnderChest(event.getPlayer()));
            }

            @EventHandler
            public void onInventoryClose(InventoryCloseEvent event) {
                if (event.getPlayer() instanceof org.bukkit.entity.Player player) {
                    Bukkit.getScheduler().runTask(HandyShulkerPlugin.this,
                            () -> syncEnderChest(player));
                }
            }
        }, this);
        registerBundleSelectionEvent();
        getLogger().info("Enabled. Existing HandyShulker clients need no changes.");
    }

    void syncEnderChest(org.bukkit.entity.Player player) {
        if (!ShulkerInventoryListener.isFabricClient(player)) return;
        enderChestSync.sync(player);
    }

    /**
     * Paper added this event after the Bukkit baseline. Reflection keeps the
     * plugin loadable on older Paper versions that do not define the class.
     */
    @SuppressWarnings("unchecked")
    private void registerBundleSelectionEvent() {
        try {
            Class<?> raw = Class.forName("io.papermc.paper.event.inventory.PlayerBundleItemSelectEvent");
            if (!Event.class.isAssignableFrom(raw)) return;
            Class<? extends Event> eventClass = (Class<? extends Event>) raw;
            Listener listener = new Listener() {};
            EventExecutor executor = (ignored, event) -> inventoryListener.onBundleSelection(event);
            Bukkit.getPluginManager().registerEvent(eventClass, listener, EventPriority.MONITOR, executor, this, true);
            getLogger().info("Registered Paper bundle selection bridge.");
        } catch (ClassNotFoundException ignored) {
            getLogger().info("This Paper version has no bundle selection event; scroll selection is unavailable.");
        }
    }
}
