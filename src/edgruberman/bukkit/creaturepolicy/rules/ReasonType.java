package edgruberman.bukkit.creaturepolicy.rules;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import edgruberman.bukkit.creaturepolicy.Rule;

/**
 * Applicability determined by spawn reason and creature type
 */
public class ReasonType extends Rule {

    protected final List<SpawnReason> reasons = new ArrayList<SpawnReason>();
    protected final List<EntityType> types = new ArrayList<EntityType>();

    @Override
    public void load(final ConfigurationSection config) {
        if (config == null) return;

        super.load(config);

        if (config.isList("reasons"))
            for (final String reason : config.getStringList("reasons"))
                this.reasons.add(SpawnReason.valueOf(reason));

        if (config.isList("types"))
            for (final String entity : config.getStringList("types"))
                this.types.add(EntityType.valueOf(entity));
    }

    @Override
    public boolean isApplicable(final CreatureSpawnEvent event) {
        return   (this.reasons.size() == 0 || this.reasons.contains(event.getSpawnReason()))
                && (this.types.size() == 0 || this.types.contains(event.getEntityType()));
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": [reasons: " + this.reasons + "; types: " + this.types + "]";
    }

}
