package edgruberman.bukkit.fixcreaturespawns;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.channels.Channel;

final class SpawnCanceller extends EntityListener {
    
    SpawnCanceller(final Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvent(Event.Type.CREATURE_SPAWN, this, Event.Priority.Normal, plugin);
    }
    
    @Override
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        
        if (this.isAllowed(event)) return;
        
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
    
    boolean isAllowed(final CreatureSpawnEvent event) {
        SpawnBlacklist blacklist = SpawnBlacklist.worlds.get(event.getLocation().getWorld());
        
        // No blacklist entries allows all spawns
        if (blacklist.creatures.size() == 0) return true;
        
        // Deny spawns matching a blacklisted definition
        List<SpawnDefinition> definitions = blacklist.creatures.get(event.getCreatureType());
        if (definitions != null)
            for (SpawnDefinition def : definitions)
                if (def.isMatch(event)) {
                    Main.messageManager.log("Blacklisted spawn definition detected: " + def, MessageLevel.FINER);
                    return false;
                }
        
        // Deny spawn if distance from player is within a defined safety radius.
        if (blacklist.safeRadiusSquared >= 0) {
            for (Player player : event.getLocation().getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(event.getLocation()) <= blacklist.safeRadiusSquared) {
                    Main.messageManager.log("Unsafe " + event.getSpawnReason() + " spawn"
                            + " at " + (int) Math.sqrt(player.getLocation().distanceSquared(event.getLocation())) + "m"
                            + " near " + player.getName(), MessageLevel.FINER
                    );
                    return false;
                }
            }
        }
        
        // Passed all the checks, allow spawn.
        return true;
    }
}