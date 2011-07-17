package edgruberman.bukkit.fixcreaturespawns;

import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;

import edgruberman.bukkit.messagemanager.MessageLevel;

public class EntityListener extends org.bukkit.event.entity.EntityListener {
    
    public EntityListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvent(Event.Type.CREATURE_SPAWN, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        
        if (Main.isAllowedSpawn(event)) return;
        
        Main.messageManager.log(
                "Cancelling " + event.getCreatureType().getName()
                    + " spawn in \"" + event.getLocation().getWorld().getName() + "\" at"
                    + " x: " + event.getLocation().getBlockX()
                    + " y: " + event.getLocation().getBlockY()
                    + " z: " + event.getLocation().getBlockZ()
                    + " on " + event.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().name()
                , MessageLevel.FINER
        );
        event.setCancelled(true);
    }
}