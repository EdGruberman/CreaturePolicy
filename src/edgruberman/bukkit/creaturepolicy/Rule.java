package edgruberman.bukkit.creaturepolicy;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Criteria for allowing or denying a creature spawn.
 */
public abstract class Rule {

    protected Policy policy;
    protected boolean allow;

    public void load(final ConfigurationSection config) {
        this.allow = !this.policy.isDefaultAllowed();  // Assume exception
        if (config == null) return;

        this.allow = config.getBoolean("allow", this.allow);
    }

    public abstract boolean isApplicable(final CreatureSpawnEvent event);

    public boolean isAllowed(final CreatureSpawnEvent event) {
        return this.allow;
    }

    public void clear() {
        this.policy = null;
        return;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "; Allow: " + this.allow;
    }

}
