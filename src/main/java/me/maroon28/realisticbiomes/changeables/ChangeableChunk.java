package me.maroon28.realisticbiomes.changeables;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.io.Serializable;
import java.util.Objects;

public record ChangeableChunk(Chunk chunk, ChangeableBiome changeableBiome) implements Serializable {
    public void changeBiome() {
        Biome biome = changeableBiome.biome();
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
        return "ChangeableChunk{" +
                "chunk=" + chunk +
                ", changeableBiome=" + changeableBiome +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChangeableChunk that = (ChangeableChunk) o;

        if (!Objects.equals(chunk, that.chunk)) return false;
        return Objects.equals(changeableBiome, that.changeableBiome);
    }

    @Override
    public int hashCode() {
        int result = chunk != null ? chunk.hashCode() : 0;
        result = 31 * result + (changeableBiome != null ? changeableBiome.hashCode() : 0);
        return result;
    }
}
