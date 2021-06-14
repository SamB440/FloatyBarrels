package com.convallyria.floatybarrels.listener;

import com.convallyria.floatybarrels.FloatyBarrels;
import com.convallyria.floatybarrels.event.BarrelFloatEvent;
import com.convallyria.floatybarrels.player.BarrelPlayer;
import com.convallyria.floatybarrels.translation.Translations;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.BubbleColumn;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public record BarrelFloatListener(FloatyBarrels plugin) implements Listener {

    @EventHandler
    public void onFloat(BarrelFloatEvent event) {
        BarrelPlayer barrelPlayer = event.getBarrelPlayer();
        Player player = barrelPlayer.getPlayer();
        Block newBlock = event.getTo();
        if (plugin.canSink() && newBlock.getType() == Material.BUBBLE_COLUMN) {
            BubbleColumn bubbleColumn = (BubbleColumn) newBlock.getBlockData();
            if (bubbleColumn.isDrag()) {
                World world = player.getWorld();
                int minHeight = world.getMinHeight();
                // Find the lowest bubble block of this column
                Block to = null;
                for (int y = newBlock.getY(); y >= minHeight; y--) {
                    Block testBlock = newBlock.getLocation().clone().subtract(0, y, 0).getBlock();
                    if (testBlock.getType() == Material.MAGMA_BLOCK) {
                        event.setTo(to = testBlock.getLocation().clone().add(0, 1, 0).getBlock());
                        break;
                    }
                }
                if (to == null) return;

                Block finalTo = to;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    barrelPlayer.eject(null);
                    if (plugin.canDropItems()) {
                        Barrel barrel = (Barrel) finalTo.getState();
                        for (ItemStack item : barrel.getInventory().getContents()) {
                            if (item != null && item.getType() != Material.AIR) {
                                world.dropItemNaturally(finalTo.getLocation(), item);
                            }
                        }
                        barrel.getInventory().clear();
                    }
                }, 1L);
                Translations.BARREL_SUNK.send(player);
                return;
            }
        }

        if (plugin.blockPath() && newBlock.getType().isSolid()) {
            Translations.BARREL_PATH_BLOCKED.send(player);
            event.setCancelled(true);
        }
    }
}
