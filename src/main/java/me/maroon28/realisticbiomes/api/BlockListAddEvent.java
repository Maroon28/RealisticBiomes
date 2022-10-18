package me.maroon28.realisticbiomes.api;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockListAddEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final List<Block> block;

    public BlockListAddEvent(List<Block> block) {
        this.block = block;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public List<Block> getBlockList() {
        return block;
    }
}
