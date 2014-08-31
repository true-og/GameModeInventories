/*
 *  Copyright 2014 eccentric_nz.
 */
package me.eccentric_nz.gamemodeinventories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author eccentric_nz
 */
public class GameModeInventoriesMySQL {

    private final GameModeInventoriesDBConnection service = GameModeInventoriesDBConnection.getInstance();
    private final Connection connection = service.getConnection();
    private Statement statement = null;
    private final GameModeInventories plugin;

    public GameModeInventoriesMySQL(GameModeInventories plugin) {
        this.plugin = plugin;
    }

    public void createTables() {
        service.setIsMySQL(true);
        try {
            service.testConnection(connection);
            statement = connection.createStatement();
            // add inventories table
            String queryInventories = "CREATE TABLE IF NOT EXISTS inventories (id int(11) NOT NULL AUTO_INCREMENT, uuid varchar(48) DEFAULT '', player varchar(24) DEFAULT '', gamemode varchar(24) DEFAULT '', inventory text, xp double, armour text, enderchest text, attributes text, armour_attributes text, PRIMARY KEY (id)) DEFAULT CHARSET=utf8 COLLATE utf8_general_ci";
            statement.executeUpdate(queryInventories);

            // update inventories if there is no attributes column
            String queryAttr = "SHOW COLUMNS FROM inventories LIKE 'attributes'";
            ResultSet rsAttr = statement.executeQuery(queryAttr);
            if (!rsAttr.next()) {
                String queryAlter4 = "ALTER TABLE inventories ADD attributes text";
                statement.executeUpdate(queryAlter4);
                String queryAlter5 = "ALTER TABLE inventories ADD armour_attributes text";
                statement.executeUpdate(queryAlter5);
                System.out.println("[GameModeInventories] Adding attributes to database!");
            }

            // add blocks table
            String queryBlocks = "CREATE TABLE IF NOT EXISTS blocks (id int(11) NOT NULL AUTO_INCREMENT, location text, PRIMARY KEY (id)) DEFAULT CHARSET=utf8 COLLATE utf8_general_ci";
            statement.executeUpdate(queryBlocks);

        } catch (SQLException e) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.MY_PLUGIN_NAME + "MySQL create table error: " + e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                plugin.getServer().getConsoleSender().sendMessage(plugin.MY_PLUGIN_NAME + "MySQL close statement error: " + e);
            }
        }
    }
}