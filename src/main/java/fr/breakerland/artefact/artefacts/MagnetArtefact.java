package fr.breakerland.artefact.artefacts;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import fr.breakerland.artefact.Artefact;
import fr.breakerland.artefact.ArtefactPlugin;

public class MagnetArtefact extends Artefact implements Listener {
	private final Set<Player> players = new HashSet<>();

	public MagnetArtefact(ArtefactPlugin plugin, String name, ItemStack icon) {
		super(name, icon);

		plugin.getServer().getScheduler().runTaskTimer(plugin, (Runnable) () -> {
			players.forEach((player) -> player.getNearbyEntities(5, 5, 5).stream().filter((e) -> e instanceof Item).forEach((e) -> e.teleport(player)));
		}, 20 * 5, 20);
	}

	@Override
	public void setArtefact(Player player) {
		players.add(player);

	}

	@Override
	public void unsetArtefact(Player player) {
		players.remove(player);
	}
}