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
public class GameModeInventoriesMySQL {

    private final GameModeInventories plugin;

    public GameModeInventoriesMySQL(GameModeInventories plugin) {
        this.plugin = plugin;
    }

    public void createTables() {
        try (Connection connection = plugin.getDatabaseConnection();
                Statement statement = connection.createStatement(); ) {
            // add inventories table
            String queryInventories = "CREATE TABLE IF NOT EXISTS " + plugin.getPrefix()
                    + "inventories (id int(11) NOT NULL AUTO_INCREMENT, uuid varchar(48) DEFAULT '', player varchar(24) DEFAULT '', gamemode varchar(24) DEFAULT '', inventory text, xp double, armour text, enderchest text, attributes text, armour_attributes text, PRIMARY KEY (id)) DEFAULT CHARSET=utf8 COLLATE utf8_general_ci";
            statement.executeUpdate(queryInventories);
            // update inventories if there is no attributes column
            String queryAttr = "SHOW COLUMNS FROM " + plugin.getPrefix() + "inventories LIKE 'attributes'";
            try (ResultSet rsAttr = statement.executeQuery(queryAttr); ) {
                if (!rsAttr.next()) {
                    String queryAlter4 = "ALTER TABLE " + plugin.getPrefix() + "inventories ADD attributes text";
                    statement.executeUpdate(queryAlter4);
                    String queryAlter5 = "ALTER TABLE " + plugin.getPrefix() + "inventories ADD armour_attributes text";
                    statement.executeUpdate(queryAlter5);
                    plugin.getLogger().log(Level.INFO, "[GameModeInventories] Adding attributes to database!");
                }
            }
            // add blocks table
            String queryBlocks = "CREATE TABLE IF NOT EXISTS " + plugin.getPrefix()
                    + "blocks (id int(11) NOT NULL AUTO_INCREMENT, worldchunk varchar(128), location text, PRIMARY KEY (id)) DEFAULT CHARSET=utf8 COLLATE utf8_general_ci";
            statement.executeUpdate(queryBlocks);
            // update blocks if there is no world column
            String queryWorld = "SHOW COLUMNS FROM " + plugin.getPrefix() + "blocks LIKE 'worldchunk'";
            try (ResultSet rsWorld = statement.executeQuery(queryWorld); ) {
                if (!rsWorld.next()) {
                    String queryAlter6 = "ALTER TABLE " + plugin.getPrefix() + "blocks ADD worldchunk varchar(128)";
                    statement.executeUpdate(queryAlter6);
                    plugin.getLogger().log(Level.INFO, "[GameModeInventories] Adding new fields to database!");
                }
            }
            // add stands table
            String queryStands = "CREATE TABLE IF NOT EXISTS " + plugin.getPrefix()
                    + "stands (uuid varchar(48) NOT NULL, PRIMARY KEY (uuid)) DEFAULT CHARSET=utf8 COLLATE utf8_general_ci";
            statement.executeUpdate(queryStands);
            // add worlds table
            String queryWorlds = "CREATE TABLE IF NOT EXISTS " + plugin.getPrefix()
                    + "worlds (id int(11) NOT NULL AUTO_INCREMENT, uuid varchar(48) DEFAULT '', world varchar(24) DEFAULT '', x double, y double, z double, yaw float, pitch float, PRIMARY KEY (id)) DEFAULT CHARSET=utf8 COLLATE utf8_general_ci";
            statement.executeUpdate(queryWorlds);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "MySQL create table error: " + e);
        }
    }
}
