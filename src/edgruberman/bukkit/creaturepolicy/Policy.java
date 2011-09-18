package edgruberman.bukkit.creaturepolicy;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.CreatureType;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * List of rules that control creature spawns.
 */
final class Policy {
    
    World world;
    Rule defaultRule = new Rule();
    Map<CreatureType, Rule> rules = new HashMap<CreatureType, Rule>();
    
    Policy(final World world) {
        this.world = world;
    }
    
    void add(final Rule rule) {
        if (rule.type == null) {
            this.defaultRule = rule;
            return;
        }
        
        this.rules.put(rule.type, rule);
    }
    
    boolean isAllowed(final CreatureSpawnEvent event) {
        Rule rule = this.rules.get(event.getCreatureType());
        if (rule != null) return rule.isAllowed(event);
        
        return defaultRule.isAllowed(event);
    }
}