package edgruberman.bukkit.fixcreaturespawns;

import org.bukkit.event.Event;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

final class WorldMonitor extends WorldListener {
    
    WorldMonitor(final Plugin plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.WORLD_LOAD, this, Event.Priority.Monitor, plugin);
        pluginManager.registerEvent(Event.Type.WORLD_UNLOAD, this, Event.Priority.Monitor, plugin);
    }
    
    @Override
    public void onWorldLoad(final WorldLoadEvent event) {
        Main.loadBlacklist(event.getWorld(), new SpawnBlacklist(event.getWorld()));
    }
    
    @Override
    public void onWorldUnload(final WorldUnloadEvent event) {
        SpawnBlacklist.worlds.remove(event.getWorld());
    }
}