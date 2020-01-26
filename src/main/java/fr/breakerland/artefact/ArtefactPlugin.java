package fr.breakerland.artefact;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import fr.breakerland.artefact.artefacts.HealthArtefact;
import fr.breakerland.artefact.artefacts.NoFallArtefact;
import fr.breakerland.artefact.artefacts.SpeedArtefact;

public class ArtefactPlugin extends JavaPlugin implements CommandExecutor, Listener {
	private final Map<UUID, Artefact> playerArtefacts = new HashMap<>();

	private final Set<Artefact> artefacts = new HashSet<>();
	public static HealthArtefact HEALTH_ARTEFACT;
	public static SpeedArtefact SPEED_ARTEFACT;
	public static NoFallArtefact NO_FALL_ARTEFACT;

	public FileConfiguration data;
	public File f = new File("");

	@Override
	public void onEnable() {
		getCommand("artefact").setExecutor(this);
		getServer().getPluginManager().registerEvents(this, this);
		initArtefact();
	}

	void initArtefact() {
		artefacts.add(HEALTH_ARTEFACT = new HealthArtefact("health", new ItemStack(Material.NETHER_STAR)));
		artefacts.add(SPEED_ARTEFACT = new SpeedArtefact("speed", new ItemStack(Material.DIAMOND_SWORD)));
		artefacts.add(NO_FALL_ARTEFACT = new NoFallArtefact(this, "noFall", new ItemStack(Material.GOLDEN_SWORD)));
	}

	public Set<Artefact> getArtefacts() {
		return artefacts;
	}

	public Artefact getArtefact(ItemStack item) {
		if (item != null)
			for (Artefact artefact : getArtefacts())
				if (item.equals(artefact.getIcon()))
					return artefact;

		return null;
	}

	private Set<UUID> playerInGUI = new HashSet<>();

	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			playerInGUI.add(player.getUniqueId());
			openInventory(player);
		}

		return true;
	}

	public void openInventory(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 9, ChatColor.translateAlternateColorCodes('&', "&9Artefact"));
		int size = inventory.getSize();
		int i = 0;
		while (i < size)
			inventory.setItem(i++, new ItemStack(Material.WHITE_STAINED_GLASS_PANE));

		Artefact playerArtefact = playerArtefacts.get(player.getUniqueId());
		inventory.setItem(4, playerArtefact != null ? playerArtefact.getIcon() : new ItemStack(Material.AIR));
		player.openInventory(inventory);
		playerInGUI.add(player.getUniqueId());
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		if (playerInGUI.remove(player.getUniqueId())) {
			Artefact oldArtefact = playerArtefacts.get(player.getUniqueId());
			Artefact artefact = getArtefact(event.getInventory().getItem(4));
			if (oldArtefact != null) {
				if (artefact != oldArtefact) {
					oldArtefact.unsetArtefact(player);
					if (artefact != null)
						artefact.setArtefact(player);
				}
			} else if (artefact != null)
				artefact.setArtefact(player);

			playerArtefacts.put(player.getUniqueId(), artefact);
		}
	}

	public void clearArtefacts(Player player) {
		Artefact artefact = playerArtefacts.get(player.getUniqueId());
		if (artefact != null)
			artefact.unsetArtefact(player);
	}

	@EventHandler(ignoreCancelled = true)
	public void onClickInventory(InventoryClickEvent event) {
		if (!playerInGUI.contains(event.getWhoClicked().getUniqueId()))
			return;

		if (getArtefact(event.getCurrentItem()) == null && getArtefact(event.getCursor()) == null)
			event.setCancelled(true);
	}
}