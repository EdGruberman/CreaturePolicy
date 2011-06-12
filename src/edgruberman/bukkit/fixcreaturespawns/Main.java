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

    public static MessageManager messageManager;
    
    private List<String> unsafeCreatureTypes = new ArrayList<String>();
    private List<String> safeMaterial = new ArrayList<String>();
    private int safeRadius;
    
    public void onLoad() {
        Configuration.load(this);
    }
	
    public void onEnable() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        this.unsafeCreatureTypes = this.getConfiguration().getStringList("unsafeCreatureTypes", this.unsafeCreatureTypes);
        Main.messageManager.log(MessageLevel.CONFIG, "Unsafe Creature Types: " + this.unsafeCreatureTypes);
        
        this.safeMaterial = this.getConfiguration().getStringList("safeMaterial", this.safeMaterial);
        Main.messageManager.log(MessageLevel.CONFIG, "Safe Material: " + this.safeMaterial);
        
        PluginManager pluginManager = this.getServer().getPluginManager();
        EntityListener entityListener = new EntityListener(this);
        pluginManager.registerEvent(Event.Type.CREATURE_SPAWN, entityListener, Event.Priority.Normal, this);

        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        // TODO: Add plugin disable code here.
        
        Main.messageManager.log("Plugin Disabled");
        Main.messageManager = null;
    }
    
    protected boolean isAllowedSpawn(CreatureType type, Location spawningAt) {
        // Only prevent unsafe creatures.
        if (!unsafeCreatureTypes.contains(type.getName())) return true;
        
        // Prevent on all safe materials.
        if (safeMaterial.contains(spawningAt.getBlock().getRelative(BlockFace.DOWN).getType().name())) return false;
        
        // Prevent if distance from player is within the safety radius.
        for (Player player : spawningAt.getWorld().getPlayers()) {
            double distance = this.distanceBetween(spawningAt, player.getLocation());
            Main.messageManager.log(MessageLevel.FINEST
                    , "Spawn detected " + distance + "m from " + player.getName()
            );
            if (distance >= 0 && distance <= this.safeRadius) {
                Main.messageManager.log(MessageLevel.FINE
                        , "Unsafe spawn at " + distance + "m near " + player.getName()
                );
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
