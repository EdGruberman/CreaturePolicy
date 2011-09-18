package edgruberman.bukkit.fixcreaturespawns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public final class Main extends JavaPlugin {
    
    /**
     * Base path, relative to plugin data folder, to look for world specific
     * configuration overrides in. Sub-folder is used to avoid conflicts
     * between world names and configuration file names. (e.g. a world named
     * config.)
     */
    private static final String WORLD_SPECIFICS = "Worlds";
    
    static ConfigurationFile configurationFile;
    static MessageManager messageManager;

    private static Plugin plugin;
    private static Map<World, ConfigurationFile> worldFiles = new HashMap<World, ConfigurationFile>();
    
    public void onLoad() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        Main.configurationFile = new ConfigurationFile(this);
        Main.plugin = this;
    }
	
    public void onEnable() {
        Main.loadConfiguration();
        new SpawnCanceller(this);
        new WorldMonitor(this);
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }
    
    static void loadConfiguration() {
        SpawnBlacklist.worlds.clear();
        for (World world : Bukkit.getServer().getWorlds())
            Main.loadBlacklist(world, new SpawnBlacklist(world));
    }
    
    static int loadBlacklist(final World world, final SpawnBlacklist blacklist) {
        if (!Main.worldFiles.containsKey(world)) {
            Main.worldFiles.put(world, new ConfigurationFile(Main.plugin, WORLD_SPECIFICS + "/" + world.getName() + ".yml"));
        } else {
            Main.worldFiles.get(world).load();
        }
        
        Configuration cfg = Main.worldFiles.get(world).getConfiguration();
        
        blacklist.creatures.clear();
        
        List<String> keys = cfg.getKeys();
        if (keys == null || keys.size() == 0) {
            Main.messageManager.log("All spawns allowed for [" + world.getName() + "]", MessageLevel.CONFIG);
            return 0;
        }
        
        for (String key : keys) {
            if (key.equals("safeRadius")) {
                blacklist.setSafeRadius(cfg.getInt("safeRadius", SpawnBlacklist.DEFAULT_SAFE_RADIUS));
                Main.messageManager.log("Safe Radius for [" + blacklist.world.getName() + "]: " + blacklist.getSafeRadius(), MessageLevel.CONFIG);
                
            } else {
                Main.loadDefinition(cfg.getNode(key), blacklist);

            }
        }
        
        int count = 0;
        for (List<SpawnDefinition> def : blacklist.creatures.values())
            count += def.size();
        
        Main.messageManager.log("Loaded " + count + " spawn definition" + (count != 1 ? "s" : "") + " into blacklist for [" + world.getName() + "]", MessageLevel.CONFIG);
        return count;
    }
    
    private static void loadDefinition(final ConfigurationNode node, final SpawnBlacklist blacklist) {
        CreatureType type;
        try {
            type = CreatureType.valueOf(node.getString("creature"));
        } catch (IllegalArgumentException e) {
            Main.messageManager.log("Unrecognized creature \"" + node.getString("creature") + "\" in \"" + Main.worldFiles.get(blacklist.world).getFile().getPath() + "\"", MessageLevel.WARNING);
            return;
        }
        
        if (!blacklist.creatures.containsKey(type))
            blacklist.creatures.put(type, new ArrayList<SpawnDefinition>());
        
        SpawnReason reason = SpawnDefinition.DEFAULT_REASON;
        try {
            String reasonCfg = node.getString("reason");
            if (reasonCfg != null)
                reason = SpawnReason.valueOf(reasonCfg);
            
        } catch (IllegalArgumentException e) {
            Main.messageManager.log("Unrecognized reason \"" + node.getString("reason") + "\" in \"" + Main.worldFiles.get(blacklist.world).getFile().getPath() + "\"", MessageLevel.WARNING);
            return;
        }
        
        MaterialData md = SpawnDefinition.DEFAULT_MATERIAL;
        String materialEntry = node.getString("material");
        if (materialEntry != null) {
            Material material;
            try {
                material = Material.valueOf(materialEntry.split(":")[0]);
                
            } catch (IllegalArgumentException e) {
                Main.messageManager.log("Unrecognized material \"" + materialEntry.split(":")[0] + "\" in \"" + Main.worldFiles.get(blacklist.world).getFile().getPath() + "\"", MessageLevel.WARNING);
                return;
            }
            
            Byte data = SpawnDefinition.MATERIAL_DATA_ANY;
            if (materialEntry.contains(":")) {
                try {
                    data = Byte.parseByte(materialEntry.split(":")[1]);
                    
                } catch (NumberFormatException e) {
                    Main.messageManager.log("Unrecognized data value \"" + materialEntry + "\" in \"" + Main.worldFiles.get(blacklist.world).getFile().getPath() + "\"", MessageLevel.WARNING);
                    return;
                }
            }
            
            md = new MaterialData(material, data);
        }
        
        BlockFace relative = SpawnDefinition.DEFAULT_RELATIVE;
        try {
            String relativeCfg = node.getString("relative");
            if (relativeCfg != null)
                relative = BlockFace.valueOf(relativeCfg);
            
        } catch (IllegalArgumentException e) {
            Main.messageManager.log("Unrecognized relative \"" + node.getString("relative") + "\" in \"" + Main.worldFiles.get(blacklist.world).getFile().getPath() + "\"", MessageLevel.WARNING);
            return;
        }
        
        SpawnDefinition def = new SpawnDefinition(
                  reason
                , md
                , relative
        );
        blacklist.creatures.get(type).add(def);
        
        Main.messageManager.log("Loaded blacklist spawn definition for " + type + " in [" + blacklist.world.getName() + "] as " + def, MessageLevel.FINEST);
    }
}