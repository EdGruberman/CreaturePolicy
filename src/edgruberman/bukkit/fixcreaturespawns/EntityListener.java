package edgruberman.bukkit.fixcreaturespawns;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.material.MaterialData;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.channels.Channel;

class EntityListener extends org.bukkit.event.entity.EntityListener {
    
    EntityListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvent(Event.Type.CREATURE_SPAWN, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        
        if (Main.isAllowedSpawn(event)) return;
        
        if (Main.messageManager.isLevel(Channel.Type.LOG, MessageLevel.FINER)) {
            Block block = event.getLocation().getBlock().getRelative(BlockFace.DOWN);
            Main.messageManager.log(
                    "Cancelling " + event.getCreatureType().getName()
                        + " spawn in \"" + event.getLocation().getWorld().getName() + "\" at"
                        + " x: " + event.getLocation().getBlockX()
                        + " y: " + event.getLocation().getBlockY()
                        + " z: " + event.getLocation().getBlockZ()
                        + " on " + new MaterialData(block.getType(), block.getData())
                    , MessageLevel.FINER
            );
        }
        event.setCancelled(true);
    }
}