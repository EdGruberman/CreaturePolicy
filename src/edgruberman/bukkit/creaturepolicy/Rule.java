package edgruberman.bukkit.creaturepolicy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.CreatureSpawnEvent;

import edgruberman.bukkit.creaturepolicy.rules.DefaultRule;

/** criteria for allowing or denying a creature spawn */
public abstract class Rule {

    // ---- static factory ----

    public static final String DEFAULT_PACKAGE = Rule.class.getPackage().getName() + ".rules";

    public static Rule create(final Policy policy, final ConfigurationSection definition) throws ClassNotFoundException, ClassCastException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        final Class<? extends Rule> subClass = Rule.find(definition.getString("class"));
        final Constructor<? extends Rule> ctr = subClass.getConstructor(Policy.class, ConfigurationSection.class);
        return ctr.newInstance(policy, definition);
    }

    public static Class<? extends Rule> find(final String className) throws ClassNotFoundException, ClassCastException {
        // look in default package first
        try {
            return Class.forName(Rule.DEFAULT_PACKAGE + "." + className).asSubclass(Rule.class);
        } catch (final Exception e) {
            // ignore to try searching for custom class next
        }

        // look for a custom class
        return Class.forName(className).asSubclass(Rule.class);
    }



    // ---- instance ----

    public final Policy policy;
    public final String name;
    protected final boolean allow;

    protected Rule(final Policy policy, final ConfigurationSection config) {
        this.policy = policy;
        this.name = config.getName();
        this.allow = config.getBoolean("allow", (this instanceof DefaultRule ? true : !policy.isDefaultAllowed())); // Assume exception
    }

    public void clear() {}

    public boolean isApplicable(final CreatureSpawnEvent event) {
        return true;
    }

    public boolean isAllowed() {
        return this.allow;
    }

    @Override
    public String toString() {
        return this.toString(null);
    }

    protected String toString(final String custom) {
        final String name = (this.getClass().getPackage().getName().equals(Rule.DEFAULT_PACKAGE) ? this.getClass().getSimpleName() : this.getClass().getName());
        return String.format("%1$s: [allow: " + this.allow + "%2$s]", name, (custom != null ? "; " + custom : ""));
    }

}
