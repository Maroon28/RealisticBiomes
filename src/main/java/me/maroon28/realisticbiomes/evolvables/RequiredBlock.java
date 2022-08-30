package me.maroon28.realisticbiomes.evolvables;

import org.bukkit.Material;

import java.io.Serializable;

public record RequiredBlock(Material material, String biomeName, int amount) implements Serializable {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequiredBlock that = (RequiredBlock) o;

        if (amount != that.amount) return false;
        if (!biomeName.equals(that.biomeName)) return false;
        return material == that.material;
    }

    @Override
    public int hashCode() {
        int result = material.hashCode();
        result = 31 * result + biomeName.hashCode();
        result = 31 * result + amount;
        return result;
    }

    @Override
    public String toString() {
        return "RequiredBlock{" +
                "material=" + material +
                ", amount=" + amount +
                '}';
    }
}
