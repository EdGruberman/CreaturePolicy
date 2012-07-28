package edgruberman.bukkit.creaturepolicy;

import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private Publisher publisher;
    private Editor editor;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.setLogLevel(this.getConfig().getString("logLevel"));
        this.publisher = new Publisher(this);
        this.editor = new Editor(this.publisher, this.getConfig().getConfigurationSection("defaultPolicy"), this.getConfig().getStringList("exclude"));
    }

    @Override
    public void onDisable() {
        this.editor.clear();
        this.editor = null;

        this.publisher.clear();
        this.publisher = null;
    }

    private void setLogLevel(final String name) {
        Level level;
        try { level = Level.parse(name); } catch (final Exception e) {
            level = Level.INFO;
            this.getLogger().warning("Log level defaulted to " + level.getName() + "; Unrecognized java.util.logging.Level: " + name);
        }

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().config("Log level set to: " + this.getLogger().getLevel());
    }

}
