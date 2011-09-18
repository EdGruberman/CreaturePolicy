package edgruberman.bukkit.creaturepolicy;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

final class Publisher extends WorldListener {
    
    static Map<World, Policy> policies = new HashMap<World, Policy>();
    
    static void publish(final World world) {
        Policy policy = new Policy(world);
        Main.loadPolicy(policy);
        Publisher.policies.put(world, policy);
    }
    
    Publisher(final Plugin plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.WORLD_LOAD, this, Event.Priority.Monitor, plugin);
        pluginManager.registerEvent(Event.Type.WORLD_UNLOAD, this, Event.Priority.Monitor, plugin);
    }
    
    @Override
    public void onWorldLoad(final WorldLoadEvent event) {
        Publisher.publish(event.getWorld());
    }
    
    @Override
    public void onWorldUnload(final WorldUnloadEvent event) {
        Publisher.policies.remove(event.getWorld());
    }
}