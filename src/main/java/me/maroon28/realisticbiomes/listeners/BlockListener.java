package me.maroon28.realisticbiomes.listeners;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import me.maroon28.realisticbiomes.RealisticBiomes;
import me.maroon28.realisticbiomes.api.BlockAddEvent;
import me.maroon28.realisticbiomes.api.BlockListAddEvent;
import me.maroon28.realisticbiomes.api.BlockListRemoveEvent;
import me.maroon28.realisticbiomes.api.BlockRemoveEvent;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;

public class BlockListener implements Listener {
    private Chunk chunk;
    private final RealisticBiomes realisticBiomes;
    private final @NotNull FileConfiguration config;

    public BlockListener(RealisticBiomes realisticBiomes) {
        this.realisticBiomes = realisticBiomes;
        config = realisticBiomes.getConfig();
    }

    @EventHandler
    public void onBlockAdd(BlockAddEvent event) {
        Material type = event.getBlock().getType();
        chunk = event.getBlock().getChunk();
        if (increaseMaterialAmount(type)) {
            queueChunk();
        }
    }
    @EventHandler
    public void onBlockRemove(BlockRemoveEvent event) {
        Material type = event.getBlock().getType();
        chunk = event.getBlock().getChunk();
        if (decreaseMaterialAmount(type)) {
            queueChunk();
        }
    }

    @EventHandler
    public void onBlockListAdd(BlockListAddEvent event) {
        List<Block> blocks = event.getBlockList();
        if (blocks.isEmpty()) return;
        chunk = blocks.get(0).getChunk();
        for (Block block: blocks) {
            chunk = chunk == block.getChunk() ? chunk : block.getChunk();
            if (increaseMaterialAmount(block.getType())) {
                queueChunk();
            }
        }
    }
    @EventHandler
    public void onBlockListRemove(BlockListRemoveEvent event) {
        List<Block> blocks = event.getBlockList();
        if (blocks.isEmpty()) return;
        chunk = blocks.get(0).getChunk();
        for (Block block: blocks) {
            chunk = chunk == block.getChunk() ? chunk : block.getChunk();
            if (increaseMaterialAmount(block.getType())) {
                queueChunk();
            }
        }
    }

    // Regular event listeners, delegate to custom events when possible to avoid duplicate code.

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        callEvent(new BlockRemoveEvent(event.getBlock()));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        callEvent(new BlockAddEvent(event.getBlock()));
    }

    @EventHandler
    public void onBlockBreakBlock(BlockBreakBlockEvent event) {
        callEvent(new BlockRemoveEvent(event.getBlock()));
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        callEvent(new BlockListRemoveEvent(event.blockList()));
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        callEvent(new BlockListRemoveEvent(event.blockList()));
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        callEvent(new BlockRemoveEvent(event.getBlock()));
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        callEvent(new BlockRemoveEvent(event.getBlock()));
    }

    @EventHandler
    public void onTreeGrow(StructureGrowEvent event) {
        chunk = event.getLocation().getChunk();
        for (var block: event.getBlocks()) {
            if (increaseMaterialAmount(block.getType())) {
                queueChunk();
            }
        }
    }

    private void queueChunk() {
        List<String> enabledWorlds = config.getStringList("enabled-worlds");
        if (enabledWorlds.contains(chunk.getWorld().getName()))
            RealisticBiomes.chunksToStamp.add(chunk);
    }

    private void callEvent(Event event) {
        realisticBiomes.getServer().getPluginManager().callEvent(event);
    }

    private boolean isValidMaterial(Material type) {
        return realisticBiomes.getValidMaterials().contains(type);
    }

    private void debug(String message) {
        if (!config.getBoolean("debug")) return;
        realisticBiomes.getLogger().log(Level.INFO, message);
    }
    private void storeMaterialAmount(Material material, int amount) {
        var container = chunk.getPersistentDataContainer();
        var key = getKey(material);
        container.set(key, PersistentDataType.INTEGER, amount);
    }

    private int getMaterialAmount(Material material) {
        var container = chunk.getPersistentDataContainer();
        var key = getKey(material);
        if (container.has(key, PersistentDataType.INTEGER)) {
            return container.get(key, PersistentDataType.INTEGER);
        } else {
            int amount = calculateMaterialAmount(material);
            storeMaterialAmount(material, amount);
            return amount;
        }
    }

    private boolean increaseMaterialAmount(Material material) {
        if (!isValidMaterial(material)) return false;
        int amount = getMaterialAmount(material) + 1;
        storeMaterialAmount(material, amount);
        debug("Increased amount of " + material + " to " + amount);
        return true;
    }

    private boolean decreaseMaterialAmount(Material material) {
        if (!isValidMaterial(material)) return false;
        int amount = getMaterialAmount(material) - 1;
        storeMaterialAmount(material, Math.max(0, amount));
        debug("Decreased amount of " + material + " to " + amount);
        return true;
    }

    @NotNull
    private NamespacedKey getKey(Material material) {
        return new NamespacedKey(realisticBiomes, material.toString());
    }

    private int calculateMaterialAmount(Material material) {
        int amount = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < chunk.getWorld().getMaxHeight(); y++) {
                    if (chunk.getBlock(x, y, z).getType() == material)
                        amount++;
                }
            }
        }
        return amount;
    }
}
