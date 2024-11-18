/**
 * @author FauZaPespi
 * @version 1.0
 */
package fr.lunitycraft.fauza.lunitycraft_topluck;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Classe qui gère les événements liés à la casse de blocs et enregistre
 * les statistiques des joueurs dans une base de données.
 */
public class CheatListener implements Listener {

    /** Référence à l'instance principale du plugin */
    private final LunityCraft_TopLuck plugin;

    /**
     * Constructeur pour initialiser le listener avec l'instance du plugin.
     *
     * @param plugin Instance du plugin LunityCraft_TopLuck
     */
    public CheatListener(LunityCraft_TopLuck plugin) {
        this.plugin = plugin;
    }

    /**
     * Gestionnaire d'événement pour la casse de blocs. Met à jour les statistiques
     * des joueurs dans une base de données en fonction du type de bloc cassé.
     *
     * @param event L'événement de casse de bloc.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        Connection connection = plugin.getDatabaseConnection();

        // Vérifie si la connexion à la base de données est établie
        if (connection == null) {
            System.out.println("Failed to establish database connection.");
            return;
        }

        try {
            // Vérifie si le joueur existe déjà dans la base de données
            String checkQuery = "SELECT player_name FROM player_data WHERE player_name = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, player.getName());
                ResultSet resultSet = checkStmt.executeQuery();

                // Si le joueur n'existe pas, insère un nouvel enregistrement
                if (!resultSet.next()) {
                    String insertQuery = "INSERT INTO player_data (player_name, total_blocks, diamond_blocks, gold_blocks, emerald_blocks, common_blocks, rare_to_common_ratio) VALUES (?, 1, 0, 0, 0, 0, 0.0)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, player.getName());
                        insertStmt.executeUpdate();
                    }
                } else {
                    // Sinon, met à jour le nombre total de blocs cassés
                    String updateQuery = "UPDATE player_data SET total_blocks = total_blocks + 1 WHERE player_name = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, player.getName());
                        updateStmt.executeUpdate();
                    }
                }
            }

            // Mise à jour des statistiques spécifiques au type de bloc
            if (blockType == Material.DIAMOND_ORE) {
                incrementBlockCount(connection, player.getName(), "diamond_blocks");
            } else if (blockType == Material.GOLD_ORE) {
                incrementBlockCount(connection, player.getName(), "gold_blocks");
            } else if (blockType == Material.EMERALD_ORE) {
                incrementBlockCount(connection, player.getName(), "emerald_blocks");
            } else if (isCommonBlock(blockType)) {
                incrementBlockCount(connection, player.getName(), "common_blocks");
            }

            // Met à jour le ratio blocs rares / blocs communs
            updateRatio(connection, player.getName());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Incrémente le compteur d'un type de bloc spécifique pour un joueur donné.
     *
     * @param connection Connexion à la base de données.
     * @param playerName Nom du joueur.
     * @param column Colonne de la base de données correspondant au type de bloc.
     * @throws SQLException En cas d'erreur lors de l'exécution de la requête.
     */
    private void incrementBlockCount(Connection connection, String playerName, String column) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE player_data SET " + column + " = " + column + " + 1 WHERE player_name = ?"
        )) {
            statement.setString(1, playerName);
            statement.executeUpdate();
        }
    }

    /**
     * Met à jour le ratio blocs rares / blocs communs pour un joueur donné.
     *
     * @param connection Connexion à la base de données.
     * @param playerName Nom du joueur.
     * @throws SQLException En cas d'erreur lors de l'exécution de la requête.
     */
    private void updateRatio(Connection connection, String playerName) throws SQLException {
        String updateQuery = "UPDATE player_data " +
                "SET rare_to_common_ratio = (diamond_blocks + gold_blocks + emerald_blocks) / (common_blocks + 1) " +
                "WHERE player_name = ?";

        String insertQuery = "INSERT OR IGNORE INTO player_data (player_name, total_blocks, diamond_blocks, gold_blocks, emerald_blocks, common_blocks, rare_to_common_ratio) " +
                "VALUES (?, 1, 0, 0, 0, 0, 0.0)";

        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            insertStmt.setString(1, playerName);
            insertStmt.executeUpdate();
        }

        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
            updateStmt.setString(1, playerName);
            updateStmt.executeUpdate();
        }
    }

    /**
     * Vérifie si un type de bloc est considéré comme commun.
     *
     * @param blockType Type de bloc à vérifier.
     * @return {@code true} si le bloc est commun, {@code false} sinon.
     */
    private boolean isCommonBlock(Material blockType) {
        return blockType == Material.STONE || blockType == Material.COAL_ORE || blockType == Material.REDSTONE_ORE || blockType == Material.IRON_ORE;
    }
}
