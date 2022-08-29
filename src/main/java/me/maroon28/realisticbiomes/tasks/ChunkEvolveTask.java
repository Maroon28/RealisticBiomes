package me.maroon28.realisticbiomes.tasks;

import me.maroon28.realisticbiomes.RealisticBiomes;
import me.maroon28.realisticbiomes.changeables.ChangeableChunk;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import static me.maroon28.realisticbiomes.RealisticBiomes.changeableChunks;

public class ChunkEvolveTask extends BukkitRunnable {
    private final @NotNull Logger logger;
    private Chunk chunk;
    private final RealisticBiomes realisticBiomes;

    public ChunkEvolveTask(RealisticBiomes realisticBiomes) {
        this.realisticBiomes = realisticBiomes;
        logger = realisticBiomes.getLogger();
    }

    @Override
    public void run() {
        int evolvedChunks = 0;
        var iterator = changeableChunks.iterator();
        while (iterator.hasNext()) {
            var changeableChunk = iterator.next();
            this.chunk = changeableChunk.chunk();
            Biome biome = changeableChunk.changeableBiome().biome();
            if (hasEnoughTime(changeableChunk)) {
                changeableChunk.changeBiome();
                removeStamp();
                saveGeneralBiome(biome);
                evolvedChunks++;
                // The chunk has enough time now, so we can remove it from the set using the iterator
                iterator.remove();
            }
        }
        if (evolvedChunks > 0) {
            logger.log(Level.INFO, "Successfully modified the biomes of " + evolvedChunks + " chunks!");
        }
    }

    private boolean hasEnoughTime(ChangeableChunk chunk) {
        long chunkTime = getStampedTime();
        int neededTime = chunk.changeableBiome().time() * 1000;
        return System.currentTimeMillis() - chunkTime > neededTime;
    }

    private long getStampedTime() {
        var container = chunk.getPersistentDataContainer();
        NamespacedKey key = getKey();
        if (container.has(key, PersistentDataType.LONG)) {
            return container.get(key, PersistentDataType.LONG);
        }
        return 0;
    }

    private void removeStamp() {
        var container = chunk.getPersistentDataContainer();
        var key = getKey();
        container.remove(key);
    }


    private void saveGeneralBiome(Biome biome) {
        var container = chunk.getPersistentDataContainer();
        var key = new NamespacedKey(realisticBiomes, "saved-biome");
        // If the chunk already has a saved biome, override it
        container.set(key, PersistentDataType.STRING, biome.toString());
    }

    @NotNull
    private NamespacedKey getKey() {
        return new NamespacedKey(realisticBiomes, "time-until-change");
    }

}
