/*
 *  Copyright 2013 eccentric_nz.
 */
package me.eccentric_nz.gamemodeinventories;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author eccentric_nz
 */
public class GameModeInventoriesConfig {

    private final GameModeInventories plugin;
    private final File configFile;
    HashMap<String, String> strOptions = new HashMap<>();
    HashMap<String, Integer> intOptions = new HashMap<>();
    HashMap<String, Boolean> boolOptions = new HashMap<>();
    List<String> containers = new ArrayList<>();
    List<String> bl = new ArrayList<>();
    List<String> com = new ArrayList<>();
    List<String> wor = new ArrayList<>();
    List<String> no = new ArrayList<>();

    public GameModeInventoriesConfig(GameModeInventories plugin) {

        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "config.yml");
        // string
        strOptions.put("debug_level", "ERROR");
        strOptions.put("storage.mysql.server", "localhost");
        strOptions.put("storage.mysql.port", "3306");
        strOptions.put("storage.mysql.database", "GMI");
        strOptions.put("storage.mysql.user", "bukkit");
        strOptions.put("storage.mysql.password", "mysecurepassword");
        strOptions.put("storage.database", "sqlite");
        strOptions.put("storage.prefix", "");
        strOptions.put("creative_world.world", "creative");
        strOptions.put("creative_world.location", "last_known");
        // int
        intOptions.put("storage.mysql.pool_size", 10);
        // boolean
        boolOptions.put("armor", true);
        boolOptions.put("break_bedrock", false);
        boolOptions.put("bypass.inventories", true);
        boolOptions.put("bypass.items", true);
        boolOptions.put("bypass.blacklist", false);
        boolOptions.put("bypass.commands", false);
        boolOptions.put("bypass.survival", false);
        boolOptions.put("bypass.trades", false);
        boolOptions.put("command_blacklist", false);
        boolOptions.put("creative_blacklist", false);
        boolOptions.put("debug", false);
        boolOptions.put("dont_spam_chat", false);
        boolOptions.put("enderchest", true);
        boolOptions.put("no_creative_pvp", false);
        boolOptions.put("no_drops", false);
        boolOptions.put("no_falling_drops", false);
        boolOptions.put("no_pickups", false);
        boolOptions.put("no_villager_trade", false);
        boolOptions.put("no_wither_spawn", false);
        boolOptions.put("no_golem_spawn", false);
        boolOptions.put("remove_potions", true);
        boolOptions.put("restrict_creative", false);
        boolOptions.put("restrict_spectator", false);
        boolOptions.put("save_on_death", true);
        boolOptions.put("creative_world.switch_to", false);
        boolOptions.put("track_creative_place.break_no_drop", false);
        boolOptions.put("track_creative_place.enabled", true);
        boolOptions.put("track_creative_place.no_piston_move", false);
        boolOptions.put("track_creative_place.attached_block", false);
        boolOptions.put("track_creative_place.no_seeds_from_pumpkin", false);
        boolOptions.put("track_creative_place.dont_track_is_whitelist", false);
        boolOptions.put("xp", true);
        boolOptions.put("uuid_conversion_done", false);
        boolOptions.put("blocks_conversion_done", false);
        boolOptions.put("storage.mysql.test_connection", false);
        boolOptions.put("storage.mysql.useSSL", true);
        containers.add("ANVIL");
        containers.add("BARREL");
        containers.add("BEACON");
        containers.add("BEE_NEST");
        containers.add("BEEHIVE");
        containers.add("BLACK_SHULKER_BOX");
        containers.add("BLAST_FURNACE");
        containers.add("BLUE_SHULKER_BOX");
        containers.add("BREWING_STAND");
        containers.add("BROWN_SHULKER_BOX");
        containers.add("CAMPFIRE");
        containers.add("CARTOGRAPHY_TABLE");
        containers.add("CHEST");
        containers.add("CHIPPED_ANVIL");
        containers.add("COMPOSTER");
        containers.add("CYAN_SHULKER_BOX");
        containers.add("DAMAGED_ANVIL");
        containers.add("DISPENSER");
        containers.add("DROPPER");
        containers.add("ENCHANTING_TABLE");
        containers.add("ENDER_CHEST");
        containers.add("FLETCHING_TABLE");
        containers.add("FURNACE");
        containers.add("GRAY_SHULKER_BOX");
        containers.add("GREEN_SHULKER_BOX");
        containers.add("GRINDSTONE");
        containers.add("HOPPER");
        containers.add("JUKEBOX");
        containers.add("LECTERN");
        containers.add("LIGHT_BLUE_SHULKER_BOX");
        containers.add("LIGHT_GRAY_SHULKER_BOX");
        containers.add("LIME_SHULKER_BOX");
        containers.add("LOOM");
        containers.add("MAGENTA_SHULKER_BOX");
        containers.add("ORANGE_SHULKER_BOX");
        containers.add("PINK_SHULKER_BOX");
        containers.add("PURPLE_SHULKER_BOX");
        containers.add("RED_SHULKER_BOX");
        containers.add("SHULKER_BOX");
        containers.add("SMITHING_TABLE");
        containers.add("SMOKER");
        containers.add("SOUL_CAMPFIRE");
        containers.add("STONECUTTER");
        containers.add("TRAPPED_CHEST");
        containers.add("WHITE_SHULKER_BOX");
        containers.add("YELLOW_SHULKER_BOX");
        bl.add("TNT");
        bl.add("BEDROCK");
        bl.add("LAVA_BUCKET");
        com.add("give");
        com.add("i");
        com.add("buy");
        com.add("sell");
        wor.add("world");
        no.add("STONE");
        no.add("DIRT");

    }

    public void checkConfig() {

        if (!configFile.exists()) {

            plugin.getLogger().log(Level.SEVERE, "config.yml was not found at " + configFile.getAbsolutePath()
                    + ". This plugin only reads config.yml and will not generate or modify it.");
            return;

        }

        FileConfiguration config = plugin.getConfig();
        int missing = 0;
        // string values
        for (Map.Entry<String, String> entry : strOptions.entrySet()) {

            if (!config.contains(entry.getKey())) {

                missing++;
                plugin.getLogger().log(Level.WARNING, "Missing config option: " + entry.getKey());

            }

        }

        // int values
        for (Map.Entry<String, Integer> entry : intOptions.entrySet()) {

            if (!config.contains(entry.getKey())) {

                missing++;
                plugin.getLogger().log(Level.WARNING, "Missing config option: " + entry.getKey());

            }

        }

        // boolean values
        for (Map.Entry<String, Boolean> entry : boolOptions.entrySet()) {

            if (!config.contains(entry.getKey())) {

                missing++;
                plugin.getLogger().log(Level.WARNING, "Missing config option: " + entry.getKey());

            }

        }

        if (!config.contains("containers")) {

            missing++;
            plugin.getLogger().log(Level.WARNING, "Missing config option: containers");

        }

        if (!config.contains("blacklist")) {

            missing++;
            plugin.getLogger().log(Level.WARNING, "Missing config option: blacklist");

        } else if (config.getStringList("blacklist").contains("ZOMBIE_PIGMAN_SPAWN_EGG")) {

            plugin.getLogger().log(Level.WARNING,
                    "config.yml still contains ZOMBIE_PIGMAN_SPAWN_EGG; update it manually to ZOMBIFIED_PIGLIN_SPAWN_EGG if needed.");

        }

        if (!config.contains("commands")) {

            missing++;
            plugin.getLogger().log(Level.WARNING, "Missing config option: commands");

        }

        if (!config.contains("track_creative_place.worlds")) {

            missing++;
            plugin.getLogger().log(Level.WARNING, "Missing config option: track_creative_place.worlds");

        }

        if (!config.contains("track_creative_place.dont_track")) {

            missing++;
            plugin.getLogger().log(Level.WARNING, "Missing config option: track_creative_place.dont_track");

        }

        if (config.contains("storage.mysql.url")) {

            plugin.getLogger().log(Level.WARNING,
                    "Legacy storage.mysql.url is still present in config.yml; update it manually because automatic migration is disabled.");

        }

        if (missing > 0) {

            plugin.getLogger().log(Level.WARNING,
                    "config.yml is immutable; missing options were detected but not written automatically.");

        }

    }

}
