package edgruberman.bukkit.creaturepolicy;

import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.channels.Channel;

final class Enforcer extends EntityListener {
    
    Enforcer(final Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvent(Event.Type.CREATURE_SPAWN, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        
        if (Publisher.policies.get(event.getLocation().getWorld()).isAllowed(event)) return;
        
        if (Main.messageManager.isLevel(Channel.Type.LOG, MessageLevel.FINE)) {
            Main.messageManager.log(
                    "Cancelling " + event.getSpawnReason().name() + " " + event.getCreatureType().getName()
                        + " spawn in [" + event.getLocation().getWorld().getName() + "] at"
                        + " x: " + event.getLocation().getBlockX()
                        + " y: " + event.getLocation().getBlockY()
                        + " z: " + event.getLocation().getBlockZ()
                    , MessageLevel.FINE
            );
        }
        event.setCancelled(true);
    }
}