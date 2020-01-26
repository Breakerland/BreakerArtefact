package fr.breakerland.artefact.manager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import fr.breakerland.artefact.Artefact;
import fr.breakerland.artefact.ArtefactPlugin;

public class DataManager implements Listener {
	private ArtefactPlugin plugin;
	private HikariDataSource hikari;
	private static final String INSERT = "INSERT INTO Artefact VALUES(?,?)";
	private static final String SELECT = "SELECT artefact FROM Artefact WHERE uuid=?";
	private static final String SAVE = "UPDATE Artefact SET artefact=? WHERE uuid=?";
	private final Map<UUID, Artefact> playerArtefacts = new HashMap<>();

	public DataManager(ArtefactPlugin plugin) {
		this.plugin = plugin;
		init(plugin.getConfig().getConfigurationSection("database"));
		createTable();

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void createTable() {
		try (Connection connection = hikari.getConnection(); Statement statement = connection.createStatement()) {
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS Artefact(uuid varchar(36), artefact VARCHAR(16))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	void init(ConfigurationSection section) {
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
			File databaseFile = new File(plugin.getDataFolder(), section.getString("sqlite", "playerArtefacts.db"));
			if (!databaseFile.exists())
				try {
					databaseFile.createNewFile();
				} catch (IOException exception) {
					plugin.getLogger().log(Level.SEVERE, "Failed to created SQLite database.  Error: " + exception.getMessage());
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

	public void loadPlayer(final Player player) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, (Runnable) () -> {
			try (Connection connection = hikari.getConnection(); PreparedStatement insert = connection.prepareStatement(INSERT); PreparedStatement select = connection.prepareStatement(SELECT)) {
				insert.setString(1, player.getUniqueId().toString());
				insert.setString(2, null);
				insert.execute();

				select.setString(1, player.getUniqueId().toString());
				ResultSet result = select.executeQuery();
				if (result.next()) {
					Artefact artefact = plugin.getArtefactManager().getArtefact(result.getString("artefact"));
					playerArtefacts.put(player.getUniqueId(), artefact);
					if (artefact != null)
						artefact.setArtefact(player);
				}
				result.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public void savePlayer(final Player player) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, (Runnable) () -> {
			try (Connection connection = hikari.getConnection(); PreparedStatement statement = connection.prepareStatement(SAVE)) {
				Artefact artefact = playerArtefacts.remove(player.getUniqueId());
				statement.setString(1, artefact != null ? artefact.getName() : null);
				statement.setString(2, player.getUniqueId().toString());
				statement.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public Artefact getArtefact(Player player) {
		return playerArtefacts.get(player.getUniqueId());
	}

	public void setArtefact(Player player, Artefact artefact) {
		playerArtefacts.put(player.getUniqueId(), artefact);
	}

	public void clearArtefacts(Player player) {
		Artefact artefact = playerArtefacts.get(player.getUniqueId());
		if (artefact != null)
			artefact.unsetArtefact(player);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		loadPlayer(event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true)
	public void onQuitJoin(PlayerQuitEvent event) {
		savePlayer(event.getPlayer());
		clearArtefacts(event.getPlayer());
	}

	public void close() {
		if (hikari != null)
			hikari.close();
	}
}