package edgruberman.bukkit.creaturepolicy.rules;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Applicability determined by how many of the same type of entities are nearby.
 */
public class MaximumSame extends ReasonType {

    protected static final int DEFAULT_MAXIMUM = -1;
    protected static final int DEFAULT_RADIUS = -1;

    protected int maximum = MaximumSame.DEFAULT_MAXIMUM;
    protected int radius = MaximumSame.DEFAULT_RADIUS;

    @Override
    public void load(final ConfigurationSection config) {
        if (config == null) return;

        super.load(config);

        this.maximum = config.getInt("maximum", this.maximum);
        this.radius = config.getInt("radius", this.radius);
    }

    @Override
    public boolean isApplicable(final CreatureSpawnEvent event) {
        if (!super.isApplicable(event)) return false;

        // Increasing radius perimeters from spawn location
        int nearbyType = 0;
        final int x = event.getLocation().getChunk().getX();
        final int z = event.getLocation().getChunk().getZ();
        for (int perimeter = 0; perimeter <= this.radius; perimeter++)
            for (int modX = -perimeter; modX <= perimeter ; modX++ )
                for (int modZ = -perimeter; modZ <= perimeter ; modZ++ ) {
                    if (Math.abs(modX) != perimeter && Math.abs(modZ) != perimeter) continue;

                    for (final Entity entity : event.getLocation().getWorld().getChunkAt(x + modX, z + modZ).getEntities())
                        if (entity.getType() == event.getEntityType()) {
                            nearbyType++;
                            if (nearbyType == this.maximum) return true;
                        }

                }

        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": [reasons: " + this.reasons + "; types: " + this.types + "; maximum: " + this.maximum + "; radius: " + this.radius + "]";
    }

}
