package me.maroon28.realisticbiomes.tasks;

import me.maroon28.realisticbiomes.RealisticBiomes;
import me.maroon28.realisticbiomes.evolvables.EvolvableBiome;
import me.maroon28.realisticbiomes.evolvables.EvolvableChunk;
import me.maroon28.realisticbiomes.evolvables.RequiredBlock;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import static me.maroon28.realisticbiomes.RealisticBiomes.chunksToStamp;

public class ChunkStampTask extends BukkitRunnable {
    private final RealisticBiomes realisticBiomes;
    private Chunk chunk;

    public ChunkStampTask(RealisticBiomes realisticBiomes) {
        this.realisticBiomes = realisticBiomes;
    }

    @Override
    public void run() {
        var iterator = chunksToStamp.iterator();
        ArrayList<EvolvableBiome> loadedBiomes = realisticBiomes.getLoadedBiomes();
        while (iterator.hasNext()) {
            this.chunk = iterator.next();

            for (EvolvableBiome evolvableBiome : loadedBiomes) {
                // If there's a saved biome, check if it's the same as the one we're currently at
                // No need to stamp if the biome isn't going to be different.
                if (getSavedBiome() != null && getSavedBiome().equals(evolvableBiome.biome())) continue;

                for (RequiredBlock block: evolvableBiome.requiredBlocks()) {
                    int amount = getMaterialAmount(block.material());
                    // One or more of the required blocks is less than needed, break
                    if (block.amount() > amount) break;
                    stampChunk(evolvableBiome);
                }
            }
            // The chunk either got stamped or not, so no need to hold onto it.
            // Remove it with the iterator to avoid ConcurrentModification Exceptions
            iterator.remove();
        }

    }

    private Biome getSavedBiome() {
        var container = chunk.getPersistentDataContainer();
        var key = new NamespacedKey(realisticBiomes, "saved-biome");
        if (container.has(key)) {
            return Biome.valueOf(container.get(key, PersistentDataType.STRING));
        }
        return null;
    }

    private void stampChunk(EvolvableBiome biome) {
        var container = chunk.getPersistentDataContainer();
        var key = new NamespacedKey(realisticBiomes, "time-until-change");
        // The chunk is already stamped
        if (container.has(key)) return;
        // Save a timestamp of when it became viable to change.
        container.set(key, PersistentDataType.LONG, System.currentTimeMillis());
        // Move it to the evolving chunk map
        RealisticBiomes.evolvableChunks.add(new EvolvableChunk(chunk, biome));
    }

    private int getMaterialAmount(Material material) {
        var container = chunk.getPersistentDataContainer();
        var key = getKey(material);
        if (container.has(key, PersistentDataType.INTEGER)) {
            return container.get(key, PersistentDataType.INTEGER);
        }
        return 0;
    }

    @NotNull
    private NamespacedKey getKey(Material material) {
        return new NamespacedKey(realisticBiomes, material.toString());
    }
}
