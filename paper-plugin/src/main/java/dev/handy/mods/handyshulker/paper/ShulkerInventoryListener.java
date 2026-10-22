package dev.handy.mods.handyshulker.paper;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

/** Implements the same two-sided click insert/extract behavior as the mod mixin. */
public final class ShulkerInventoryListener implements Listener {
    private final HandyShulkerPlugin plugin;
    private final Map<SelectionKey, Integer> selections = new ConcurrentHashMap<>();
    private final Set<UUID> verifiedClients = ConcurrentHashMap.newKeySet();
    private volatile boolean exactClientDetection;

    ShulkerInventoryListener(HandyShulkerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!isSupportedClient(player)) return;
        if (event.getClickedInventory() == null) return;
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();
        ClickType click = event.getClick();

        if (isContainer(cursor) && cursor.getAmount() == 1) {
            if (click.isLeftClick() && clicked != null && !clicked.getType().isAir()) {
                if (cannotInsertInto(cursor, clicked)) return;
                ItemStack box = cursor.clone();
                ItemStack incoming = clicked.clone();
                int inserted = insert(box, incoming, player);
                event.setCancelled(true);
                event.setCursor(box);
                event.setCurrentItem(incoming.getAmount() == 0 ? null : incoming);
                if (box.getType() == Material.ENDER_CHEST && inserted > 0) plugin.syncEnderChest(player);
                play(player, inserted > 0 ? Sound.BLOCK_SHULKER_BOX_OPEN : Sound.ENTITY_SHULKER_HURT_CLOSED,
                        inserted > 0 ? 0.4f : 0.3f, inserted > 0 ? 1.0f : 0.9f);
            } else if (click.isRightClick() && (clicked == null || clicked.getType().isAir())) {
                ItemStack box = cursor.clone();
                int index = selectedIndex(player, event.getRawSlot());
                ItemStack extracted = remove(box, player, index);
                if (extracted != null) {
                    event.setCancelled(true);
                    event.setCursor(box);
                    event.setCurrentItem(extracted);
                    if (box.getType() == Material.ENDER_CHEST) plugin.syncEnderChest(player);
                    selections.remove(new SelectionKey(player.getUniqueId(), event.getRawSlot()));
                    play(player, Sound.BLOCK_SHULKER_BOX_CLOSE, 0.4f, 1.0f);
                }
            }
            return;
        }

        if (isContainer(clicked) && clicked.getAmount() == 1) {
            if (click.isLeftClick() && !cursor.getType().isAir()) {
                if (cannotInsertInto(clicked, cursor)) return;
                ItemStack box = clicked.clone();
                ItemStack incoming = cursor.clone();
                int inserted = insert(box, incoming, player);
                event.setCancelled(true);
                event.setCurrentItem(box);
                event.setCursor(incoming.getAmount() == 0 ? null : incoming);
                if (box.getType() == Material.ENDER_CHEST && inserted > 0) plugin.syncEnderChest(player);
                play(player, inserted > 0 ? Sound.BLOCK_SHULKER_BOX_OPEN : Sound.ENTITY_SHULKER_HURT_CLOSED,
                        inserted > 0 ? 0.4f : 0.3f, inserted > 0 ? 1.0f : 0.9f);
            } else if (click.isRightClick() && cursor.getType().isAir()) {
                ItemStack box = clicked.clone();
                int index = selectedIndex(player, event.getRawSlot());
                ItemStack extracted = remove(box, player, index);
                if (extracted != null) {
                    event.setCancelled(true);
                    event.setCurrentItem(box);
                    event.setCursor(extracted);
                    if (box.getType() == Material.ENDER_CHEST) plugin.syncEnderChest(player);
                    selections.remove(new SelectionKey(player.getUniqueId(), event.getRawSlot()));
                    play(player, Sound.BLOCK_SHULKER_BOX_CLOSE, 0.4f, 1.0f);
                }
            }
        }
    }

    /** Called reflectively for Paper's bundle selection event. */
    void onBundleSelection(Event event) {
        try {
            Player player = (Player) invoke(event, "getPlayer");
            int rawSlot = (Integer) invoke(event, "getRawSlot");
            int selected = (Integer) invoke(event, "getSelectedIndex");
            ItemStack stack = (ItemStack) invoke(event, "getBundle");
            if (isContainer(stack)) selections.put(new SelectionKey(player.getUniqueId(), rawSlot), selected);
        } catch (ReflectiveOperationException | ClassCastException exception) {
            plugin.getLogger().warning("Could not read Paper bundle selection event: " + exception.getMessage());
        }
    }

    /** Records the vanilla selection packet used by the unchanged mod client. */
    public void rememberSelection(Player player, int rawSlot, int selectedIndex) {
        rememberSelection(player.getUniqueId(), rawSlot, selectedIndex);
    }

    /** This overload deliberately does no inventory access; the 26.3 packet hook calls it on Netty's thread. */
    public void rememberSelection(UUID playerId, int rawSlot, int selectedIndex) {
        if (rawSlot < 0 || rawSlot >= 128) return;
        SelectionKey key = new SelectionKey(playerId, rawSlot);
        if (selectedIndex < 0) selections.remove(key);
        else selections.put(key, selectedIndex);
    }

    public void clearSelections(UUID playerId) {
        selections.keySet().removeIf(key -> key.player().equals(playerId));
        verifiedClients.remove(playerId);
    }

    void setExactClientDetection(boolean enabled) {
        exactClientDetection = enabled;
    }

    /** Verify the 26.3 mod-specific scroll packet against its target inventory slot on the server thread. */
    public void verifyClientSelection(Player player, int rawSlot) {
        if (!exactClientDetection || rawSlot < 0 || rawSlot >= player.getOpenInventory().countSlots()) return;
        if (isContainer(player.getOpenInventory().getItem(rawSlot))) {
            verifiedClients.add(player.getUniqueId());
            plugin.syncEnderChest(player);
        }
    }

    boolean isSupportedClient(Player player) {
        if (exactClientDetection) return verifiedClients.contains(player.getUniqueId());
        return player.hasPermission("handyshulker.use");
    }

    private static Object invoke(Event event, String method) throws ReflectiveOperationException {
        Method accessor = event.getClass().getMethod(method);
        return accessor.invoke(event);
    }

    private int selectedIndex(Player player, int rawSlot) {
        return selections.getOrDefault(new SelectionKey(player.getUniqueId(), rawSlot), -1);
    }

    private static boolean isContainer(ItemStack item) {
        if (item == null) return false;
        return item.getType() == Material.ENDER_CHEST || item.getType().name().endsWith("_SHULKER_BOX");
    }

    private static boolean cannotInsertInto(ItemStack container, ItemStack incoming) {
        return container.getType().name().endsWith("_SHULKER_BOX")
                && incoming.getType().name().endsWith("_SHULKER_BOX");
    }

    private static int insert(ItemStack box, ItemStack incoming, Player player) {
        if (box.getType().name().endsWith("_SHULKER_BOX")
                && incoming.getType().name().endsWith("_SHULKER_BOX")) return 0;
        if (incoming.getType() == Material.AIR) return 0;
        Inventory contents = containerInventory(box, player);
        if (contents == null) return 0;
        int original = incoming.getAmount();
        for (int slot = 0; slot < contents.getSize() && incoming.getAmount() > 0; slot++) {
            ItemStack existing = contents.getItem(slot);
            if (existing == null || existing.getType() == Material.AIR || !existing.isSimilar(incoming)) continue;
            int space = Math.min(existing.getMaxStackSize(), contents.getMaxStackSize()) - existing.getAmount();
            if (space > 0) {
                int amount = Math.min(space, incoming.getAmount());
                existing.setAmount(existing.getAmount() + amount);
                incoming.setAmount(incoming.getAmount() - amount);
            }
        }
        for (int slot = 0; slot < contents.getSize() && incoming.getAmount() > 0; slot++) {
            ItemStack existing = contents.getItem(slot);
            if (existing != null && existing.getType() != Material.AIR) continue;
            int amount = Math.min(incoming.getMaxStackSize(), incoming.getAmount());
            contents.setItem(slot, incoming.clone());
            contents.getItem(slot).setAmount(amount);
            incoming.setAmount(incoming.getAmount() - amount);
        }
        if (incoming.getAmount() != original && box.getType() != Material.ENDER_CHEST) {
            saveShulkerInventory(box, contents);
        }
        return original - incoming.getAmount();
    }

    private static ItemStack remove(ItemStack box, Player player, int selectedIndex) {
        Inventory contents = containerInventory(box, player);
        if (contents == null) return null;
        int nonEmpty = 0;
        for (int slot = 0; slot < contents.getSize(); slot++) {
            ItemStack item = contents.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;
            if (selectedIndex < 0 || selectedIndex == nonEmpty) {
                contents.setItem(slot, null);
                if (box.getType() != Material.ENDER_CHEST) saveShulkerInventory(box, contents);
                return item;
            }
            nonEmpty++;
        }
        return null;
    }

    private static Inventory shulkerInventory(ItemStack box) {
        if (box.getItemMeta() instanceof BlockStateMeta meta && meta.getBlockState() instanceof ShulkerBox state) {
            return state.getInventory();
        }
        return null;
    }

    private static Inventory containerInventory(ItemStack box, Player player) {
        if (box.getType() == Material.ENDER_CHEST) return player.getEnderChest();
        return shulkerInventory(box);
    }

    private static void saveShulkerInventory(ItemStack box, Inventory contents) {
        if (!(box.getItemMeta() instanceof BlockStateMeta meta) || !(meta.getBlockState() instanceof ShulkerBox state)) return;
        state.getInventory().setContents(contents.getContents());
        meta.setBlockState(state);
        box.setItemMeta(meta);
    }

    private static void play(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    private record SelectionKey(UUID player, int rawSlot) {}
}
