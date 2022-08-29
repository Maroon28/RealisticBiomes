package me.maroon28.realisticbiomes.commands;

import me.maroon28.realisticbiomes.RealisticBiomes;
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
                player.sendMessage("Configuration successfully reloaded! Loaded " + size + " biomes!");
            }
            return true;
        }

        return false;
    }
}
