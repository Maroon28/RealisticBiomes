package me.maroon28.realisticbiomes;

import me.maroon28.realisticbiomes.changeables.ChangeableBiome;
import me.maroon28.realisticbiomes.changeables.ChangeableBlock;
import me.maroon28.realisticbiomes.changeables.ChangeableChunk;
import me.maroon28.realisticbiomes.listeners.BlockListener;
import me.maroon28.realisticbiomes.tasks.ChunkEvolveTask;
import me.maroon28.realisticbiomes.tasks.ChunkStampTask;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class RealisticBiomes extends JavaPlugin {
    FileConfiguration config = getConfig();
    ArrayList<ChangeableBiome> loadedBiomes = new ArrayList<>();

    Set<Material> validMaterials = new HashSet<>();
    public static Set<Chunk> chunksToStamp = new HashSet<>();
    public static Set<ChangeableChunk> changeableChunks = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        fetchChangeableBiomes();
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        runTasks();
        deserializeSets();
    }

    @Override
    public void onDisable() {
        serializeHashSet("changeable-chunks.dat", (HashSet<?>) changeableChunks);
        serializeHashSet("stampable-chunks.dat", (HashSet<?>) chunksToStamp);
    }

    private void runTasks() {
        var evolveTask = new ChunkEvolveTask(this);
        var stampTask = new ChunkStampTask(this);
        evolveTask.runTaskTimer(this, 0, config.getInt("tasks.evolve-interval") * 20L);
        stampTask.runTaskTimer(this, 0, config.getInt("tasks.stamp-interval") * 20L);
    }

    private void deserializeSets() {
        changeableChunks = (Set<ChangeableChunk>) deserializeHashSet("changeable-chunks.dat");
        chunksToStamp = (Set<Chunk>) deserializeHashSet("stampable-chunks.dat");
    }


    private void fetchChangeableBiomes() {
        Set<String> configuredBiomes = config.getConfigurationSection("biomes").getKeys(false);
        for (var biomeSection : configuredBiomes) {
            int configuredTime = config.getInt(biomeSection + "." + "time");
            // if the value is 0, default it to 1000
            int time = configuredTime == 0 ? 1000 : configuredTime;
            var blocks = fetchChangeableBlocks(biomeSection);
            var biome = Biome.valueOf(biomeSection);
            loadedBiomes.add(new ChangeableBiome(biome, blocks, time));
        }

    }

    private ArrayList<ChangeableBlock> fetchChangeableBlocks(String biomeSection) {

        Set<String> biomeValues = config.getConfigurationSection(biomeSection).getKeys(false);
        if (biomeValues.isEmpty()) return null;

        ArrayList<ChangeableBlock> blocks = new ArrayList<>();
        for (var value : biomeValues) {
            if (value.equals("time")) continue;
            Material material = Material.valueOf(value);
            int requiredAmount = config.getInt(biomeSection + "." + value);
            blocks.add(new ChangeableBlock(material, requiredAmount));
            validMaterials.add(material);
        }

        return blocks;
    }

    private HashSet<?> deserializeHashSet(String fileName) {
        File file = new File(this.getDataFolder(), fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        ObjectInputStream input;
        HashSet<?> set;
        try {
            input = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
            Object readObject = input.readObject();
            input.close();
            if(!(readObject instanceof HashSet<?>)) {
                readObject = new HashSet<>();
            }
            set = (HashSet<?>) readObject;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return set;
    }

    private void serializeHashSet(String fileName, HashSet<?> set) {
        File file = new File(this.getDataFolder(), fileName);
        ObjectOutputStream output;
        try {
            output = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
            output.writeObject(set);
            output.flush();
            output.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public ArrayList<ChangeableBiome> getLoadedBiomes() {
        return loadedBiomes;
    }

    public Set<Material> getValidMaterials() {
        return validMaterials;
    }
}
