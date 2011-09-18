package edgruberman.bukkit.creaturepolicy;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.CreatureType;
import org.bukkit.event.entity.CreatureSpawnEvent;

import edgruberman.bukkit.messagemanager.MessageLevel;

/**
 * Criteria for controlling a spawn.
 */
final class Rule {
    
    static final boolean DEFAULT_ALLOW = true;
    
    CreatureType type;
    boolean allow;
    List<Exception> exceptions = new ArrayList<Exception>();
    
    Rule() {
        this.type = null;
        this.allow = Rule.DEFAULT_ALLOW;
    }
    
    Rule(final CreatureType type, final boolean allow) {
        this.type = type;
        this.allow = allow;
    }
    
    boolean isAllowed(final CreatureSpawnEvent event) {
        for (Exception e : exceptions)
            if (e.isMatch(event)) {
                Main.messageManager.log("Exception match for " + this.type + " as " + e, MessageLevel.FINER);
                return !allow;
            }
        
        return allow;
    }
    
    @Override
    public String toString() {
        String ex = "";
        for (Exception e : this.exceptions) {
            if (!ex.equals("")) ex += "; ";
            ex += e;
        }
        
        return (this.type != null ? "Creature: " + this.type : "default") + "; Allow: " + this.allow + "; Exceptions: [" + ex + "]"; 
    }
}