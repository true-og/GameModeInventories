/*
 *  Copyright 2014 eccentric_nz.
 */
package me.eccentric_nz.gamemodeinventories.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import me.eccentric_nz.gamemodeinventories.GameModeInventories;

/**
 * @author eccentric_nz
 */
public class GameModeInventoriesSQLite {

    private final GameModeInventories plugin;

    public GameModeInventoriesSQLite(GameModeInventories plugin) {
        this.plugin = plugin;
    }

    public void createTables() {
        try (Connection connection = plugin.getDatabaseConnection();
                Statement statement = connection.createStatement(); ) {
            String queryInventories = "CREATE TABLE IF NOT EXISTS " + plugin.getPrefix()
                    + "inventories (id INTEGER PRIMARY KEY NOT NULL, uuid TEXT, player TEXT, gamemode TEXT, inventory TEXT, xp REAL, armour TEXT, enderchest TEXT, attributes TEXT, armour_attributes TEXT)";
            statement.executeUpdate(queryInventories);
            // update inventories if there is no uuid column
            String queryUUID = "SELECT sql FROM sqlite_master WHERE tbl_name = '" + plugin.getPrefix()
                    + "inventories' AND sql LIKE '%uuid TEXT%'";
            try (ResultSet rsUUID = statement.executeQuery(queryUUID); ) {
                if (!rsUUID.next()) {
                    String queryAlterU = "ALTER TABLE " + plugin.getPrefix() + "inventories ADD uuid TEXT";
                    statement.executeUpdate(queryAlterU);
                    plugin.getLogger().log(Level.INFO, "[GameModeInventories] Adding UUID to database!");
                }
            }
            // update inventories if there is no xp column
            String queryXP = "SELECT sql FROM sqlite_master WHERE tbl_name = '" + plugin.getPrefix()
                    + "inventories' AND sql LIKE '%xp REAL%'";
            try (ResultSet rsXP = statement.executeQuery(queryXP); ) {
                if (!rsXP.next()) {
                    String queryAlter = "ALTER TABLE " + plugin.getPrefix() + "inventories ADD xp REAL";
                    statement.executeUpdate(queryAlter);
                    plugin.getLogger().log(Level.INFO, "[GameModeInventories] Adding xp to database!");
                }
            }
            // update inventories if there is no armour column
            String queryArmour = "SELECT sql FROM sqlite_master WHERE tbl_name = '" + plugin.getPrefix()
                    + "inventories' AND sql LIKE '%armour TEXT%'";
            try (ResultSet rsArmour = statement.executeQuery(queryArmour); ) {
                if (!rsArmour.next()) {
                    String queryAlter2 = "ALTER TABLE " + plugin.getPrefix() + "inventories ADD armour TEXT";
                    statement.executeUpdate(queryAlter2);
                    plugin.getLogger().log(Level.INFO, "[GameModeInventories] Adding armour to database!");
                }
            }
            // update inventories if there is no enderchest column
            String queryEnder = "SELECT sql FROM sqlite_master WHERE tbl_name = '" + plugin.getPrefix()
                    + "inventories' AND sql LIKE '%enderchest TEXT%'";
            try (ResultSet rsEnder = statement.executeQuery(queryEnder); ) {
                if (!rsEnder.next()) {
                    String queryAlter3 = "ALTER TABLE " + plugin.getPrefix() + "inventories ADD enderchest TEXT";
                    statement.executeUpdate(queryAlter3);
                    plugin.getLogger().log(Level.INFO, "[GameModeInventories] Adding enderchest to database!");
                }
            }
            // update inventories if there is no attributes column
            String queryAttr = "SELECT sql FROM sqlite_master WHERE tbl_name = '" + plugin.getPrefix()
                    + "inventories' AND sql LIKE '%attributes TEXT%'";
            try (ResultSet rsAttr = statement.executeQuery(queryAttr); ) {
                if (!rsAttr.next()) {
                    String queryAlter4 = "ALTER TABLE " + plugin.getPrefix() + "inventories ADD attributes TEXT";
                    statement.executeUpdate(queryAlter4);
                    String queryAlter5 = "ALTER TABLE " + plugin.getPrefix() + "inventories ADD armour_attributes TEXT";
                    statement.executeUpdate(queryAlter5);
                    plugin.getLogger().log(Level.INFO, "[GameModeInventories] Adding attributes to database!");
                }
            }
            // add blocks table
            String queryBlocks = "CREATE TABLE IF NOT EXISTS " + plugin.getPrefix()
                    + "blocks (id INTEGER PRIMARY KEY NOT NULL, worldchunk TEXT, location TEXT)";
            statement.executeUpdate(queryBlocks);
            // update inventories if there is no attributes column
            String queryWorld = "SELECT sql FROM sqlite_master WHERE tbl_name = '" + plugin.getPrefix()
                    + "blocks' AND sql LIKE '%worldchunk TEXT%'";
            try (ResultSet rsWorld = statement.executeQuery(queryWorld); ) {
                if (!rsWorld.next()) {
                    String queryAlter6 = "ALTER TABLE " + plugin.getPrefix() + "blocks ADD worldchunk TEXT";
                    statement.executeUpdate(queryAlter6);
                    plugin.getLogger().log(Level.INFO, "[GameModeInventories] Adding new fields to database!");
                }
            }
            // add stands table
            String queryStands =
                    "CREATE TABLE IF NOT EXISTS " + plugin.getPrefix() + "stands (uuid TEXT PRIMARY KEY NOT NULL)";
            statement.executeUpdate(queryStands);
            // add worlds table
            String queryWorlds = "CREATE TABLE IF NOT EXISTS " + plugin.getPrefix()
                    + "worlds (id INTEGER PRIMARY KEY NOT NULL, uuid TEXT, world TEXT, x REAL, y REAL, z REAL, yaw REAL, pitch REAL)";
            statement.executeUpdate(queryWorlds);
            // close
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "SQLite create table error: " + e);
        }
    }
}
