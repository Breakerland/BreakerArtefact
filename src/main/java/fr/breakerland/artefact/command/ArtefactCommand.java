package fr.breakerland.artefact.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.breakerland.artefact.ArtefactPlugin;

public class ArtefactCommand implements CommandExecutor {
	ArtefactPlugin plugin;

	public ArtefactCommand(ArtefactPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		if (sender instanceof Player)
			plugin.getInventoryManager().openInventory((Player) sender);

		return true;
	}
}