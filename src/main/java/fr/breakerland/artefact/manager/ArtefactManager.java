package fr.breakerland.artefact.manager;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.breakerland.artefact.Artefact;
import fr.breakerland.artefact.ArtefactPlugin;
import fr.breakerland.artefact.artefacts.HealthArtefact;
import fr.breakerland.artefact.artefacts.MagnetArtefact;
import fr.breakerland.artefact.artefacts.NoFallArtefact;
import fr.breakerland.artefact.artefacts.SpeedArtefact;

public class ArtefactManager {
	private final Set<Artefact> artefacts = new HashSet<>();

	public static HealthArtefact HEALTH_ARTEFACT;
	public static SpeedArtefact SPEED_ARTEFACT;
	public static NoFallArtefact NO_FALL_ARTEFACT;
	public static MagnetArtefact MAGNET_ARTEFACT;

	public ArtefactManager(ArtefactPlugin plugin) {
		artefacts.add(HEALTH_ARTEFACT = new HealthArtefact("health", new ItemStack(Material.NETHER_STAR)));
		artefacts.add(SPEED_ARTEFACT = new SpeedArtefact("speed", new ItemStack(Material.DIAMOND_SWORD)));
		artefacts.add(NO_FALL_ARTEFACT = new NoFallArtefact(plugin, "noFall", new ItemStack(Material.GOLDEN_SWORD)));
		artefacts.add(MAGNET_ARTEFACT = new MagnetArtefact(plugin, "magnet", new ItemStack(Material.IRON_SWORD)));
	}

	public Set<Artefact> getArtefacts() {
		return artefacts;
	}

	public Artefact getArtefact(String name) {
		if (name != null && !name.isEmpty())
			for (Artefact artefact : getArtefacts())
				if (name.equals(artefact.getName()))
					return artefact;

		return null;
	}

	public Artefact getArtefact(ItemStack item) {
		if (item != null)
			for (Artefact artefact : getArtefacts())
				if (item.equals(artefact.getIcon()))
					return artefact;

		return null;
	}
}
