package fr.breakerland.artefact.artefacts;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.breakerland.artefact.Artefact;

public class SpeedArtefact extends Artefact {

	public SpeedArtefact(String name, ItemStack icon) {
		super(name, icon);
	}

	@Override
	public void setArtefact(Player player) {
		player.setWalkSpeed(1.0f);

	}

	@Override
	public void unsetArtefact(Player player) {
		player.setWalkSpeed(0.2f);
	}

}