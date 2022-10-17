package me.maroon28.realisticbiomes.api;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockListRemoveEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final List<Block> block;

    public BlockListRemoveEvent(List<Block> block) {
        this.block = block;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public List<Block> getBlockList() {
        return block;
    }
}
