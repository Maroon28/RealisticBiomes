package me.maroon28.realisticbiomes.api;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BlockAddEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Block block;

    public BlockAddEvent(Block block) {
        this.block = block;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public Block getBlock() {
        return block;
    }
}
