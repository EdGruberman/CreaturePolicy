package edgruberman.bukkit.creaturepolicy;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.CreatureSpawnEvent;

import edgruberman.bukkit.creaturepolicy.rules.DefaultRule;

/** collection of rules that define whether to allow or deny a creature spawn */
public final class Policy {

    public final Publisher publisher;

    /** default rule to apply when no other rules are applicable */
    private Rule defaultRule = new DefaultRule(this);
    private final List<Rule> rules = new ArrayList<Rule>();

    Policy(final Publisher publisher, final ConfigurationSection policyRules) {
        this.publisher = publisher;

        for (final String key : policyRules.getKeys(false)) {
            if (key.equals(DefaultRule.NAME) && policyRules.isBoolean(key)) {
                this.defaultRule = new DefaultRule(this, policyRules.getBoolean(key));
                continue;
            }

            Rule rule;
            try {
                rule = Rule.create(this, policyRules.getConfigurationSection(key));
            } catch (final InvocationTargetException e) {
                this.publisher.plugin.getLogger().warning("Unable to create rule '" + key + "'; " + e.getCause());
                continue;
            } catch (final Exception e) {
                this.publisher.plugin.getLogger().warning("Unable to create rule '" + key + "'; " + e);
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

    /** first applicable rule defines if spawn is allowed or denied; if no rule applies, policy default is returned */
    public boolean isAllowed(final CreatureSpawnEvent event) {
        for (final Rule rule : this.rules) {
            if (!rule.isApplicable(event)) continue;

            this.publisher.plugin.getLogger().log(Level.FINEST, "Applicable Rule: {0}", rule);
            return rule.isAllowed();
        }

        return this.defaultRule.isAllowed();
    }

    @Override
    public String toString() {
        return "Policy: [" + this.defaultRule + ", Rules(" + this.rules.size() + "): " + this.rules.toString() + "]";
    }

}
