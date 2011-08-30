package edgruberman.bukkit.fixcreaturespawns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public final class Main extends JavaPlugin {
    
    private static final ArrayList<String> DEFAULT_UNSAFE_SPAWN_REASONS = new ArrayList<String>(Arrays.asList(
              "NATURAL"
    ));
    private static final ArrayList<String> DEFAULT_UNSAFE_CREATURE_TYPES = new ArrayList<String>(Arrays.asList(
              "CREEPER"
            , "SKELETON"
            , "SPIDER"
            , "ZOMBIE"
    ));
    private static final ArrayList<String> DEFAULT_SAFE_MATERIAL = new ArrayList<String>(Arrays.asList(
              "LEAVES"
    ));
    private static final int DEFAULT_SAFE_RADIUS = 24;

    static ConfigurationFile configurationFile;
    static MessageManager messageManager;
    
    private static Set<SpawnReason> unsafeSpawnReasons = new HashSet<SpawnReason>();
    private static Set<CreatureType> unsafeCreatureTypes = new HashSet<CreatureType>();
    private static Set<Material> safeMaterial = new HashSet<Material>();
    private static Set<MaterialData> safeMaterialData = new HashSet<MaterialData>();
    private static int safeRadiusSquared = -1;
    
    public void onLoad() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        Main.configurationFile = new ConfigurationFile(this);
    }
	
    public void onEnable() {
        Main.loadConfiguration();
        new EntityListener(this);
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }
    
    static void loadConfiguration() {
        Configuration cfg = Main.configurationFile.getConfiguration();
        
        Main.unsafeSpawnReasons.clear();
        for (String name : cfg.getStringList("unsafeSpawnReasons", Main.DEFAULT_UNSAFE_SPAWN_REASONS))
            Main.unsafeSpawnReasons.add(SpawnReason.valueOf(name));
            
        Main.messageManager.log("Unsafe Spawn Reasons: " + Main.unsafeSpawnReasons.toString(), MessageLevel.CONFIG);
        
        Main.unsafeCreatureTypes.clear();
        for (String name : cfg.getStringList("unsafeCreatureTypes", Main.DEFAULT_UNSAFE_CREATURE_TYPES))
            Main.unsafeCreatureTypes.add(CreatureType.valueOf(name));
            
        Main.messageManager.log("Unsafe Creature Types: " + Main.unsafeCreatureTypes.toString(), MessageLevel.CONFIG);
        
        Main.safeMaterial.clear();
        Main.safeMaterialData.clear();
        for (String entry : cfg.getStringList("safeMaterial", Main.DEFAULT_SAFE_MATERIAL)) {
            Material material = null;
            Byte data = null;
            if (entry.contains(":")) {
                material = Material.valueOf(entry.split(":")[0]);
                data = Byte.parseByte(entry.split(":")[1]);
                Main.safeMaterialData.add(new MaterialData(material, data));
            } else {
                material = Material.valueOf(entry);
                Main.safeMaterial.add(material);
            }
        }
        Main.messageManager.log("Safe Material: " + Main.safeMaterial.toString() + " " + Main.safeMaterialData.toString(), MessageLevel.CONFIG);
        
        Main.safeRadiusSquared = (int) Math.pow(cfg.getInt("safeRadius", Main.DEFAULT_SAFE_RADIUS), 2);
        int safeRadius = Main.safeRadiusSquared;
        if (Main.safeRadiusSquared >= 0) safeRadius = (int) Math.sqrt(Main.safeRadiusSquared);
        Main.messageManager.log("Safe Radius: " + safeRadius, MessageLevel.CONFIG);
    }
    
    static boolean isAllowedSpawn(final CreatureSpawnEvent event) {
        // Allow safe spawn reasons.
        if (!Main.unsafeSpawnReasons.contains(event.getSpawnReason())) return true;
        
        // Allow safe creatures.
        if (!Main.unsafeCreatureTypes.contains(event.getCreatureType())) return true;
        
        // Prevent on safe materials.
        Block block = event.getLocation().getBlock().getRelative(BlockFace.DOWN);
        if (Main.safeMaterial.contains(block.getType())) return false;
        if (Main.safeMaterialData.contains(new MaterialData(block.getType(), block.getData()))) return false;
        
        // Prevent if distance from player is within a defined safety radius.
        if (Main.safeRadiusSquared >= 0) {
            for (Player player : event.getLocation().getWorld().getPlayers()) {
                double distanceSquared = player.getLocation().distanceSquared(event.getLocation());
                if (distanceSquared <= Main.safeRadiusSquared) {
                    Main.messageManager.log("Unsafe " + event.getSpawnReason() + " spawn at " + (int) Math.sqrt(distanceSquared) + "m near " + player.getName(), MessageLevel.FINE);
                    return false;
                }
            }
        }
        
        // Passed all the checks, allow spawn.
        return true;
    }
}