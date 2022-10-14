package me.maroon28.realisticbiomes;

import me.maroon28.realisticbiomes.evolvables.EvolvableBiome;
import me.maroon28.realisticbiomes.evolvables.EvolvableChunk;
import me.maroon28.realisticbiomes.evolvables.RequiredBlock;
import me.maroon28.realisticbiomes.commands.MainCommand;
import me.maroon28.realisticbiomes.listeners.BlockListener;
import me.maroon28.realisticbiomes.tasks.ChunkEvolveTask;
import me.maroon28.realisticbiomes.tasks.ChunkStampTask;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Biome;
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
    public static ArrayList<EvolvableBiome> loadedBiomes = new ArrayList<>();

    public static HashSet<Material> validMaterials = new HashSet<>();
    public static HashSet<Chunk> chunksToStamp = new HashSet<>();
    public static HashSet<EvolvableChunk> evolvableChunks = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        fetchChangeableBiomes();
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        runTasks();
        loadSets();
        getCommand("realisticbiomes").setExecutor(new MainCommand(this));
    }

    @Override
    public void onDisable() {
        saveHashSet("changeable-chunks.dat", evolvableChunks);
        saveHashSet("stampable-chunks.dat",  chunksToStamp);
    }

    private void runTasks() {
        var evolveTask = new ChunkEvolveTask(this);
        var stampTask = new ChunkStampTask(this);
        var config = getConfig();
        evolveTask.runTaskTimer(this, 0, config.getInt("tasks.evolve-interval") * 20L);
        stampTask.runTaskTimer(this, 0, config.getInt("tasks.stamp-interval") * 20L);
    }

    private void loadSets() {
        evolvableChunks = loadHashSet("changeable-chunks.dat");
        chunksToStamp = loadHashSet("stampable-chunks.dat");
    }


    public void fetchChangeableBiomes() {
        var config = getConfig();
        Set<String> configuredBiomes = config.getConfigurationSection("biomes.").getKeys(false);
        for (var biomeSection : configuredBiomes) {
            int configuredTime = config.getInt("biomes." + biomeSection + ".time");
            // if the value is 0, default it to 100
            int time = configuredTime == 0 ? 100 : configuredTime;
            var blocks = fetchChangeableBlocks(biomeSection);
            try {
                var biome = Biome.valueOf(biomeSection);
                loadedBiomes.add(new EvolvableBiome(biome, blocks, time));
            } catch (IllegalArgumentException exception) {
                getLogger().warning("Biome " + biomeSection + " does not exist!" + " Is the name right?");
            }
        }

    }

    private ArrayList<RequiredBlock> fetchChangeableBlocks(String biomeSection) {
        var config = getConfig();
        Set<String> biomeValues = config.getConfigurationSection("biomes." + biomeSection).getKeys(false);
        if (biomeValues.isEmpty()) return null;

        ArrayList<RequiredBlock> blocks = new ArrayList<>();
        for (var value : biomeValues) {
            if (value.equals("time")) continue;
            int requiredAmount = config.getInt("biomes." + biomeSection + "." + value);
            try {
                Material material = Material.valueOf(value);
                blocks.add(new RequiredBlock(material, biomeSection, requiredAmount));
                validMaterials.add(material);
            }  catch (IllegalArgumentException exception) {
                getLogger().warning("Material " + value + " for biome " + biomeSection + " does not exist! Is the name right?");
            }
        }

        return blocks;
    }

    private <T> HashSet<T> loadHashSet(String fileName)  {
        File file = new File(this.getDataFolder(), fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // There wasn't a file to read from, so just save time and return an empty set.
            return new HashSet<>();
        }
        HashSet<T> set;
        try {
            FileInputStream fileStream = new FileInputStream(file);
            GZIPInputStream zipStream = new GZIPInputStream(fileStream);
            ObjectInputStream objectStream = new ObjectInputStream(zipStream);
            Object readObject = objectStream.readObject();
            objectStream.close();
            zipStream.close();
            fileStream.close();
            if(!(readObject instanceof HashSet)) {
                readObject = new HashSet<T>();
            }
            set = (HashSet<T>) readObject;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new HashSet<>();
        }
        return set;
    }

    private <T> void saveHashSet(String fileName, HashSet<T> set) {
        File file = new File(this.getDataFolder(), fileName);
        try {
            FileOutputStream fileStream = new FileOutputStream(file);
            GZIPOutputStream zipStream = new GZIPOutputStream(fileStream);
            ObjectOutputStream output = new ObjectOutputStream(zipStream);
            output.writeObject(set);
            output.flush();
            zipStream.finish();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<EvolvableBiome> getLoadedBiomes() {
        return loadedBiomes;
    }

    public Set<Material> getValidMaterials() {
        return validMaterials;
    }
}
