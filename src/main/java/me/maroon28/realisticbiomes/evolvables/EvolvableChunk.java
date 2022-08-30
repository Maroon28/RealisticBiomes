package me.maroon28.realisticbiomes.evolvables;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.io.Serializable;
import java.util.Objects;

public record EvolvableChunk(Chunk chunk, EvolvableBiome evolvableBiome) implements Serializable {
    public void changeBiome() {
        Biome biome = evolvableBiome.biome();
        int cX = chunk.getX() * 16;
        int cZ = chunk.getZ() * 16;
        World world = chunk.getWorld();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y <= world.getMaxHeight(); y++) {
                    Block block = world.getBlockAt(x + cX, y, z + cZ);
                    block.setBiome(biome);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "EvolvableChunk{" +
                "chunk=" + chunk +
                ", evolvableBiome=" + evolvableBiome +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EvolvableChunk that = (EvolvableChunk) o;

        if (!Objects.equals(chunk, that.chunk)) return false;
        return Objects.equals(evolvableBiome, that.evolvableBiome);
    }

    @Override
    public int hashCode() {
        int result = chunk != null ? chunk.hashCode() : 0;
        result = 31 * result + (evolvableBiome != null ? evolvableBiome.hashCode() : 0);
        return result;
    }
}
