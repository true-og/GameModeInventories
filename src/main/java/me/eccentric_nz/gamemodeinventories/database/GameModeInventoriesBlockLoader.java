/*
 *  Copyright 2015 eccentric_nz.
 */
package me.eccentric_nz.gamemodeinventories.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import me.eccentric_nz.gamemodeinventories.GMIDebug;
import me.eccentric_nz.gamemodeinventories.GameModeInventories;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author eccentric_nz
 */
public class GameModeInventoriesBlockLoader extends BukkitRunnable {

    private final GameModeInventories plugin;
    private final String gmiwc;

    public GameModeInventoriesBlockLoader(GameModeInventories plugin, String gmiwc) {
        this.plugin = plugin;
        this.gmiwc = gmiwc;
    }

    @Override
    public void run() {
        String blocksQuery = "SELECT location FROM " + plugin.getPrefix() + "blocks WHERE worldchunk = ?";
        try (Connection connection = plugin.getDatabaseConnection();
                PreparedStatement psb = connection.prepareStatement(blocksQuery); ) {
            psb.setString(1, gmiwc);
            try (ResultSet rb = psb.executeQuery(); ) {
                if (rb.isBeforeFirst()) {
                    List<String> l = new ArrayList<>();
                    while (rb.next()) {
                        l.add(rb.getString("location"));
                    }
                    plugin.getCreativeBlocks().put(gmiwc, l);
                }
                plugin.debug("Protecting blocks for chunk: " + gmiwc, GMIDebug.ALL);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load blocks, " + e);
        }
    }
}
