package dev.handy.mods.handyshulker.paper.v263;

import dev.handy.mods.handyshulker.paper.ShulkerInventoryListener;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/** Minecraft 26.3 wire-codec adapter for HandyShulker's existing S2C payload. */
public final class Paper263PayloadSender implements Listener {
    private static final String CHANNEL = "handyshulker:sync_ender_chest";
    private static final String SELECTION_HANDLER = "handyshulker_bundle_selection";
    private static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> CODEC =
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list());

    private final JavaPlugin plugin;
    private final ShulkerInventoryListener inventoryListener;

    public Paper263PayloadSender(JavaPlugin plugin, ShulkerInventoryListener inventoryListener) {
        this.plugin = plugin;
        this.inventoryListener = inventoryListener;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        java.util.UUID playerId = player.getUniqueId();
        Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
        channel.eventLoop().execute(() -> {
            ChannelPipeline pipeline = channel.pipeline();
            if (pipeline.get(SELECTION_HANDLER) != null) return;
            pipeline.addBefore("packet_handler", SELECTION_HANDLER, new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
                    if (message instanceof ServerboundSelectBundleItemPacket packet) {
                        int slot = packet.slotId();
                        int selected = packet.selectedItemIndex();
                        // Store before forwarding: Paper queues packet handling to the server thread,
                        // while scheduler work may run after a following click packet.
                        inventoryListener.rememberSelection(playerId, slot, selected);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player online = Bukkit.getPlayer(playerId);
                            if (online != null) inventoryListener.verifyClientSelection(online, slot);
                        });
                    }
                    context.fireChannelRead(message);
                }
            });
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        inventoryListener.clearSelections(event.getPlayer().getUniqueId());
        Channel channel = ((CraftPlayer) event.getPlayer()).getHandle().connection.connection.channel;
        channel.eventLoop().execute(() -> {
            if (channel.pipeline().get(SELECTION_HANDLER) != null) {
                channel.pipeline().remove(SELECTION_HANDLER);
            }
        });
    }

    public void send(Player player) {
        ServerPlayer handle = ((CraftPlayer) player).getHandle();
        Inventory enderChest = player.getEnderChest();
        List<ItemStack> contents = new ArrayList<>(enderChest.getSize());
        for (org.bukkit.inventory.ItemStack item : enderChest.getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                contents.add(ItemStack.EMPTY);
            } else {
                contents.add(org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(item));
            }
        }

        ByteBuf raw = Unpooled.buffer();
        try {
            RegistryAccess registries = handle.level().registryAccess();
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(raw, registries);
            CODEC.encode(buffer, contents);
            player.sendPluginMessage(plugin, CHANNEL, ByteBufUtil.getBytes(buffer));
        } finally {
            raw.release();
        }
    }
}
