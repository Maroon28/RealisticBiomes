package me.maroon28.realisticbiomes.api;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BlockRemoveEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Block block;

    public BlockRemoveEvent(Block block) {
        this.block = block;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public Block getBlock() {
        return block;
    }
}
