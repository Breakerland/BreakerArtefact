package fr.breakerland.artefact.artefacts;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.breakerland.artefact.Artefact;

public class HealthArtefact extends Artefact {

	public HealthArtefact(String name, ItemStack icon) {
		super(name, icon);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setArtefact(Player player) {
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30);
	}

	@Override
	public void unsetArtefact(Player player) {
		player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
	}
}