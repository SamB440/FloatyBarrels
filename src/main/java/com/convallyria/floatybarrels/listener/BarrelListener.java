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
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

public record BarrelListener(FloatyBarrels plugin) implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        final Player player = event.getPlayer();
        if (!player.hasPermission("floatybarrels.float")) return;
        if (!player.isSneaking() && plugin.explodingBarrels()
                && event.getItem() != null && event.getItem().getType() == Material.FLINT_AND_STEEL) {
            plugin.getBarrelPlayer(player.getUniqueId()).ifPresent(barrelPlayer -> barrelPlayer.getBarrel().ifPresent(barrel -> {
                barrelPlayer.setBarrelEnterTime(0); // Important! Will dupe barrels if this is not set.
                barrelPlayer.eject(null);
                boolean hasExplosives = barrel.getInventory().contains(Material.TNT);
                if (!hasExplosives) return;
                int explosivePower = 0;
                for (ItemStack stack : barrel.getInventory().getContents()) {
                    if (stack != null && stack.getType() == Material.TNT) {
                        explosivePower = explosivePower + stack.getAmount();
                    }
                }
                int clampedExplosivePower = Math.min(explosivePower, plugin.maxExplosiveStrength());
                player.getWorld().createExplosion(barrel.getLocation(), clampedExplosivePower, false, true, player);
                event.setUseInteractedBlock(Event.Result.DENY);
                Translations.BARREL_EXPLODED.send(player);
            }));
            return;
        }

        final Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.BARREL || !player.isSneaking()) return;
        if (isSurroundedByWater(block)) {
            Barrel barrel = (Barrel) block.getState();
            Directional directional = (Directional) barrel.getBlockData();
            if (!plugin.getValidFaces().contains(directional.getFacing())) {
                return;
            }

            barrel.open();

            event.setUseInteractedBlock(Event.Result.DENY); // We don't want them opening the inventory!

            // Spawn a slime at centre of barrel and set its properties
            final Location barrelCentre = block.getLocation().clone().add(0.5, 0.3, 0.5);
            player.getWorld().spawn(barrelCentre, Slime.class, slimeSpawn -> {
                slimeSpawn.setInvisible(true);
                slimeSpawn.setGravity(false);
                slimeSpawn.setSize(1);
                slimeSpawn.setAI(false);
                slimeSpawn.setAware(false);
                slimeSpawn.setCollidable(false);
                slimeSpawn.setVelocity(new Vector(0, 0, 0));
                slimeSpawn.setInvisible(true);
                slimeSpawn.setInvulnerable(true);
                slimeSpawn.setPersistent(true);
                slimeSpawn.setSilent(true);

                if (plugin.overrideColliding() && player.isCollidable()) player.setCollidable(false);

                BarrelPlayer barrelPlayer = plugin.addBarrelPlayer(player);
                barrelPlayer.setBarrel(barrel, slimeSpawn);

                slimeSpawn.addPassenger(player); // Add player as passenger on slime
                Translations.BARREL_FLOATING.send(player);
            });
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
