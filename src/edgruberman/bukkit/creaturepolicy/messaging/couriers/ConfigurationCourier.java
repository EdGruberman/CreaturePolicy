package edgruberman.bukkit.creaturepolicy.messaging.couriers;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.creaturepolicy.messaging.Courier;
import edgruberman.bukkit.creaturepolicy.messaging.Message;
import edgruberman.bukkit.creaturepolicy.messaging.Recipients;
import edgruberman.bukkit.creaturepolicy.messaging.messages.ConfigurationMessage;
import edgruberman.bukkit.creaturepolicy.messaging.recipients.PermissionSubscribers;
import edgruberman.bukkit.creaturepolicy.messaging.recipients.Sender;
import edgruberman.bukkit.creaturepolicy.messaging.recipients.ServerPlayers;
import edgruberman.bukkit.creaturepolicy.messaging.recipients.WorldPlayers;

public class ConfigurationCourier extends Courier {

    protected final String basePath;
    protected final ConfigurationSection base;

    public ConfigurationCourier(final Plugin plugin, final String basePath) {
        super(plugin);
        this.basePath = basePath;
        this.base = null;
    }

    public ConfigurationCourier(final Plugin plugin) {
        this(plugin, (String) null);
    }

    public ConfigurationCourier(final Plugin plugin, final ConfigurationSection base) {
        super(plugin);
        this.basePath = null;
        this.base = base;
    }

    public ConfigurationSection getBase() {
        if (this.base != null)
            return this.base;

        if (this.basePath != null)
            return this.plugin.getConfig().getConfigurationSection(this.basePath);

        return this.plugin.getConfig();
    }

    public String format(final String path, final Object... args) {
        return String.format(this.getBase().getString(path), args);
    }

    public void deliver(final Recipients recipients, final String path, final Object... args) {
        for (final Message message : ConfigurationMessage.create(this.getBase(), path, args))
            this.deliver(recipients, message);
    }

    @Override
    public void send(final CommandSender sender, final String path, final Object... args) {
        final Recipients recipients = new Sender(sender);
        this.deliver(recipients, path, args);
    }

    @Override
    public void broadcast(final String path, final Object... args) {
        final Recipients recipients = new ServerPlayers();
        this.deliver(recipients, path, args);
    }

    @Override
    public void world(final World world, final String path, final Object... args) {
        final Recipients recipients = new WorldPlayers(world);
        this.deliver(recipients, path, args);
    }

    @Override
    public void publish(final String permission, final String path, final Object... args) {
        final Recipients recipients = new PermissionSubscribers(permission);
        this.deliver(recipients, path, args);
    }

}