package me.maroon28.realisticbiomes.tasks;

import me.maroon28.realisticbiomes.RealisticBiomes;
import me.maroon28.realisticbiomes.evolvables.EvolvableChunk;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static me.maroon28.realisticbiomes.RealisticBiomes.evolvableChunks;

public class ChunkEvolveTask extends BukkitRunnable {
    private final @NotNull Logger logger;
    private final FileConfiguration config;
    private Chunk chunk;
    private final RealisticBiomes realisticBiomes;

    public ChunkEvolveTask(RealisticBiomes realisticBiomes) {
        this.realisticBiomes = realisticBiomes;
        logger = realisticBiomes.getLogger();
        config = realisticBiomes.getConfig();
    }

    @Override
    public void run() {
        int evolvedChunks = 0;
        var iterator = evolvableChunks.iterator();
        while (iterator.hasNext()) {
            var evolvableChunk = iterator.next();
            this.chunk = evolvableChunk.chunk();
            Biome biome = evolvableChunk.evolvableBiome().biome();
            if (hasEnoughTime(evolvableChunk)) {
                changeBiome(evolvableChunk);
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

    private boolean hasEnoughTime(EvolvableChunk chunk) {
        long chunkTime = getStampedTime();
        int neededTime = chunk.evolvableBiome().time() * 1000;
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

    private void changeBiome(EvolvableChunk evolvableChunk) {
        Biome biome = evolvableChunk.evolvableBiome().biome();
        Chunk chunk = evolvableChunk.chunk();
        int cX = chunk.getX() * 16;
        int cZ = chunk.getZ() * 16;
        World world = chunk.getWorld();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y <= world.getMaxHeight(); y++) {
                    Block block = world.getBlockAt(x + cX, y, z + cZ);
                    if (canChangeBiome(block))
                        block.setBiome(biome);
                }
            }
        }
    }

    private boolean canChangeBiome(Block block) {
        List<String> blacklistedBiomes = config.getStringList("blacklisted-biomes");
        if (config.getBoolean("use-biome-blacklist-as-whitelist")) {
            return blacklistedBiomes.contains(block.getBiome().toString());
        } else {
            return !blacklistedBiomes.contains(block.getBiome().toString());
        }
    }

    @NotNull
    private NamespacedKey getKey() {
        return new NamespacedKey(realisticBiomes, "time-until-change");
    }

}
