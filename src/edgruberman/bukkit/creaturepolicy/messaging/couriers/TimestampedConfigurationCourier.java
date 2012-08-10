package edgruberman.bukkit.creaturepolicy.messaging.couriers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import edgruberman.bukkit.creaturepolicy.messaging.Message;
import edgruberman.bukkit.creaturepolicy.messaging.Recipients;
import edgruberman.bukkit.creaturepolicy.messaging.messages.TimestampedConfigurationMessage;

public class TimestampedConfigurationCourier extends ConfigurationCourier {

    public TimestampedConfigurationCourier(final Plugin plugin, final String basePath) {
        super(plugin, basePath);
    }

    public TimestampedConfigurationCourier(final Plugin plugin) {
        super(plugin);
    }

    public TimestampedConfigurationCourier(final Plugin plugin, final ConfigurationSection base) {
        super(plugin, base);
    }

    @Override
    public void deliver(final Recipients recipients, final String path, final Object... args) {
        for (final Message message : TimestampedConfigurationMessage.create(this.getBase(), path, args))
            this.deliver(recipients, message);
    }

}