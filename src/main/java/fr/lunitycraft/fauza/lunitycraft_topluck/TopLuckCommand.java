/**
 * @author FauZaPespi
 * @version 1.0
 */
package fr.lunitycraft.fauza.lunitycraft_topluck;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

/**
 * Classe représentant la commande /topluck.
 * Permet d'ouvrir une interface graphique affichant des informations sur un joueur sélectionné,
 * ainsi que des actions comme le gel, la visualisation d'inventaire ou la téléportation.
 */
public class TopLuckCommand implements CommandExecutor {

    private final LunityCraft_TopLuck plugin;

    /**
     * Constructeur pour initialiser la commande avec une référence au plugin principal.
     *
     * @param plugin le plugin principal de l'application
     */
    public TopLuckCommand(LunityCraft_TopLuck plugin) {
        this.plugin = plugin;
    }

    /**
     * Méthode appelée lorsque la commande /topluck est exécutée.
     *
     * @param sender  l'expéditeur de la commande (joueur ou console)
     * @param command l'objet Command associé
     * @param label   l'alias utilisé pour la commande
     * @param args    les arguments passés à la commande
     * @return true si la commande a été exécutée avec succès, false sinon
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Vérifie que l'expéditeur est un joueur
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        Player player = (Player) sender;

        // Vérifie que le joueur possède la permission nécessaire
        if (!player.hasPermission("LunityCraft.topluck")) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        // Vérifie le nombre d'arguments
        if (args.length > 1) {
            player.sendMessage(ChatColor.RED + "Usage : /topluck <playerName>");
            return false;
        }

        if (args.length == 1) {
            // Recherche du joueur cible
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "[TopLuck] Joueur introuvable.");
                return true;
            }

            // Création de l'inventaire de l'interface graphique
            Inventory gui = Bukkit.createInventory(null, 27, "Top Luck");

            // Remplit l'interface avec des panneaux de verre cyan en arrière-plan
            ItemStack cyanGlassPane = new ItemStack(Material.STAINED_GLASS_PANE);
            ItemMeta glassMeta = cyanGlassPane.getItemMeta();
            if (glassMeta != null) glassMeta.setDisplayName(" ");
            cyanGlassPane.setItemMeta(glassMeta);
            for (int i = 0; i < gui.getSize(); i++) {
                gui.setItem(i, cyanGlassPane);
            }

            // Ajoute la tête du joueur avec des statistiques
            ItemStack playerHead = createPlayerHead(target);
            gui.setItem(11, playerHead);

            // Ajoute les boutons d'actions
            gui.setItem(10, createActionButton(Material.DIAMOND_BLOCK, "Geler le joueur", "Gèle le joueur sélectionné."));
            gui.setItem(12, createActionButton(Material.GOLD_BLOCK, "Voir l'inventaire", "Ouvre l'inventaire du joueur."));
            gui.setItem(14, createActionButton(Material.EYE_OF_ENDER, "Se téléporter", "Vous téléporte au joueur sélectionné."));

            // Ouvre l'inventaire pour le joueur
            player.openInventory(gui);
            return true;
        }

        return false;
    }

    /**
     * Crée une tête de joueur avec des informations spécifiques.
     *
     * @param target le joueur pour lequel la tête est créée
     * @return un ItemStack représentant la tête du joueur
     */
    private ItemStack createPlayerHead(Player target) {
        ItemStack playerHead = new ItemStack(Material.SKULL_ITEM, 1, (short) 3); // Pour les versions <1.13
        SkullMeta headMeta = (SkullMeta) playerHead.getItemMeta();

        if (headMeta != null) {
            headMeta.setDisplayName(ChatColor.GREEN + "Joueur : " + target.getName());
            headMeta.setOwner(target.getName());
            headMeta.setLore(Arrays.asList(
                    ChatColor.LIGHT_PURPLE + "Ratio : " + "0", // Exemple de statistiques
                    ChatColor.AQUA + "Total miné : " + "0",
                    ChatColor.DARK_AQUA + "Blocs de diamant : " + "0",
                    ChatColor.GOLD + "Blocs d'or : " + "0",
                    ChatColor.GREEN + "Blocs d'émeraude : " + "0"
            ));
        }
        playerHead.setItemMeta(headMeta);
        return playerHead;
    }

    /**
     * Crée un bouton d'action avec un nom et une description.
     *
     * @param material le matériau de l'élément
     * @param name     le nom du bouton
     * @param loreText la description du bouton
     * @return un ItemStack représentant le bouton
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
}
