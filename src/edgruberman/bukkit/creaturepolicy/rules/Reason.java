package edgruberman.bukkit.creaturepolicy.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import edgruberman.bukkit.creaturepolicy.Policy;
import edgruberman.bukkit.creaturepolicy.Rule;

/** applicability determined by spawn reason and creature type */
public class Reason extends Rule {

    /** all reasons */
    public final static List<SpawnReason> DEFAULT_REASONS = Collections.emptyList();

    /** all creatures */
    public final static List<EntityType> DEFAULT_CREATURES = Collections.emptyList();



    protected final List<SpawnReason> reasons = new ArrayList<SpawnReason>();
    protected final List<EntityType> creatures = new ArrayList<EntityType>();

    public Reason(final Policy policy, final ConfigurationSection config) {
        super(policy, config);

        if (config.isList("reasons"))
            for (final String reason : config.getStringList("reasons"))
                this.reasons.add(SpawnReason.valueOf(reason));

        if (config.isList("creatures"))
            for (final String entity : config.getStringList("creatures"))
                this.creatures.add(EntityType.valueOf(entity));
    }

    @Override
    public boolean isApplicable(final CreatureSpawnEvent event) {
        return (this.reasons.size() == 0 || this.reasons.contains(event.getSpawnReason()))
                && (this.creatures.size() == 0 || this.creatures.contains(event.getEntityType()));
    }

    @Override
    public String toString() {
        return super.toString("reasons: " + this.reasons + "; creatures: " + this.creatures);
    }

}
