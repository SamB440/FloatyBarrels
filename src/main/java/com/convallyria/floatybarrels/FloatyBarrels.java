package com.convallyria.floatybarrels;

import com.convallyria.floatybarrels.barrel.SteerKey;
import com.convallyria.floatybarrels.barrel.SteerType;
import com.convallyria.floatybarrels.listener.BarrelFloatListener;
import com.convallyria.floatybarrels.listener.BarrelListener;
import com.convallyria.floatybarrels.player.BarrelPlayer;
import com.convallyria.floatybarrels.tinyprotocol.TinyProtocol;
import io.netty.channel.Channel;
import net.minecraft.network.protocol.game.PacketPlayInSteerVehicle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class FloatyBarrels extends JavaPlugin {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.00");
    private final Map<UUID, BarrelPlayer> barrelPlayers = new HashMap<>();

    private TinyProtocol protocol;

    @Override
    public void onEnable() {
        if (protocol == null) {
            protocol = new TinyProtocol(this) {
                @Override
                public Object onPacketInAsync(Player sender, Channel channel, Object packet) {
                    if (packet instanceof PacketPlayInSteerVehicle steerVehicle) {
                        getBarrelPlayer(sender.getUniqueId()).ifPresent(barrelPlayer -> {
                            barrelPlayer.getBarrel().ifPresent(barrel -> {
                                double forward = Double.parseDouble(DECIMAL_FORMAT.format(steerVehicle.c()));
                                double side = Double.parseDouble(DECIMAL_FORMAT.format(steerVehicle.b()));
                                SteerKey forwardSteerKey = SteerKey.getSteerKey(SteerType.FORWARD, forward);
                                SteerKey sideSteerKey = SteerKey.getSteerKey(SteerType.SIDE, side);
                                Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(FloatyBarrels.class), () -> barrelPlayer.handleInput(forwardSteerKey, sideSteerKey));
                            });
                        });
                    }
                    return super.onPacketInAsync(sender, channel, packet);
                }
            };
        }
        this.createConfig();
        this.registerListeners();
        getLogger().info("Floating along a river!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Exiting the barrels!");
        for (Player player : Bukkit.getOnlinePlayers()) {
            getBarrelPlayer(player.getUniqueId()).ifPresent(barrelPlayer -> {
                barrelPlayer.getBarrel().ifPresent(barrel -> barrelPlayer.eject(null));
            });
        }
    }

    private void createConfig() {
        this.saveDefaultConfig();
    }

    private void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BarrelListener(this), this);
        pluginManager.registerEvents(new BarrelFloatListener(this), this);
    }

    public Optional<BarrelPlayer> getBarrelPlayer(UUID uuid) {
        return Optional.ofNullable(barrelPlayers.get(uuid));
    }

    public BarrelPlayer addBarrelPlayer(Player player) {
        if (barrelPlayers.containsKey(player.getUniqueId())) return barrelPlayers.get(player.getUniqueId());
        BarrelPlayer barrelPlayer = new BarrelPlayer(player);
        barrelPlayers.put(player.getUniqueId(), barrelPlayer);
        return barrelPlayer;
    }

    public void removeBarrelPlayer(Player player) {
        barrelPlayers.remove(player.getUniqueId());
    }
}
