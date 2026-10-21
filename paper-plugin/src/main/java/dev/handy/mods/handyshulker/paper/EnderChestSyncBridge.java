package dev.handy.mods.handyshulker.paper;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/** Optional reflective link to the 26.3-only Minecraft codec adapter. */
final class EnderChestSyncBridge {
    private static final String ADAPTER = "dev.handy.mods.handyshulker.paper.v263.Paper263PayloadSender";
    private final JavaPlugin plugin;
    private final Object sender;
    private final Method sendMethod;

    EnderChestSyncBridge(JavaPlugin plugin, ShulkerInventoryListener inventoryListener) {
        this.plugin = plugin;
        Object loadedSender = null;
        Method loadedMethod = null;
        try {
            Class<?> type = Class.forName(ADAPTER, true, plugin.getClass().getClassLoader());
            Constructor<?> constructor = type.getConstructor(JavaPlugin.class, ShulkerInventoryListener.class);
            loadedSender = constructor.newInstance(plugin, inventoryListener);
            loadedMethod = type.getMethod("send", Player.class);
            plugin.getLogger().info("Ender chest client preview sync is available.");
        } catch (ClassNotFoundException ignored) {
            plugin.getLogger().info("Using the legacy build: ender chest previews cannot be synchronized to the client.");
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("Could not initialize ender chest payload sender: " + exception.getMessage());
        }
        sender = loadedSender;
        sendMethod = loadedMethod;
    }

    void sync(Player player) {
        if (sender == null || sendMethod == null) return;
        try {
            sendMethod.invoke(sender, player);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("Could not sync ender chest preview for " + player.getName() + ": "
                    + exception.getMessage());
        }
    }
}
