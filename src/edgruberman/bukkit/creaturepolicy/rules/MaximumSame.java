package edgruberman.bukkit.creaturepolicy.rules;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Applicability determined by how many of the same type of entities are nearby.
 */
public class MaximumSame extends ReasonType {

    /**
     * No limit.
     */
    protected static final int DEFAULT_MAXIMUM = -1;

    /**
     * Only count entities in the same chunk spawn is occurring in.
     */
    protected static final int DEFAULT_RADIUS = 0;

    /**
     * Treat maximum as per spawner, independent of player count.
     */
    protected static final boolean DEFAULT_SHARED = false;

    /**
     * Maximum count of same type of entities allowed within radius.
     */
    protected int maximum = MaximumSame.DEFAULT_MAXIMUM;

    /**
     * Number of chunks outward from spawn location to count entities against
     * maximum.
     */
    protected int radius = MaximumSame.DEFAULT_RADIUS;

    /**
     * true to divide maximum by the total number of online players; false
     * to use the maximum independently of player count.
     */
    protected boolean shared = MaximumSame.DEFAULT_SHARED;

    @Override
    public void load(final ConfigurationSection config) {
        if (config == null) return;

        super.load(config);

        this.maximum = config.getInt("maximum", this.maximum);
        this.radius = config.getInt("radius", this.radius);
        this.shared = config.getBoolean("shared", this.shared);
    }

    @Override
    public boolean isApplicable(final CreatureSpawnEvent event) {
        if (!super.isApplicable(event)) return false;

        final int maximum = (this.shared ? (this.maximum / this.policy.publisher.plugin.getServer().getOfflinePlayers().length) : this.maximum);

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
                            if (nearbyType == maximum) return true;
                        }

                }

        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": [reasons: " + this.reasons + "; types: " + this.types + "; maximum: " + this.maximum + "; radius: " + this.radius + "]";
    }

}
