package com.convallyria.floatybarrels.player;

import com.convallyria.floatybarrels.FloatyBarrels;
import com.convallyria.floatybarrels.barrel.SteerKey;
import com.convallyria.floatybarrels.event.BarrelFloatEvent;
import com.convallyria.floatybarrels.utils.BlockUtils;
import net.minecraft.world.entity.monster.EntitySlime;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftSlime;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Optional;

public class BarrelPlayer {

    private final FloatyBarrels plugin;
    private final Player player;
    private Barrel barrel;
    private long barrelEnterTime;
    private Slime slime;
    private long cooldown;

    public BarrelPlayer(Player player) {
        this.plugin = JavaPlugin.getPlugin(FloatyBarrels.class);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public Optional<Barrel> getBarrel() {
        return Optional.ofNullable(barrel);
    }

    public void setBarrel(Barrel barrel, Slime slime) {
        this.barrel = barrel;
        this.barrelEnterTime = System.currentTimeMillis();
        this.slime = slime;
        this.cooldown = System.currentTimeMillis();
    }

    public long getBarrelEnterTime() {
        return barrelEnterTime;
    }

    public long getTicksPassedSinceBarrelEnter() {
        return ((System.currentTimeMillis() - barrelEnterTime) / 1000) * 20;
    }

    public boolean hasCooldown() {
        return ((System.currentTimeMillis() - cooldown) / 1000) < plugin.getMovementCooldown();
    }

    public void setBarrelEnterTime(long barrelEnterTime) {
        this.barrelEnterTime = barrelEnterTime;
    }

    public Slime getSlime() {
        return slime;
    }

    public void handleInput(@Nullable SteerKey forward, @Nullable SteerKey side) {
        if (!Bukkit.isPrimaryThread()) {
            plugin.getLogger().severe("Cannot handle input off main thread!");
            return;
        }

        if (hasCooldown()) return;
        if (barrel != null) {
            Block newBlock = null;
            if (side != null) {
                Pair<Block, Block> leftRight = BlockUtils.getLeftAndRightBlocks(player, barrel.getBlock());
                newBlock = side == SteerKey.A ? leftRight.getLeft() : leftRight.getRight();
            } else if (forward != null) {
                final BlockFace facing = forward == SteerKey.S ? player.getFacing().getOppositeFace() : player.getFacing();
                newBlock = barrel.getBlock().getRelative(facing);
            }

            if (newBlock == null) return;
            BarrelFloatEvent barrelFloatEvent = new BarrelFloatEvent(this, newBlock);
            Bukkit.getPluginManager().callEvent(barrelFloatEvent);
            if (barrelFloatEvent.isCancelled()) return;
            newBlock = barrelFloatEvent.getTo();

            BlockState state = newBlock.getState();
            state.setType(Material.BARREL);
            state.setBlockData(barrel.getBlockData());
            state.update(true, true);

            Location newLocation = newBlock.getLocation().clone().add(0.5,0.5,0.5);
            EntitySlime nmsSlime = ((CraftSlime) slime).getHandle();
            // Have to use NMS as CB blocks entities with passengers from being teleported.
            nmsSlime.enderTeleportTo(newLocation.getX(), newLocation.getY(), newLocation.getZ());

            Barrel newBarrel = (Barrel) newBlock.getState();
            newBarrel.open();
            if (plugin.canMoveInventory()) {
                newBarrel.getInventory().setContents(barrel.getInventory().getContents()); // Copy inventory
                barrel.getInventory().clear(); // No duping, thanks!
            }
            barrel.getBlock().setType(plugin.getReplaceBlock(), true);
            this.barrel = newBarrel;
            this.cooldown = System.currentTimeMillis();
        }
    }

    public void eject(@Nullable EntityDismountEvent event) {
        FloatyBarrels plugin = JavaPlugin.getPlugin(FloatyBarrels.class);
        if (getTicksPassedSinceBarrelEnter() < 40) {
            if (event != null) event.setCancelled(true);
            return;
        }

        slime.remove();
        barrel.close();
        if (plugin.overrideColliding() && !player.isCollidable()) player.setCollidable(true);
        plugin.removeBarrelPlayer(player);
    }
}
