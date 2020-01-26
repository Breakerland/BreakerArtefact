package fr.breakerland.artefact.artefacts;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import fr.breakerland.artefact.Artefact;
import fr.breakerland.artefact.ArtefactPlugin;

public class NoFallArtefact extends Artefact implements Listener {
	private final Set<UUID> players = new HashSet<>();

	public NoFallArtefact(ArtefactPlugin plugin, String name, ItemStack icon) {
		super(name, icon);

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void setArtefact(Player player) {
		players.add(player.getUniqueId());

	}

	@Override
	public void unsetArtefact(Player player) {
		players.remove(player.getUniqueId());
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageEvent(final EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FALL && players.contains(event.getEntity().getUniqueId()))
			event.setCancelled(true);
	}
}