package edgruberman.bukkit.creaturepolicy.rules;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import edgruberman.bukkit.creaturepolicy.Rule;

/**
 * Spawn criteria of creature type and spawn reason only.
 */
public class ReasonRule extends Rule {

    protected final List<EntityType> entities = new ArrayList<EntityType>();
    protected final List<SpawnReason> reasons = new ArrayList<SpawnReason>();

    @Override
    public void load(final ConfigurationSection config) {
        if (config == null) return;

        super.load(config);

        if (config.isList("entities"))
            for (final String entity : config.getStringList("entities"))
                this.entities.add(EntityType.valueOf(entity));

        if (config.isList("reasons"))
            for (final String reason : config.getStringList("reasons"))
                this.reasons.add(SpawnReason.valueOf(reason));
    }

    @Override
    public boolean isApplicable(final CreatureSpawnEvent event) {
        return this.reasons.contains(event.getSpawnReason()) && this.entities.contains(event.getEntityType());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": [entities: " + this.entities + "; reasons: " + this.reasons + "]";
    }

}
