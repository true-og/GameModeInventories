/*
 *  Copyright 2014 eccentric_nz.
 */
package me.eccentric_nz.gamemodeinventories;

import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import java.util.logging.Level;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.plugin.PluginManager;

/**
 * @author eccentric_nz
 */
public class GameModeInventoriesBlockLogger {

    private final GameModeInventories plugin;
    private CoreProtectAPI coreProtectAPI = null;
    private Consumer logBlockConsumer = null;
    private GMIBlockLogger whichLogger;
    private boolean logging = false;

    public GameModeInventoriesBlockLogger(GameModeInventories plugin) {
        this.plugin = plugin;
    }

    public CoreProtectAPI getCoreProtectAPI() {
        return coreProtectAPI;
    }

    public Consumer getLogBlockConsumer() {
        return logBlockConsumer;
    }

    public GMIBlockLogger getWhichLogger() {
        return whichLogger;
    }

    public boolean isLogging() {
        return logging;
    }

    public void enableLogger() {
        PluginManager pm = plugin.getServer().getPluginManager();
        if (pm.isPluginEnabled("CoreProtect")) {
            CoreProtect cp = (CoreProtect) pm.getPlugin("CoreProtect");
            // Check that CoreProtect is loaded
            if (cp == null || !(cp instanceof CoreProtect)) {
                return;
            }
            // Check that the API is enabled
            CoreProtectAPI CoreProtect = cp.getAPI();
            if (CoreProtect.isEnabled() == false) {
                return;
            }
            // Check that a compatible version of the API is loaded
            if (CoreProtect.APIVersion() < 6) {
                return;
            }
            plugin.getLogger().log(Level.INFO, "Connecting to CoreProtect");
            coreProtectAPI = CoreProtect;
            whichLogger = GMIBlockLogger.CORE_PROTECT;
            logging = true;
        }
        if (pm.isPluginEnabled("LogBlock")) {
            LogBlock lb = (LogBlock) pm.getPlugin("LogBlock");
            if (lb != null) {
                plugin.getLogger().log(Level.INFO, "Connecting to LogBlock");
                logBlockConsumer = lb.getConsumer();
                whichLogger = GMIBlockLogger.LOG_BLOCK;
                logging = true;
            }
        }
    }

    public enum GMIBlockLogger {
        CORE_PROTECT,
        LOG_BLOCK
    }
}
