package edgruberman.bukkit.creaturepolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;

/**
 * Applies policies to worlds
 */
public final class Publisher implements Listener {

    public final Plugin plugin;

    public final Map<World, Policy> policies = new HashMap<World, Policy>();

    public Publisher(final Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void clear() {
        HandlerList.unregisterAll(this);
        for (final Policy policy : this.policies.values()) policy.clear();
        this.policies.clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        final Policy policy = this.policies.get(event.getLocation().getWorld());
        if (policy == null) return;

        if (policy.isAllowed(event)) return;

        if (this.plugin.getLogger().isLoggable(Level.FINE)) {
            this.plugin.getLogger().fine(
                    "Cancelling " + event.getSpawnReason().name() + " " + event.getEntityType().getName()
                        + " spawn in [" + event.getLocation().getWorld().getName() + "] at"
                        + " x: " + event.getLocation().getBlockX()
                        + " y: " + event.getLocation().getBlockY()
                        + " z: " + event.getLocation().getBlockZ()
            );
        }
        event.setCancelled(true);
    }

}
