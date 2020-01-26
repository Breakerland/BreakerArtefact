package fr.breakerland.artefact.manager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.breakerland.artefact.Artefact;
import fr.breakerland.artefact.ArtefactPlugin;

public class InventoryManager implements Listener {
	ArtefactPlugin plugin;

	private Set<UUID> playerInGUI = new HashSet<>();

	public InventoryManager(ArtefactPlugin plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void openInventory(Player player) {
		playerInGUI.add(player.getUniqueId());

		Inventory inventory = Bukkit.createInventory(player, 9, ChatColor.translateAlternateColorCodes('&', "&9Artefact"));
		int size = inventory.getSize();
		int i = 0;
		while (i < size)
			inventory.setItem(i++, new ItemStack(Material.WHITE_STAINED_GLASS_PANE));

		Artefact playerArtefact = plugin.getDataManager().getArtefact(player);
		inventory.setItem(4, playerArtefact != null ? playerArtefact.getIcon() : new ItemStack(Material.AIR));
		player.openInventory(inventory);
		playerInGUI.add(player.getUniqueId());
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		if (playerInGUI.remove(player.getUniqueId())) {
			Artefact oldArtefact = plugin.getDataManager().getArtefact(player);
			Artefact artefact = plugin.getArtefactManager().getArtefact(event.getInventory().getItem(4));
			if (oldArtefact != null) {
				if (artefact != oldArtefact) {
					oldArtefact.unsetArtefact(player);
					if (artefact != null)
						artefact.setArtefact(player);
				}
			} else if (artefact != null)
				artefact.setArtefact(player);

			plugin.getDataManager().setArtefact(player, artefact);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onClickInventory(InventoryClickEvent event) {
		if (!playerInGUI.contains(event.getWhoClicked().getUniqueId()))
			return;

		if (plugin.getArtefactManager().getArtefact(event.getCurrentItem()) == null && plugin.getArtefactManager().getArtefact(event.getCursor()) == null)
			event.setCancelled(true);
	}
}