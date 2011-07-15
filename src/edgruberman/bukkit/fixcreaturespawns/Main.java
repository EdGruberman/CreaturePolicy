package edgruberman.bukkit.fixcreaturespawns;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

public class Main extends org.bukkit.plugin.java.JavaPlugin {

    static ConfigurationFile configurationFile;
    static MessageManager messageManager;
    
    private List<String> unsafeCreatureTypes = new ArrayList<String>();
    private List<String> safeMaterial = new ArrayList<String>();
    private int safeRadius;
    
    public void onLoad() {
        Main.configurationFile = new ConfigurationFile(this);
        Main.configurationFile.load();
        
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
    }
	
    public void onEnable() {
        this.unsafeCreatureTypes = this.getConfiguration().getStringList("unsafeCreatureTypes", this.unsafeCreatureTypes);
        Main.messageManager.log("Unsafe Creature Types: " + this.unsafeCreatureTypes, MessageLevel.CONFIG);
        
        this.safeMaterial = this.getConfiguration().getStringList("safeMaterial", this.safeMaterial);
        Main.messageManager.log("Safe Material: " + this.safeMaterial, MessageLevel.CONFIG);
        
        PluginManager pluginManager = this.getServer().getPluginManager();
        EntityListener entityListener = new EntityListener(this);
        pluginManager.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Event.Priority.Normal, this);

        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }
    
    protected boolean isAllowedSpawn(CreatureType type, Location spawningAt) {
        // Only prevent unsafe creatures.
        if (!unsafeCreatureTypes.contains(type.getName())) return true;
        
        // Prevent on all safe materials.
        if (safeMaterial.contains(spawningAt.getBlock().getRelative(BlockFace.DOWN).getType().name())) return false;
        
        // Prevent if distance from player is within the safety radius.
        for (Player player : spawningAt.getWorld().getPlayers()) {
            double distance = this.distanceBetween(spawningAt, player.getLocation());
            Main.messageManager.log("Spawn detected " + distance + "m from " + player.getName(), MessageLevel.FINEST);
            if (distance >= 0 && distance <= this.safeRadius) {
                Main.messageManager.log("Unsafe spawn at " + distance + "m near " + player.getName(), MessageLevel.FINE);
                //return false;
            }
        }
        
        // Passed all the checks, allow spawn by default.
        return true;
    }
    
    /**
     * Long live Pythagoras!
     */
    private double distanceBetween(Location locA, Location locB) {
        if (locA == null || locB == null) return -1;
        
        // d = sqrt( (xA-xB)^2 + (yA-yB)^2 + (zA-zB)^2 )       
        return Math.sqrt(
                  Math.pow(locA.getX() - locB.getX(), 2)
                + Math.pow(locA.getY() - locB.getY(), 2)
                + Math.pow(locA.getZ() - locB.getZ(), 2)
        );
    }
}
