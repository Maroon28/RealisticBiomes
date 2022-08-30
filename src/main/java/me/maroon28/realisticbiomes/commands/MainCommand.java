package me.maroon28.realisticbiomes.commands;

import me.maroon28.realisticbiomes.RealisticBiomes;
import me.maroon28.realisticbiomes.tasks.ChunkEvolveTask;
import me.maroon28.realisticbiomes.tasks.ChunkStampTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class MainCommand implements CommandExecutor {
    private final RealisticBiomes realisticBiomes;

    public MainCommand(RealisticBiomes realisticBiomes) {
        this.realisticBiomes = realisticBiomes;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args[0].equals("reload") && sender.hasPermission("realisticbiomes.reload")) {
            // Reload the configuration and save it.
            realisticBiomes.reloadConfig();
            // Clear any currently loaded biomes
            RealisticBiomes.loadedBiomes.clear();
            // Refetch the biomes from the now reloaded config.
            realisticBiomes.fetchChangeableBiomes();
            int size = RealisticBiomes.loadedBiomes.size();
            realisticBiomes.getLogger().log(Level.INFO, "Successfully reloaded and loaded " + size + " biomes!");
            if (sender instanceof Player player) {
                player.sendMessage(Component.text("Configuration reloaded! ").color(NamedTextColor.GREEN)
                        .append(Component.text( size + " biomes successfully loaded from config!").color(NamedTextColor.YELLOW)));            }
            return true;
        }

        if (args[0].equals("force") && sender.hasPermission("realisticbiomes.force")) {
            // Stamp all the chunks changed recently
            var stampTask = new ChunkStampTask(realisticBiomes);
            stampTask.runTask(realisticBiomes);

            // Force run the evolveTask now that chunks were stamped
            var evolveTask = new ChunkEvolveTask(realisticBiomes);
            // Grab the size now before running to use later
            int size = RealisticBiomes.evolvableChunks.size();
            evolveTask.runTask(realisticBiomes);
            if (sender instanceof Player player) {
                player.sendMessage(Component.text("Force evolved all queued chunks! ").color(NamedTextColor.GREEN)
                        .append(Component.text( size + " chunks evolved!").color(NamedTextColor.YELLOW)));
            }
            return true;
        }

        return false;
    }
}
