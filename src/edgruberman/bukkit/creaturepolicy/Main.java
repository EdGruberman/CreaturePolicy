package edgruberman.bukkit.creaturepolicy;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import edgruberman.bukkit.creaturepolicy.commands.Reload;
import edgruberman.bukkit.creaturepolicy.messaging.ConfigurationCourier;
import edgruberman.bukkit.creaturepolicy.util.CustomPlugin;

public final class Main extends CustomPlugin {

    public static ConfigurationCourier courier;

    private Publisher publisher;

    @Override
    public void onLoad() { this.putConfigMinimum(CustomPlugin.CONFIGURATION_FILE, "5.0.0a0"); }

    @Override
    public void onEnable() {
        this.reloadConfig();

        Main.courier = ConfigurationCourier.create(this).setColorCode("color-code").build();
        this.publisher = new Publisher(this, this.getDefaultRules(), this.getConfig().getStringList("exclude"));

        this.getCommand("creaturepolicy:reload").setExecutor(new Reload(this));
    }

    @Override
    public void onDisable() {
        this.publisher.clear(); this.publisher = null;
        Main.courier = null;
    }

    private ConfigurationSection getDefaultRules() {
        final ConfigurationSection defaults = this.getConfig().getConfigurationSection("policy.defaults");
        if (defaults != null && defaults.getString("type", "default").equals("default")) return defaults;

        final ConfigurationSection policies = this.getConfig().getConfigurationSection("policy");
        if (policies != null)
            for (final String name : policies.getKeys(false)) {
                final ConfigurationSection policy = policies.getConfigurationSection(name);
                if (policy.getString("type", "world").equals("default")) return policy;
            }

        return new MemoryConfiguration();
    }

}
