package com.convallyria.floatybarrels.event;

import com.convallyria.floatybarrels.player.BarrelPlayer;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class BarrelFloatEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final BarrelPlayer barrelPlayer;
    private Block to;
    private boolean cancel;

    public BarrelFloatEvent(@NotNull final BarrelPlayer barrelPlayer, @NotNull final Block to) {
        this.barrelPlayer = barrelPlayer;
        this.to = to;
    }

    @NotNull
    public BarrelPlayer getBarrelPlayer() {
        return barrelPlayer;
    }

    @NotNull
    public Block getTo() {
        return to;
    }

    public void setTo(@NotNull Block to) {
        this.to = to;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
