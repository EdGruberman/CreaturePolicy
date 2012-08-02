package edgruberman.bukkit.creaturepolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.CreatureSpawnEvent;

import edgruberman.bukkit.creaturepolicy.rules.DefaultRule;
import edgruberman.bukkit.creaturepolicy.rules.Rule;

/** Collection of rules that define whether to allow or deny a creature spawn */
public final class Policy {

    public final Publisher publisher;

    /** Default rule to apply when no other rules are applicable */
    private Rule defaultRule = new DefaultRule(this);
    private final List<Rule> rules = new ArrayList<Rule>();

    Policy(final Publisher publisher, final ConfigurationSection policyRules) {
        this.publisher = publisher;

        for (final String key : policyRules.getKeys(false)) {
            if (key.equals(DefaultRule.NAME) && policyRules.isBoolean(key)) {
                this.defaultRule = new DefaultRule(this, policyRules.getBoolean(key));
                continue;
            }

            final ConfigurationSection ruleConfig = policyRules.getConfigurationSection(key);
            if (!ruleConfig.getBoolean("enabled")) continue;

            Rule rule;
            try {
                rule = Rule.create(this, ruleConfig);
            } catch (final Exception e) {
                this.publisher.plugin.getLogger().warning("Unable to create rule '" + ruleConfig.getName() + "'; " + e.getClass().getName() + "; " + e.getMessage());
                continue;
            }

            if (rule instanceof DefaultRule) {
                this.defaultRule = rule;
            } else {
                this.rules.add(rule);
            }
        }
    }

    void clear() {
        for (final Rule rule : this.rules) rule.clear();
        this.rules.clear();
    }

    public boolean isDefaultAllowed() {
        return this.defaultRule.isAllowed();
    }

    /**
     * First applicable rule defines if spawn is allowed or denied;
     * If no rule applies, policy default is returned
     */
    public boolean isAllowed(final CreatureSpawnEvent event) {
        for (final Rule rule : this.rules) {
            if (!rule.isApplicable(event)) continue;

            if (this.publisher.plugin.getLogger().isLoggable(Level.FINER))
                this.publisher.plugin.getLogger().finer("Applicable Rule: " + rule.toString());

            return rule.isAllowed();
        }

        return this.defaultRule.isAllowed();
    }

    @Override
    public String toString() {
        return "Policy: [Default Rule: [" + this.defaultRule.toString() + "], Rules(" + this.rules.size() + "): " + this.rules.toString() + "]";
    }
}