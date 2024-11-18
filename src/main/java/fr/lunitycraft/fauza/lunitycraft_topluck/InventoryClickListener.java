/**
 * @author FauZaPespi
 * @version 1.0
 */
package fr.lunitycraft.fauza.lunitycraft_topluck;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    /**
     * Stocke les joueurs sélectionnés par leurs UUID.
     */
    private final Map<UUID, String> selectedPlayers = new HashMap<>();

    /**
     * Titre de l'interface graphique associée à cet événement.
     */
    private final String guiTitle;

    /**
     * Constructeur de {@code InventoryClickListener}.
     *
     * @param guiTitle Le titre de l'interface graphique que ce listener gère.
     */
    public InventoryClickListener(String guiTitle) {
        this.guiTitle = guiTitle;
    }

    /**
     * Gère l'événement lorsqu'un joueur clique sur un élément d'inventaire.
     * Si le joueur clique sur un inventaire correspondant au titre configuré, l'action est annulée
     * et des options spécifiques sont affichées en fonction de l'item cliqué.
     *
     * @param event L'événement de clic dans l'inventaire.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        // Vérifie si l'inventaire est celui attendu.
        if (inventory.getName().equals(guiTitle)) {
            event.setCancelled(true); // Empêche la prise d'objets.

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta()) {
                ItemMeta meta = clickedItem.getItemMeta();

                // Vérifie si l'item cliqué est une tête de joueur.
                if (meta instanceof SkullMeta) {
                    SkullMeta skullMeta = (SkullMeta) meta;
                    String playerName = skullMeta.getOwner();

                    // Si un joueur est associé, ajoutez-le à la sélection et affichez les options.
                    if (playerName != null && !playerName.isEmpty()) {
                        selectedPlayers.put(event.getWhoClicked().getUniqueId(), playerName);
                        event.getWhoClicked().closeInventory();
                        showPlayerOptions((Player) event.getWhoClicked(), playerName);
                    }
                }
            }
        }
    }

    /**
     * Affiche une interface graphique d'options pour le joueur sélectionné.
     *
     * @param player               Le joueur qui a effectué la sélection.
     * @param selectedPlayerName   Le nom du joueur sélectionné.
     */
    private void showPlayerOptions(Player player, String selectedPlayerName) {
        Inventory gui = player.getServer().createInventory(null, 27, "Options for " + selectedPlayerName);

        // Remplissage de fond : panneaux de verre cyan.
        ItemStack cyanGlassPane = new ItemStack(Material.STAINED_GLASS_PANE);
        ItemMeta glassMeta = cyanGlassPane.getItemMeta();
        if (glassMeta != null) glassMeta.setDisplayName(" "); // Nom vide pour l'esthétique.
        cyanGlassPane.setItemMeta(glassMeta);
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, cyanGlassPane);
        }

        // Boutons d'action.
        gui.setItem(10, createActionButton(Material.DIAMOND_BLOCK, "Freeze Player", "Freezes the selected player."));
        gui.setItem(12, createActionButton(Material.GOLD_BLOCK, "View Inventory", "Allows you to view the player's inventory."));
        gui.setItem(14, createActionButton(Material.EYE_OF_ENDER, "Teleport to Player", "Teleports you to the selected player."));

        // Ouvre le menu pour le joueur.
        player.openInventory(gui);
    }

    /**
     * Crée un bouton d'action avec une apparence et un comportement spécifique.
     *
     * @param material  Le matériau de l'item.
     * @param name      Le nom à afficher pour cet item.
     * @param loreText  Le texte de description (lore) associé.
     * @return Un {@link ItemStack} configuré.
     */
    private ItemStack createActionButton(Material material, String name, String loreText) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(" ", loreText));
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Récupère le joueur sélectionné associé à un joueur spécifique.
     *
     * @param player Le joueur pour lequel récupérer la sélection.
     * @return Le nom du joueur sélectionné ou {@code null} si aucun joueur n'est sélectionné.
     */
    public String getSelectedPlayer(Player player) {
        return selectedPlayers.get(player.getUniqueId());
    }

    /**
     * Gère les clics sur le menu d'options d'un joueur sélectionné.
     *
     * @param event L'événement de clic dans l'inventaire.
     */
    @EventHandler
    public void onOptionClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getName().startsWith("Options for ")) {
            event.setCancelled(true); // Empêche la prise d'objets.

            Player player = (Player) event.getWhoClicked();
            String selectedPlayerName = getSelectedPlayer(player);
            if (selectedPlayerName == null) return;

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.hasItemMeta()) {
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String displayName = meta.getDisplayName();

                    // Exécute une commande en fonction du bouton cliqué.
                    if (displayName.equals("Freeze Player")) {
                        player.getServer().dispatchCommand(player, "freeze " + selectedPlayerName);
                    } else if (displayName.equals("View Inventory")) {
                        player.getServer().dispatchCommand(player, "invsee " + selectedPlayerName);
                    } else if (displayName.equals("Teleport to Player")) {
                        player.getServer().dispatchCommand(player, "tp " + selectedPlayerName);
                    }
                }
            }
        }
    }
}
