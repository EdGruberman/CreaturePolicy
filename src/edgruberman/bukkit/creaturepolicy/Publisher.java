package edgruberman.bukkit.creaturepolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;

/** Applies policies to worlds */
public class Publisher implements Listener {

    public final Plugin plugin;

    private final ConfigurationSection defaults;
    private final List<String> exclude = new ArrayList<String>();
    private final Map<World, Policy> policies = new HashMap<World, Policy>();

    Publisher(final Plugin plugin, final ConfigurationSection defaults, final List<String> exclude) {
        this.plugin = plugin;
        this.defaults = defaults;
        this.exclude.addAll(exclude);

        for (final World world : Bukkit.getServer().getWorlds())
            this.loadPolicy(world);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadPolicy(final World world) {
        if (this.exclude.contains(world.getName())) {
            this.plugin.getLogger().config("Excluding creature spawn policy management for world: " + world.getName());
            return;
        }

        ConfigurationSection specific = this.plugin.getConfig().getConfigurationSection("policy." + world.getName());
        if (specific == null || !specific.getString("type", "world").equals("world")) specific = new MemoryConfiguration();

        final Configuration rules = new MemoryConfiguration();
        for (final Map.Entry<String, Object> d : this.defaults.getValues(true).entrySet()) rules.set(d.getKey(), d.getValue());
        for (final Map.Entry<String, Object> s : specific.getValues(true).entrySet()) rules.set(s.getKey(), s.getValue());

        final Policy policy = new Policy(this, rules);
        this.policies.put(world, policy);
        this.plugin.getLogger().config("Loaded policy for world [" + world.getName() + "]: " + policy.toString());
    }

    void clear() {
        HandlerList.unregisterAll(this);
        for (final Policy policy : this.policies.values()) policy.clear();
        this.policies.clear();
        this.exclude.clear();
    }

    @EventHandler
    public void onWorldLoad(final WorldLoadEvent event) {
        this.loadPolicy(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(final WorldUnloadEvent event) {
        this.policies.remove(event.getWorld());
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(final CreatureSpawnEvent event) {
        final Policy policy = this.policies.get(event.getLocation().getWorld());
        if (policy == null) return;

        if (policy.isAllowed(event)) return;

        if (this.plugin.getLogger().isLoggable(Level.FINEST)) {
            this.plugin.getLogger().finest(
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
