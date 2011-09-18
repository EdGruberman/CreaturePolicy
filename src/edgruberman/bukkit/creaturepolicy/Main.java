package edgruberman.bukkit.creaturepolicy;

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
        new Enforcer(this);
        new Publisher(this);
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }
    
    static void loadConfiguration() {
        Publisher.policies.clear();
        for (World world : Bukkit.getServer().getWorlds())
            Publisher.publish(world);
    }
    
    static void loadPolicy(final Policy policy) {
        if (!Main.worldFiles.containsKey(policy.world)) {
            Main.worldFiles.put(policy.world, new ConfigurationFile(Main.plugin, WORLD_SPECIFICS + "/" + policy.world.getName() + ".yml"));
        } else {
            Main.worldFiles.get(policy.world).load();
        }
        
        Configuration cfg = Main.worldFiles.get(policy.world).getConfiguration();
        
        policy.rules.clear();
        
        List<String> keys = cfg.getKeys();
        if (keys == null || keys.size() == 0) {
            Main.messageManager.log("Policy loaded for [" + policy.world.getName() + "] with " + policy.defaultRule, MessageLevel.CONFIG);
            return;
        }
        
        for (String key : keys)
            Main.loadRule(key, cfg.getNode(key), policy);
        
        Main.messageManager.log("Loaded " + policy.rules.size() + " rule" + (policy.rules.size() != 1 ? "s" : "") + " into policy for [" + policy.world.getName() + "]", MessageLevel.CONFIG);
        Main.messageManager.log("Policy loaded for [" + policy.world.getName() + "] with " + policy.defaultRule, MessageLevel.CONFIG);
    }
    
    private static void loadRule(final String creature, final ConfigurationNode node, final Policy policy) {
        CreatureType type = null;
        if (!creature.equals("default")) {
            try {
                type = CreatureType.valueOf(creature);
            } catch (IllegalArgumentException e) {
                Main.messageManager.log("Unrecognized creature \"" + node.getString("creature") + "\" in \"" + Main.worldFiles.get(policy.world).getFile().getPath() + "\"", MessageLevel.WARNING);
                return;
            }
        }
        
        boolean allow = node.getBoolean("allow", Rule.DEFAULT_ALLOW);
        
        Rule rule = new Rule(type, allow);
        
        for (ConfigurationNode e : node.getNodeList("exceptions", null))
            Main.loadException(e, rule);
        
        policy.add(rule);
        
        Main.messageManager.log("Loaded rule in policy for [" + policy.world.getName() + "] as " + rule, MessageLevel.FINEST);
    }
    
    private static void loadException(final ConfigurationNode node, final Rule rule) {
        SpawnReason reason = Exception.DEFAULT_REASON;
        try {
            String reasonCfg = node.getString("reason");
            if (reasonCfg != null)
                reason = SpawnReason.valueOf(reasonCfg);
            
        } catch (IllegalArgumentException e) {
            Main.messageManager.log("Unrecognized reason \"" + node.getString("reason") + "\"", MessageLevel.WARNING);
            return;
        }
        
        MaterialData md = Exception.DEFAULT_MATERIAL;
        String materialEntry = node.getString("material");
        if (materialEntry != null) {
            Material material;
            try {
                material = Material.valueOf(materialEntry.split(":")[0]);
                
            } catch (IllegalArgumentException e) {
                Main.messageManager.log("Unrecognized material \"" + materialEntry.split(":")[0] + "\"", MessageLevel.WARNING);
                return;
            }
            
            Byte data = Exception.MATERIAL_DATA_ANY;
            if (materialEntry.contains(":")) {
                try {
                    data = Byte.parseByte(materialEntry.split(":")[1]);
                    
                } catch (NumberFormatException e) {
                    Main.messageManager.log("Unrecognized data value \"" + materialEntry + "\"", MessageLevel.WARNING);
                    return;
                }
            }
            
            md = new MaterialData(material, data);
        }
        
        BlockFace relative = Exception.DEFAULT_RELATIVE;
        try {
            String relativeCfg = node.getString("relative");
            if (relativeCfg != null)
                relative = BlockFace.valueOf(relativeCfg);
            
        } catch (IllegalArgumentException e) {
            Main.messageManager.log("Unrecognized relative \"" + node.getString("relative") + "\"", MessageLevel.WARNING);
            return;
        }
        
        Exception e = new Exception(reason, md, relative);
        rule.exceptions.add(e);
    }
}