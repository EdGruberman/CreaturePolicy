package edgruberman.bukkit.creaturepolicy.rules;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import edgruberman.bukkit.creaturepolicy.Policy;
import edgruberman.bukkit.creaturepolicy.Rule;

/** always applicable */
public class DefaultRule extends Rule {

    public static final String NAME = "allow";

    private static final ConfigurationSection TRUE = new MemoryConfiguration().createSection(DefaultRule.NAME);
    private static final ConfigurationSection FALSE = new MemoryConfiguration().createSection(DefaultRule.NAME);

    static {
        DefaultRule.TRUE.set("allow", true);
        DefaultRule.FALSE.set("allow", false);
    }

    /** create rule that is always applicable and always allows spawns */
    public DefaultRule(final Policy policy) {
        this(policy, DefaultRule.TRUE);
    }

    /** create rule that is always applicable and allows spawns as supplied */
    public DefaultRule(final Policy policy, final boolean allow) {
        this(policy, (allow ? DefaultRule.TRUE : DefaultRule.FALSE));
    }

    /** create rule that is always applicable and allows spawns as defined in configuration */
    public DefaultRule(final Policy policy, final ConfigurationSection config) {
        super(policy, config);
    }

}
