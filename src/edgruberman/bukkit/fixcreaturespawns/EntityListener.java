package edgruberman.bukkit.fixcreaturespawns;

import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.CreatureSpawnEvent;

import edgruberman.bukkit.messagemanager.MessageLevel;

public class EntityListener extends org.bukkit.event.entity.EntityListener {
    
    private Main plugin;
    
    public EntityListener(Main plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        
        
        if (this.plugin.isAllowedSpawn(event.getCreatureType(), event.getLocation())) return;
        
        Main.messageManager.log(MessageLevel.FINER, "Cancelling " + event.getCreatureType().getName()
                + " spawn in \"" + event.getLocation().getWorld().getName() + "\" at"
                + " x: " + event.getLocation().getBlockX()
                + " y: " + event.getLocation().getBlockY()
                + " z: " + event.getLocation().getBlockZ()
                + " on " + event.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().name()
        );
        event.setCancelled(true);
    }
}
