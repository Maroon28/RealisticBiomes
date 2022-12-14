package me.maroon28.realisticbiomes.evolvables;

import org.bukkit.block.Biome;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public record EvolvableBiome(Biome biome, ArrayList<RequiredBlock> requiredBlocks, int time) implements Serializable {
    @Override
    public String toString() {
        return "EvolvableBiome{" +
                "biome=" + biome +
                ", requiredBlocks=" + requiredBlocks +
                ", time=" + time +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EvolvableBiome that = (EvolvableBiome) o;

        if (time != that.time) return false;
        if (biome != that.biome) return false;
        return Objects.equals(requiredBlocks, that.requiredBlocks);
    }

    @Override
    public int hashCode() {
        int result = biome != null ? biome.hashCode() : 0;
        result = 31 * result + (requiredBlocks != null ? requiredBlocks.hashCode() : 0);
        result = 31 * result + time;
        return result;
    }
}
