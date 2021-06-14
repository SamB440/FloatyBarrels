package com.convallyria.floatybarrels.listener;

import com.convallyria.floatybarrels.FloatyBarrels;
import com.convallyria.floatybarrels.player.BarrelPlayer;
import com.convallyria.floatybarrels.translation.Translations;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

public record BarrelListener(FloatyBarrels plugin) implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        final Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        final Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.BARREL) return;
        if (isSurroundedByWater(block)) {
            Barrel barrel = (Barrel) block.getState();
            Directional directional = (Directional) barrel.getBlockData();
            if (!plugin.getValidFaces().contains(directional.getFacing())) {
                return;
            }

            barrel.open();

            event.setUseInteractedBlock(Event.Result.DENY); // We don't want them opening the inventory!

            // Spawn an arrow at centre of barrel and set its properties
            final Location barrelCentre = block.getLocation().clone().add(0.5, 0.3, 0.5);
            Slime slime = (Slime) player.getWorld().spawnEntity(barrelCentre, EntityType.SLIME);
            slime.setGravity(false);
            slime.setSize(1);
            slime.setAI(false);
            slime.setAware(false);
            slime.setCollidable(false);
            slime.setVelocity(new Vector(0, 0, 0));
            slime.setInvisible(true);
            slime.setInvulnerable(true);
            slime.setPersistent(true);
            slime.setSilent(true);

            if (plugin.overrideColliding() && player.isCollidable()) player.setCollidable(false);

            BarrelPlayer barrelPlayer = plugin.addBarrelPlayer(player);
            barrelPlayer.setBarrel(barrel, slime);
            slime.addPassenger(player); // Add player as passenger on arrow

            Translations.BARREL_FLOATING.send(player);
        }
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        Entity dismounted = event.getEntity();
        Entity entity = event.getDismounted();
        if (dismounted instanceof Player player
            && entity instanceof Slime slime) {
            Block block = slime.getLocation().getBlock();
            if (block.getType() == Material.BARREL) {
                plugin.getBarrelPlayer(player.getUniqueId()).ifPresent(barrelPlayer -> {
                    barrelPlayer.getBarrel().ifPresent(barrel -> barrelPlayer.eject(event));
                });
            }
        }
    }

    private boolean isSurroundedByWater(Block block) {
        final Location start = block.getLocation();
        if (!plugin.requireWater()) return start.clone().add(0, 1, 0).getBlock().getType().isAir();

        final int radius = 1;
        for (double x = start.getX() - radius; x <= start.getX() + radius; x++) {
            for (double y = start.getY() - radius; y <= start.getY() + radius; y++) {
                for (double z = start.getZ() - radius; z <= start.getZ() + radius; z++) {
                    Location location = new Location(start.getWorld(), x, y, z);
                    if (location.getBlock().getType() != Material.WATER
                            && location.getBlock().getType() != Material.AIR
                            && location.getBlock().getType() != Material.BARREL) {
                        return false;
                    }
                }
            }
        }
        return start.clone().add(0, 1, 0).getBlock().getType().isAir();
    }
}
