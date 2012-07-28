package edgruberman.bukkit.creaturepolicy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * Updates publisher managed policies based on world loading/unloading
 */
public class Editor implements Listener {

    /**
     * Base path, relative to plugin data folder, to look for world specific
     * configuration overrides in. Sub-folder is used to avoid conflicts
     * between world names and future possible configuration folder names.
     */
    private static final String WORLD_OVERRIDES = "Worlds";

    private final Publisher publisher;
    private final Policy defaultPolicy;
    private final List<String> exclude = new ArrayList<String>();

    public Editor(final Publisher publisher, final ConfigurationSection defaultPolicy, final List<String> exclude) {
        this.publisher = publisher;
        this.defaultPolicy = new Policy(publisher, defaultPolicy);
        if (exclude != null) this.exclude.addAll(exclude);

        for (final World world : publisher.plugin.getServer().getWorlds())
            this.loadPolicy(world);
    }

    public void loadPolicy(final World world) {
        if (this.exclude.contains(world.getName())) {
            this.publisher.plugin.getLogger().config("Excluding policy load for world: " + world.getName());
            return;
        }

        Policy policy = this.defaultPolicy;
        final File worldFile = new File(this.publisher.plugin.getDataFolder() + File.separator + Editor.WORLD_OVERRIDES + File.separator + world.getName(), "config.yml");
        if (worldFile.exists()) policy = new Policy(this.publisher, YamlConfiguration.loadConfiguration(worldFile));
        this.publisher.policies.put(world, policy);
        this.publisher.plugin.getLogger().config("Loaded policy for world [" + world.getName() + "]: " + policy.toString());
    }

    public void clear() {
        HandlerList.unregisterAll(this);
        this.defaultPolicy.clear();
        this.exclude.clear();
    }

    @EventHandler
    public void onWorldLoad(final WorldLoadEvent event) {
        this.loadPolicy(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(final WorldUnloadEvent event) {
        this.publisher.policies.remove(event.getWorld());
    }

}
