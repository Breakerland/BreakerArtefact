package fr.breakerland.artefact;

import org.bukkit.plugin.java.JavaPlugin;

import fr.breakerland.artefact.command.ArtefactCommand;
import fr.breakerland.artefact.manager.ArtefactManager;
import fr.breakerland.artefact.manager.DataManager;
import fr.breakerland.artefact.manager.InventoryManager;

public class ArtefactPlugin extends JavaPlugin {

	private DataManager dataManager;
	private ArtefactManager artefactManager;
	private InventoryManager inventoryManager;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		dataManager = new DataManager(this);
		artefactManager = new ArtefactManager(this);
		inventoryManager = new InventoryManager(this);
		getCommand("artefact").setExecutor(new ArtefactCommand(this));
	}

	@Override
	public void onDisable() {
		dataManager.close();
	}

	public DataManager getDataManager() {
		return dataManager;
	}

	public ArtefactManager getArtefactManager() {
		return artefactManager;
	}

	public InventoryManager getInventoryManager() {
		return inventoryManager;
	}
}