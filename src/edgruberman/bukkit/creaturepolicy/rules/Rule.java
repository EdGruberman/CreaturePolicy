package edgruberman.bukkit.creaturepolicy.rules;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.CreatureSpawnEvent;

import edgruberman.bukkit.creaturepolicy.Policy;

/**
 * Criteria for allowing or denying a creature spawn
 */
public abstract class Rule {

    // ---- Static Factory ----

    public static Rule create(final Policy policy, final ConfigurationSection definition) throws ClassNotFoundException, ClassCastException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        final Class<? extends Rule> subClass = Rule.find(definition.getString("class"));
        final Constructor<? extends Rule> ctr = subClass.getConstructor(Policy.class, ConfigurationSection.class);
        return ctr.newInstance(policy, definition);
    }

    public static Class<? extends Rule> find(final String className) throws ClassNotFoundException, ClassCastException {
        // Look in local package first
        try {
            return Class.forName(Rule.class.getPackage().getName() + "." + className).asSubclass(Rule.class);
        } catch (final Exception e) {
            // Ignore to try searching for custom class next
        }

        // Look for a custom class
        return Class.forName(className).asSubclass(Rule.class);
    }



    // ---- Instance ----

    public final Policy policy;
    public final String name;
    protected boolean allow;

    protected Rule(final Policy policy, final ConfigurationSection config) {
        this.policy = policy;
        this.name = config.getName();
        this.allow = config.getBoolean("allow", (this instanceof DefaultRule ? true : !this.policy.isDefaultAllowed())); // Assume exception
    }

    public void clear() {}

    public boolean isApplicable(final CreatureSpawnEvent event) {
        return true;
    }

    public final boolean isAllowed() {
        return this.allow;
    }

    @Override
    public String toString() {
        return this.toString(null);
    }

    protected String toString(final String custom) {
        final String name = (this.getClass().getPackage().equals(Rule.class.getPackage()) ? this.getClass().getSimpleName() : this.getClass().getName());
        return String.format("%1$s: [allow: " + this.allow + "%2$s]", name, (custom != null ? "; " + custom : ""));
    }

}
