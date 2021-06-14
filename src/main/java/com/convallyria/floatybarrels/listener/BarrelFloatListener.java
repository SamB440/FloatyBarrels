package com.convallyria.floatybarrels.listener;

import com.convallyria.floatybarrels.FloatyBarrels;
import com.convallyria.floatybarrels.event.BarrelFloatEvent;
import com.convallyria.floatybarrels.player.BarrelPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.BubbleColumn;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public record BarrelFloatListener(FloatyBarrels plugin) implements Listener {

    @EventHandler
    public void onFloat(BarrelFloatEvent event) {
        BarrelPlayer barrelPlayer = event.getBarrelPlayer();
        Block newBlock = event.getTo();
        if (newBlock.getType() == Material.BUBBLE_COLUMN) {
            BubbleColumn bubbleColumn = (BubbleColumn) newBlock.getBlockData();
            if (bubbleColumn.isDrag()) {
                World world = barrelPlayer.getPlayer().getWorld();
                int minHeight = world.getMinHeight();
                // Find the lowest bubble block of this column
                for (int y = newBlock.getY(); y >= minHeight; y--) {
                    Block testBlock = newBlock.getLocation().clone().subtract(0, y, 0).getBlock();
                    if (testBlock.getType() == Material.MAGMA_BLOCK) {
                        event.setTo(testBlock.getLocation().clone().add(0, 1, 0).getBlock());
                        break;
                    }
                }

                Bukkit.getScheduler().runTaskLater(plugin, () -> barrelPlayer.eject(null), 1L);
                barrelPlayer.getPlayer().sendMessage(ChatColor.RED + "Yer barrel be sunk!");
                return;
            }
        }

        if (newBlock.getType().isSolid()) {
            // TODO translation
            barrelPlayer.getPlayer().sendMessage(ChatColor.RED + "The path is blocked!");
            event.setCancelled(true);
        }
    }
}
