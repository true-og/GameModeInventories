package me.eccentric_nz.gamemodeinventories;

import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.eccentric_nz.gamemodeinventories.database.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class GameModeInventories extends JavaPlugin {

    public static GameModeInventories plugin;
    public final String MY_PLUGIN_NAME = ChatColor.GOLD + "[GameModeInventories] " + ChatColor.RESET;
    private final HashMap<String, List<String>> creativeBlocks = new HashMap<>();
    private final List<Material> blackList = new ArrayList<>();
    private final List<Material> noTrackList = new ArrayList<>();
    private final List<String> points = new ArrayList<>();
    private final List<UUID> stands = new ArrayList<>();
    public BukkitTask recordingTask;
    private GameModeInventoriesInventory inventoryHandler;
    private GameModeInventoriesBlock block;
    private GameModeInventoriesMessage m;
    private GameModeInventoriesBlockLogger blockLogger;
    private GMIDebug db_level;
    private String prefix;
    private HikariDataSource dataSource;
    private FileConfiguration canonicalConfig;

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach((p) -> {
            if (p.hasPermission("gamemodeinventories.use")) {
                if (p.isOnline()) {
                    inventoryHandler.switchInventories(p, p.getGameMode());
                }
            }
        });
        new GameModeInventoriesStand(this).saveStands();
        new GameModeInventoriesQueueDrain(this).forceDrainQueue();
    }

    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pm = Bukkit.getServer().getPluginManager();
        loadCanonicalConfig();
        if (canonicalConfig == null) {
            pm.disablePlugin(this);
            return;
        }
        Version bukkitversion = getServerVersion(getServer().getVersion());
        Version minversion = new Version("1.13");
        // check CraftBukkit version
        if (bukkitversion.compareTo(minversion) >= 0) {
            GameModeInventoriesConfig tc = new GameModeInventoriesConfig(this);
            tc.checkConfig();
            loadDatabase();
            // update database add and populate block fields
            if (!getConfig().getBoolean("blocks_conversion_done")) {
                new GameModeInventoriesBlocksConverter(this).convertBlocksTable();
                plugin.getLogger().log(Level.INFO, "[GameModeInventories] Blocks conversion successful :)");
                plugin.getLogger()
                        .log(
                                Level.INFO,
                                "[GameModeInventories] config.yml is immutable; leaving blocks_conversion_done unchanged.");
            }
            // check if creative world exists
            if (getConfig().getBoolean("creative_world.switch_to")) {
                World creative = getServer().getWorld(getConfig().getString("creative_world.world"));
                if (creative == null) {
                    plugin.getLogger()
                            .log(
                                    Level.INFO,
                                    "[GameModeInventories] Creative world specified in the config was not found; world switching will stay disabled until config.yml is updated manually.");
                }
            }
            block = new GameModeInventoriesBlock(this);
            m = new GameModeInventoriesMessage(this);
            m.getMessages();
            m.updateMessages();
            try {
                db_level = GMIDebug.valueOf(getConfig().getString("debug_level"));
            } catch (IllegalArgumentException e) {
                db_level = GMIDebug.ERROR;
            }
            inventoryHandler = new GameModeInventoriesInventory(this);
            pm.registerEvents(new GameModeInventoriesListener(this), this);
            pm.registerEvents(new GameModeInventoriesChunkLoadListener(this), this);
            pm.registerEvents(new GameModeInventoriesDeath(this), this);
            pm.registerEvents(new GameModeInventoriesBlockListener(this), this);
            if (getConfig().getBoolean("track_creative_place.dont_track_is_whitelist")) {
                pm.registerEvents(new GameModeInventoriesTrackWhiteListener(this), this);
            } else {
                pm.registerEvents(new GameModeInventoriesTrackBlackListener(this), this);
            }
            pm.registerEvents(new GameModeInventoriesPistonListener(this), this);
            pm.registerEvents(new GameModeInventoriesCommandListener(this), this);
            pm.registerEvents(new GameModeInventoriesWorldListener(this), this);
            pm.registerEvents(new GameModeInventoriesEntityListener(this), this);
            pm.registerEvents(new GameModeInventoriesPhysicsListener(this), this);
            pm.registerEvents(new GameModeInventoriesVillagerListener(this), this);
            GameModeInventoriesCommands command = new GameModeInventoriesCommands(this);
            getCommand("gmi").setExecutor(command);
            getCommand("gmi").setTabCompleter(command);
            new GameModeInventoriesStand(this).loadStands();
            loadBlackList();
            loadNoTrackList();
            setUpBlockLogger();
            actionRecorderTask();
        } else {
            getServer()
                    .getConsoleSender()
                    .sendMessage(MY_PLUGIN_NAME + ChatColor.RED
                            + "This plugin requires CraftBukkit/Spigot 1.9 or higher, disabling...");
            pm.disablePlugin(this);
        }
    }

    private void loadCanonicalConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getLogger()
                    .log(
                            Level.SEVERE,
                            "config.yml was not found at "
                                    + configFile.getAbsolutePath()
                                    + ". This plugin only reads config.yml and will not generate or modify it.");
            canonicalConfig = null;
            return;
        }
        canonicalConfig = YamlConfiguration.loadConfiguration(configFile);
    }

    @Override
    public FileConfiguration getConfig() {
        return (canonicalConfig != null) ? canonicalConfig : super.getConfig();
    }

    private Version getServerVersion(String s) {
        Pattern pat = Pattern.compile("\\((.+?)\\)", Pattern.DOTALL);
        Matcher mat = pat.matcher(s);
        String v;
        if (mat.find()) {
            String[] split = mat.group(1).split(" ");
            String[] tmp = split[1].split("-");
            if (tmp.length > 1) {
                v = tmp[0];
            } else {
                v = split[1];
            }
        } else {
            v = "1.7.10";
        }
        return new Version(v);
    }

    /**
     * Sets up the database.
     */
    private void loadDatabase() {
        String dbtype = getConfig().getString("storage.database");
        prefix = getConfig().getString("storage.prefix");
        try {
            if (dbtype.equals("sqlite")) {
                GameModeInventoriesSQLiteConnectionPool pool = new GameModeInventoriesSQLiteConnectionPool(this);
                dataSource = pool.getHikari();
                GameModeInventoriesSQLite sqlite = new GameModeInventoriesSQLite(this);
                sqlite.createTables();
            } else {
                GameModeInventoriesMySQLConnectionPool pool = new GameModeInventoriesMySQLConnectionPool(this);
                dataSource = pool.getHikari();
                GameModeInventoriesMySQL mysql = new GameModeInventoriesMySQL(this);
                mysql.createTables();
            }
        } catch (ClassNotFoundException e) {
            getServer().getConsoleSender().sendMessage(MY_PLUGIN_NAME + "Connection and Tables Error: " + e);
        }
    }

    public Connection getDatabaseConnection() {
        Connection con = null;
        try {
            con = dataSource.getConnection();
        } catch (SQLException e) {
            debug("Could not get database connection: " + e.getMessage());
        }
        return con;
    }

    /**
     * Loads block logger support if available
     */
    public void setUpBlockLogger() {
        blockLogger = new GameModeInventoriesBlockLogger(this);
        blockLogger.enableLogger();
    }

    public GameModeInventoriesBlockLogger getBlockLogger() {
        return blockLogger;
    }

    public void debug(Object o, GMIDebug b) {
        if (getConfig().getBoolean("debug") == true) {
            if (b.equals(db_level) || b.equals(GMIDebug.ALL)) {
                getServer().getConsoleSender().sendMessage(MY_PLUGIN_NAME + "Debug: " + o);
            }
        }
    }

    public void debug(Object o) {
        debug(o, GMIDebug.ERROR);
    }

    public GameModeInventoriesInventory getInventoryHandler() {
        return inventoryHandler;
    }

    public GameModeInventoriesBlock getBlock() {
        return block;
    }

    public HashMap<String, List<String>> getCreativeBlocks() {
        return creativeBlocks;
    }

    public List<Material> getBlackList() {
        return blackList;
    }

    private void loadBlackList() {
        List<String> bl = getConfig().getStringList("blacklist");
        bl.forEach((s) -> {
            try {
                blackList.add(Material.valueOf(s));
            } catch (IllegalArgumentException iae) {
                getServer()
                        .getConsoleSender()
                        .sendMessage(MY_PLUGIN_NAME + m.getMessage().get("INVALID_MATERIAL") + " " + s);
            }
        });
    }

    public List<Material> getNoTrackList() {
        return noTrackList;
    }

    private void loadNoTrackList() {
        List<String> ntl = getConfig().getStringList("track_creative_place.dont_track");
        ntl.forEach((s) -> {
            try {
                noTrackList.add(Material.valueOf(s));
            } catch (IllegalArgumentException iae) {
                getServer()
                        .getConsoleSender()
                        .sendMessage(MY_PLUGIN_NAME + m.getMessage().get("INVALID_MATERIAL_TRACK") + " " + s);
            }
        });
    }

    public List<String> getPoints() {
        return points;
    }

    public List<UUID> getStands() {
        return stands;
    }

    public GameModeInventoriesMessage getM() {
        return m;
    }

    public String getPrefix() {
        return prefix;
    }

    public void actionRecorderTask() {
        int recorder_tick_delay = 3;
        // we schedule it once, it will reschedule itself
        recordingTask = getServer()
                .getScheduler()
                .runTaskLaterAsynchronously(this, new GameModeInventoriesRecordingTask(this), recorder_tick_delay);
    }
}
