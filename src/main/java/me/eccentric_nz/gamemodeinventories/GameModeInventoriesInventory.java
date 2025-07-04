/*
 * Kristian S. Stangeland aadnk
 * Norway
 * kristian@comphenix.net
 * thtp://www.comphenix.net/
 */
package me.eccentric_nz.gamemodeinventories;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class GameModeInventoriesInventory {

    private final GameModeInventories plugin;
    private final boolean saveXP;
    private final boolean saveArmour;
    private final boolean saveEnderChest;
    private final boolean potions;
    GameModeInventoriesXPCalculator xpc;

    public GameModeInventoriesInventory(GameModeInventories plugin) {
        this.plugin = plugin;
        saveXP = this.plugin.getConfig().getBoolean("xp");
        saveArmour = this.plugin.getConfig().getBoolean("armor");
        saveEnderChest = this.plugin.getConfig().getBoolean("enderchest");
        potions = this.plugin.getConfig().getBoolean("remove_potions");
    }

    public void switchInventories(Player player, GameMode newGM) {
        String uuid = player.getUniqueId().toString();
        String name = player.getName();
        String currentGM = player.getGameMode().name();
        if (saveXP) {
            xpc = new GameModeInventoriesXPCalculator(player);
        }
        String inv = GameModeInventoriesBukkitSerialization.toDatabase(
                player.getInventory().getContents());
        try (Connection connection = plugin.getDatabaseConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM " + plugin.getPrefix() + "inventories WHERE uuid = ? AND gamemode = ?"); ) {
            // get their current gamemode inventory from database
            statement.setString(1, uuid);
            statement.setString(2, currentGM);
            try (ResultSet rsInv = statement.executeQuery(); ) {
                int id = 0;
                if (rsInv.next()) {
                    // update it with their current inventory
                    id = rsInv.getInt("id");
                    String updateQuery = "UPDATE " + plugin.getPrefix() + "inventories SET inventory = ? WHERE id = ?";
                    try (PreparedStatement ps = connection.prepareStatement(updateQuery); ) {
                        ps.setString(1, inv);
                        ps.setInt(2, id);
                        ps.executeUpdate();
                    }
                } else {
                    // they haven't got an inventory saved yet so make one with their current inventory
                    String insertQuery = "INSERT INTO " + plugin.getPrefix()
                            + "inventories (uuid, player, gamemode, inventory) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ps =
                            connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS); ) {
                        ps.setString(1, uuid);
                        ps.setString(2, name);
                        ps.setString(3, currentGM);
                        ps.setString(4, inv);
                        ps.executeUpdate();
                        try (ResultSet idRS = ps.getGeneratedKeys(); ) {
                            if (idRS.next()) {
                                id = idRS.getInt(1);
                            }
                        }
                    }
                }

                if (saveXP) {
                    // get players XP
                    int a = xpc.getCurrentExp();
                    String xpQuery = "UPDATE " + plugin.getPrefix() + "inventories SET xp = ? WHERE id = ?";
                    try (PreparedStatement psx = connection.prepareStatement(xpQuery); ) {
                        psx.setInt(1, a);
                        psx.setInt(2, id);
                        psx.executeUpdate();
                    }
                }
                if (saveArmour) {
                    // get players armour
                    String arm = GameModeInventoriesBukkitSerialization.toDatabase(
                            player.getInventory().getArmorContents());
                    String armourQuery = "UPDATE " + plugin.getPrefix() + "inventories SET armour = ? WHERE id = ?";
                    try (PreparedStatement psa = connection.prepareStatement(armourQuery); ) {
                        psa.setString(1, arm);
                        psa.setInt(2, id);
                        psa.executeUpdate();
                    }
                }
                if (saveEnderChest) {
                    // get players enderchest
                    Inventory ec = player.getEnderChest();
                    if (ec != null) {
                        String ender = GameModeInventoriesBukkitSerialization.toDatabase(ec.getContents());
                        String enderQuery =
                                "UPDATE " + plugin.getPrefix() + "inventories SET enderchest = ? WHERE id = ?";
                        try (PreparedStatement pse = connection.prepareStatement(enderQuery); ) {
                            pse.setString(1, ender);
                            pse.setInt(2, id);
                            pse.executeUpdate();
                        }
                    }
                }
                if (potions && currentGM.equals("CREATIVE") && !newGM.equals(GameMode.CREATIVE)) {
                    // remove all potion effects
                    player.getActivePotionEffects().forEach((effect) -> {
                        player.removePotionEffect(effect.getType());
                    });
                }
                // check if they have an inventory for the new gamemode
                try {
                    statement.setString(1, uuid);
                    statement.setString(2, newGM.name());
                    try (ResultSet rsNewInv = statement.executeQuery(); ) {
                        int amount;
                        if (rsNewInv.next()) {
                            // set their inventory to the saved one
                            String savedinventory = rsNewInv.getString("inventory");
                            ItemStack[] stacks;
                            if (savedinventory.startsWith("[")) {
                                stacks = GameModeInventoriesJSONSerialization.toItemStacks(savedinventory);
                            } else {
                                stacks = GameModeInventoriesBukkitSerialization.fromDatabase(savedinventory);
                            }
                            player.getInventory().setContents(stacks);
                            amount = rsNewInv.getInt("xp");
                            if (saveArmour) {
                                String savedarmour = rsNewInv.getString("armour");
                                if (savedarmour != null) {
                                    ItemStack[] a;
                                    if (savedarmour.startsWith("[")) {
                                        a = GameModeInventoriesJSONSerialization.toItemStacks(savedarmour);
                                    } else {
                                        a = GameModeInventoriesBukkitSerialization.fromDatabase(savedarmour);
                                    }
                                    player.getInventory().setArmorContents(a);
                                }
                            }
                            if (saveEnderChest) {
                                String savedender = rsNewInv.getString("enderchest");
                                if (savedender == null
                                        || savedender.equals("[Null]")
                                        || savedender.equals("")
                                        || savedender.isEmpty()) {
                                    // empty inventory
                                    savedender =
                                            "[\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\",\"null\"]";
                                }
                                ItemStack[] e;
                                if (savedender.startsWith("[")) {
                                    e = GameModeInventoriesJSONSerialization.toItemStacks(savedender);
                                } else {
                                    e = GameModeInventoriesBukkitSerialization.fromDatabase(savedender);
                                }
                                Inventory echest = player.getEnderChest();
                                echest.setContents(e);
                            }
                        } else {
                            // start with an empty inventory
                            player.getInventory().clear();
                            if (saveArmour) {
                                player.getInventory().setBoots(null);
                                player.getInventory().setChestplate(null);
                                player.getInventory().setLeggings(null);
                                player.getInventory().setHelmet(null);
                            }
                            if (saveEnderChest) {
                                Inventory echest = player.getEnderChest();
                                echest.clear();
                            }
                            amount = 0;
                        }

                        if (saveXP) {
                            xpc.setExp(amount);
                        }
                        player.updateInventory();
                    }
                } catch (IOException ex) {
                    GameModeInventories.plugin.debug("Could not restore inventory on gamemode change, " + ex);
                }
            }
        } catch (SQLException e) {
            GameModeInventories.plugin.debug("Could not save inventory on gamemode change, " + e);
        }
    }

    public void saveOnDeath(Player p) {
        String uuid = p.getUniqueId().toString();
        String name = p.getName();
        String gm = p.getGameMode().name();
        String inv = GameModeInventoriesBukkitSerialization.toDatabase(
                p.getInventory().getContents());
        String arm = GameModeInventoriesBukkitSerialization.toDatabase(
                p.getInventory().getArmorContents());
        try (Connection connection = plugin.getDatabaseConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT id FROM " + plugin.getPrefix() + "inventories WHERE uuid = ? AND gamemode = ?"); ) {
            // get their current gamemode inventory from database
            statement.setString(1, uuid);
            statement.setString(2, gm);
            try (ResultSet rsInv = statement.executeQuery(); ) {
                if (rsInv.isBeforeFirst() && rsInv.next()) {
                    // update it with their current inventory
                    int id = rsInv.getInt("id");
                    String updateQuery =
                            "UPDATE " + plugin.getPrefix() + "inventories SET inventory = ?, armour = ? WHERE id = ?";
                    try (PreparedStatement ps = connection.prepareStatement(updateQuery); ) {
                        ps.setString(1, inv);
                        ps.setString(2, arm);
                        ps.setInt(3, id);
                        ps.executeUpdate();
                    }
                } else {
                    // they haven't got an inventory saved yet so make one with their current inventory
                    String invQuery = "INSERT INTO " + plugin.getPrefix()
                            + "inventories (uuid, player, gamemode, inventory, armour) VALUES (?, ?, ?, ?, ?,)";
                    try (PreparedStatement ps = connection.prepareStatement(invQuery); ) {
                        ps.setString(1, uuid);
                        ps.setString(2, name);
                        ps.setString(3, gm);
                        ps.setString(4, inv);
                        ps.setString(5, arm);
                        ps.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            GameModeInventories.plugin.debug("Could not save inventories on player death, " + e);
        }
    }

    public void restoreOnSpawn(Player p) {
        String uuid = p.getUniqueId().toString();
        String gm = p.getGameMode().name();
        // restore their inventory
        try (Connection connection = plugin.getDatabaseConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM " + plugin.getPrefix() + "inventories WHERE uuid = ? AND gamemode = ?"); ) {
            // get their current gamemode inventory from database
            statement.setString(1, uuid);
            statement.setString(2, gm);
            try (ResultSet rsInv = statement.executeQuery(); ) {
                if (rsInv.next()) {
                    try {
                        // set their inventory to the saved one
                        String savedinventory = rsInv.getString("inventory");
                        ItemStack[] i;
                        if (savedinventory.startsWith("[")) {
                            i = GameModeInventoriesJSONSerialization.toItemStacks(savedinventory);
                        } else {
                            i = GameModeInventoriesBukkitSerialization.fromDatabase(savedinventory);
                        }
                        p.getInventory().setContents(i);
                        String savedarmour = rsInv.getString("armour");
                        ItemStack[] a;
                        if (savedarmour.startsWith("[")) {
                            a = GameModeInventoriesJSONSerialization.toItemStacks(savedarmour);
                        } else {
                            a = GameModeInventoriesBukkitSerialization.fromDatabase(savedarmour);
                        }
                        p.getInventory().setArmorContents(a);
                    } catch (IOException e) {
                        GameModeInventories.plugin.debug("Could not restore inventories on respawn, " + e);
                    }
                }
            }
        } catch (SQLException e) {
            GameModeInventories.plugin.debug("Could not restore inventories on respawn, " + e);
        }
    }

    public boolean isInstanceOf(Entity e) {
        return e instanceof PoweredMinecart
                || e instanceof StorageMinecart
                || e instanceof HopperMinecart
                || e instanceof ItemFrame
                || e instanceof ArmorStand;
    }

    public boolean isInstanceOf(InventoryHolder h) {
        return (h instanceof AbstractHorse);
    }
}
