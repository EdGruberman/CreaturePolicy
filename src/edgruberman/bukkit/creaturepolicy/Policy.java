package edgruberman.bukkit.creaturepolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * List of rules that define allowable creature spawns.
 */
public final class Policy {

    public static final boolean DEFAULT_DEFAULT_ALLOW = true;

    public final String internalRules = this.getClass().getPackage().getName() + ".rules";
    public final Publisher publisher;

    /**
     * Default allow action to apply if no rules are applicable.
     */
    private boolean defaultAllow = Policy.DEFAULT_DEFAULT_ALLOW;
    private final List<Rule> rules = new ArrayList<Rule>();

    Policy(final Publisher publisher, final ConfigurationSection config) {
        this.publisher = publisher;
        this.load(config);
    }

    public boolean isDefaultAllowed() {
        return this.defaultAllow;
    }

    public void clear() {
        for (final Rule rule : this.rules) rule.clear();
        this.rules.clear();
    }

    /**
     * First applicable rule defines if spawn is allowed or denied.  If no
     * rule applies, policy default is returned.
     */
    boolean isAllowed(final CreatureSpawnEvent event) {
        for (final Rule rule : this.rules) {
            if (rule.isApplicable(event)) {
                final boolean allow = rule.isAllowed(event);
                if (this.publisher.plugin.getLogger().isLoggable(Level.FINER)) this.publisher.plugin.getLogger().finer("Allow: " + allow + "; " + rule.toString());
                return allow;
            }
        }

        return this.defaultAllow;
    }

    private void load(final ConfigurationSection config) {
        if (config == null) return;

        this.defaultAllow = config.getBoolean("defaultAllow", this.defaultAllow);
        final ConfigurationSection rulesConfig = config.getConfigurationSection("rules");
        if (rulesConfig == null) return;

        for (final String ruleName : rulesConfig.getKeys(false)) {
            final ConfigurationSection ruleConfig = rulesConfig.getConfigurationSection(ruleName);
            String type = ruleConfig.getString("type");
            if (type == null) {
                this.publisher.plugin.getLogger().warning("Rule missing type: " + ruleName);
                continue;
            }

            if (!type.contains(".")) type = this.internalRules + '.' + type;

            Rule rule;
            try {
                rule = (Rule) Class.forName(type).newInstance();
            } catch (final Exception e) {
                this.publisher.plugin.getLogger().warning("Unable to instantiate rule: " + type + "; " + e.getClass().getName() + ": " + e.getMessage());
                continue;
            }

            rule.policy = this;
            rule.load(ruleConfig);
            this.rules.add(rule);
        }

        return;
    }

    @Override
    public String toString() {
        return "Policy: [defaultAllow: " + this.defaultAllow + "; Rules(" + this.rules.size() + "): " + this.rules.toString() + "]";
    }
}