package fr.breakerland.artefact;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Artefact {

	private final String name;
	private ItemStack icon;

	Artefact(String name) {
		this.name = name;
	}

	protected Artefact(String name, ItemStack icon) {
		this(name);
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public ItemStack getIcon() {
		return icon;
	}

	public abstract void setArtefact(Player player);

	public abstract void unsetArtefact(Player player);
}