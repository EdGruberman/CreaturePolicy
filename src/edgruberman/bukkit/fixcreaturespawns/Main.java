package edgruberman.bukkit.fixcreaturespawns;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public class Main extends org.bukkit.plugin.java.JavaPlugin {

    static ConfigurationFile configurationFile;
    static MessageManager messageManager;
    
    private static List<String> unsafeCreatureTypes = new ArrayList<String>();
    private static List<String> safeMaterial = new ArrayList<String>();
    private static int safeRadiusSquared = 24;
    
    public void onLoad() {
        Main.configurationFile = new ConfigurationFile(this);
        Main.configurationFile.load();
        
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
    }
	
    public void onEnable() {
        Main.unsafeCreatureTypes = this.getConfiguration().getStringList("unsafeCreatureTypes", Main.unsafeCreatureTypes);
        Main.messageManager.log("Unsafe Creature Types: " + Main.unsafeCreatureTypes, MessageLevel.CONFIG);
        
        Main.safeMaterial = this.getConfiguration().getStringList("safeMaterial", Main.safeMaterial);
        Main.messageManager.log("Safe Material: " + Main.safeMaterial, MessageLevel.CONFIG);
        
        Main.safeRadiusSquared = (int) Math.pow(this.getConfiguration().getInt("safeRadius", (int) Math.sqrt(Main.safeRadiusSquared)), 2);
        Main.messageManager.log("Safe Radius: " + (int) Math.sqrt(Main.safeRadiusSquared), MessageLevel.CONFIG);
        
        new EntityListener(this);
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }
    
    static boolean isAllowedSpawn(CreatureSpawnEvent event) {
        // Only prevent unsafe creatures.
        if (!unsafeCreatureTypes.contains(event.getCreatureType().getName())) return true;
        
        // Prevent on all safe materials.
        if (safeMaterial.contains(event.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().name())) return false;
        
        if (event.getSpawnReason() != SpawnReason.NATURAL) return true;
        
        // Prevent if distance from player is within the safety radius.
        for (Player player : event.getLocation().getWorld().getPlayers()) {
            double distanceSquared = player.getLocation().distanceSquared(event.getLocation());
            
            if (distanceSquared <= Main.safeRadiusSquared) {
                Main.messageManager.log("Unsafe " + event.getSpawnReason() + " spawn at " + (int) Math.sqrt(distanceSquared) + "m near " + player.getName(), MessageLevel.FINE);
                return false;
            }
        }
        
        // Passed all the checks, allow spawn by default.
        return true;
    }
}