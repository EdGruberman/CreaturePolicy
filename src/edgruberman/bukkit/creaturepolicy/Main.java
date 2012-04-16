package edgruberman.bukkit.creaturepolicy;

import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private Publisher publisher;
    private Editor editor;

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.setLoggingLevel(this.getConfig().getString("logLevel", "INFO"));
        this.start(this, this.getConfig());
    }

    @Override
    public void onDisable() {
        this.editor.clear();
        this.editor = null;

        this.publisher.clear();
        this.publisher = null;
    }

    public void start(final Plugin context, final ConfigurationSection config) {
        this.publisher = new Publisher(context);
        this.editor = new Editor(this.publisher, config.getConfigurationSection("defaultPolicy"), config.getStringList("exclude"));
    }

    private void setLoggingLevel(final String name) {
        Level level;
        try { level = Level.parse(name); } catch (final Exception e) {
            level = Level.INFO;
            this.getLogger().warning("Defaulting to " + level.getName() + "; Unrecognized java.util.logging.Level: " + name);
        }

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().config("Logging level set to: " + this.getLogger().getLevel());
    }

}
