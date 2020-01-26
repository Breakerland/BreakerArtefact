package fr.breakerland.artefact;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import fr.breakerland.artefact.artefacts.HealthArtefact;
import fr.breakerland.artefact.artefacts.NoFallArtefact;
import fr.breakerland.artefact.artefacts.SpeedArtefact;

public class ArtefactPlugin extends JavaPlugin implements CommandExecutor, Listener {
	private HikariDataSource hikari;
	private static final String INSERT = "INSERT INTO Artefact VALUES(?,?)";
	private static final String SELECT = "SELECT artefact FROM Artefact WHERE uuid=?";
	private static final String SAVE = "UPDATE Artefact SET artefact=? WHERE uuid=?";
	private final Map<UUID, Artefact> playerArtefacts = new HashMap<>();

	private final Set<Artefact> artefacts = new HashSet<>();
	public static HealthArtefact HEALTH_ARTEFACT;
	public static SpeedArtefact SPEED_ARTEFACT;
	public static NoFallArtefact NO_FALL_ARTEFACT;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		initDB(getConfig().getConfigurationSection("database"));
		initArtefact();
		createTable();
		getCommand("artefact").setExecutor(this);
		getServer().getPluginManager().registerEvents(this, this);
	}

	private void createTable() {
		try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Artefact(uuid varchar(36), artefact VARCHAR(16))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void loadPlayer(final Player player) {
		Bukkit.getScheduler().runTaskAsynchronously(this, (Runnable) () -> {
			try (Connection connection = hikari.getConnection(); PreparedStatement insert = connection.prepareStatement(INSERT); PreparedStatement select = connection.prepareStatement(SELECT)) {
				insert.setString(1, player.getUniqueId().toString());
				insert.setString(2, null);
				insert.execute();

				select.setString(1, player.getUniqueId().toString());
				ResultSet result = select.executeQuery();
				if (result.next())
					playerArtefacts.put(player.getUniqueId(), getArtefact(result.getString("artefact")));
				result.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	private void savePlayer(final Player player) {
		Bukkit.getScheduler().runTaskAsynchronously(this, (Runnable) () -> {
			try (Connection connection = hikari.getConnection(); PreparedStatement statement = connection.prepareStatement(SAVE)) {
				Artefact artefact = playerArtefacts.get(player.getUniqueId());
				statement.setString(1, artefact != null ? artefact.getName() : null);
				statement.setString(2, player.getUniqueId().toString());
				statement.execute();
				playerArtefacts.remove(player.getUniqueId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void onDisable() {
		if (hikari != null)
			hikari.close();
	}

	void initDB(ConfigurationSection section) {
		HikariConfig config = new HikariConfig();
		if (section.getString("backed", "sqlite").equalsIgnoreCase("mysql")) {
			config.setJdbcUrl("jdbc:mysql://" + section.getString("host", "localhost") + ":" + section.getString("port", "3306") + "/" + section.getString("database", "artefact"));
			config.setUsername(section.getString("user", "user"));
			config.setPassword(section.getString("password", "password"));
			config.setDriverClassName("com.mysql.jdbc.Driver");
			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("prepStmtCacheSize", "250");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		} else {
			File databaseFile = new File(getDataFolder(), section.getString("sqlite", "playerArtefacts.db"));
			if (!databaseFile.exists())
				try {
					databaseFile.createNewFile();
				} catch (IOException exception) {
					getLogger().log(Level.SEVERE, "Failed to created SQLite database.  Error: " + exception.getMessage());
				}

			config.setPoolName("ArtefactsSQLitePool");
			config.setDriverClassName("org.sqlite.JDBC");
			config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
			config.setConnectionTestQuery("SELECT 1");
			config.setMaxLifetime(60000); // 60 Sec
			config.setIdleTimeout(45000); // 45 Sec
			config.setMaximumPoolSize(50); // 50 Connections (including idle connections)
		}

		hikari = new HikariDataSource(config);
	}

	void initArtefact() {
		artefacts.add(HEALTH_ARTEFACT = new HealthArtefact("health", new ItemStack(Material.NETHER_STAR)));
		artefacts.add(SPEED_ARTEFACT = new SpeedArtefact("speed", new ItemStack(Material.DIAMOND_SWORD)));
		artefacts.add(NO_FALL_ARTEFACT = new NoFallArtefact(this, "noFall", new ItemStack(Material.GOLDEN_SWORD)));
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

	private void openInventory(Player player) {
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
	public void onPlayerJoin(PlayerJoinEvent event) {
		loadPlayer(event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true)
	public void onQuitJoin(PlayerQuitEvent event) {
		savePlayer(event.getPlayer());
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