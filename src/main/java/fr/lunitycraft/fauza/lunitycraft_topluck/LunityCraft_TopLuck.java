/**
 * @author FauZaPespi
 * @version 1.0
 */
package fr.lunitycraft.fauza.lunitycraft_topluck;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;

/**
 * La classe principale qui gère le cycle de vie du plugin (activation/désactivation) et initialise la base de données.
 */
public final class LunityCraft_TopLuck extends JavaPlugin implements Listener {

    /** Connexion à la base de données SQLite */
    private Connection connection;

    /**
     * Méthode appelée lors de l'activation du plugin.
     * Initialise les commandes, enregistre les listeners, et configure la base de données.
     */
    @Override
    public void onEnable() {

        // Enregistre la commande "topluck"
        this.getCommand("topluck").setExecutor(new TopLuckCommand(this));

        // Enregistre les listeners pour les interactions avec l'inventaire
        getServer().getPluginManager().registerEvents(new InventoryClickListener("Top Luck"), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener("Options for "), this);  // Listener pour les options

        // Chargement du driver SQLite et création des tables
        try {
            Class.forName("org.sqlite.JDBC");
            createDatabaseAndTable();
            System.out.println("[TopLuck] DB or/and tables created");
        } catch (ClassNotFoundException e) {
            System.out.println("[TopLuck] SQLite JDBC driver not found.");
            return;
        }

        // Enregistre le listener pour gérer les statistiques de casse de blocs
        getServer().getPluginManager().registerEvents(new CheatListener(this), this);
    }

    /**
     * Méthode appelée lors de la désactivation du plugin.
     * Ferme la connexion à la base de données si elle est ouverte.
     */
    @Override
    public void onDisable() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Crée la base de données et la table "player_data" si elles n'existent pas.
     */
    private void createDatabaseAndTable() {
        try {
            // Création du dossier de données s'il n'existe pas
            File dataFolder = getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            // Création du fichier de base de données s'il n'existe pas
            File dbFile = new File(dataFolder, "data.db");
            if (!dbFile.exists()) {
                dbFile.getParentFile().mkdirs();
                dbFile.createNewFile();
            }

            // Initialisation de la connexion SQLite
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());

            // Création de la table "player_data" si elle n'existe pas
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS player_data (" +
                                "player_name TEXT PRIMARY KEY, " +
                                "total_blocks INTEGER DEFAULT 0, " +
                                "diamond_blocks INTEGER DEFAULT 0, " +
                                "gold_blocks INTEGER DEFAULT 0, " +
                                "emerald_blocks INTEGER DEFAULT 0, " +
                                "common_blocks INTEGER DEFAULT 0, " +
                                "rare_to_common_ratio REAL DEFAULT 0.0" +
                                ");"
                );
            }
            System.out.println("[TopLuck] Database and table initialized successfully.");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retourne la connexion à la base de données.
     * Si la connexion n'existe pas ou a été fermée, elle est recréée.
     *
     * @return Connexion à la base de données SQLite.
     */
    public Connection getDatabaseConnection() {
        if (connection == null) {
            createDatabaseAndTable();
        }
        return connection;
    }
}
