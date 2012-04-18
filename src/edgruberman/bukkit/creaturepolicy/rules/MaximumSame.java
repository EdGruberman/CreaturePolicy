package edgruberman.bukkit.creaturepolicy.rules;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * Applicability determined by how many of the same type of entities are nearby.
 */
public class MaximumSame extends ReasonType implements Listener {

    /**
     * No limit.
     */
    protected static final int DEFAULT_MAXIMUM = -1;

    /**
     * No least limit.
     */
    protected static final int DEFAULT_LEAST = Integer.MAX_VALUE;

    /**
     * Only count entities in the same chunk spawn is occurring in.
     */
    protected static final int DEFAULT_RADIUS = 0;

    /**
     * Treat maximum as per spawner, independent of player count.
     */
    protected static final boolean DEFAULT_SHARED = false;

    /**
     * Do not cache last applicability; Count entities each spawn.
     */
    protected static final long DEFAULT_CACHE = -1;

    /**
     * Maximum count of same type of entities allowed within radius.
     */
    protected int maximum = MaximumSame.DEFAULT_MAXIMUM;

    /**
     * Least maximum possible.
     */
    protected int least = MaximumSame.DEFAULT_LEAST;

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

    /**
     * MilliSeconds to cache last applicability calculation.
     */
    protected long cache = MaximumSame.DEFAULT_CACHE;

    /**
     * Relates a chunk to the last time entities were counted for this rule.
     */
    protected Map<Long, Applicability> last = new HashMap<Long, Applicability>();

    @Override
    public void load(final ConfigurationSection config) {
        if (config == null) return;

        super.load(config);

        this.maximum = config.getInt("maximum", this.maximum);
        this.least = config.getInt("least", this.least);
        this.radius = config.getInt("radius", this.radius);
        this.shared = config.getBoolean("shared", this.shared);
        this.cache = config.getLong("cache", this.cache);

        if (this.cache > 0)
            this.policy.publisher.plugin.getServer().getPluginManager().registerEvents(this, this.policy.publisher.plugin);
    }

    @Override
    public void clear() {
        HandlerList.unregisterAll(this);
        this.last.clear();
        super.clear();
    }

    @Override
    public boolean isApplicable(final CreatureSpawnEvent event) {
        if (!super.isApplicable(event)) return false;

        // Use last applicable result if caching is enabled and cache is not stale
        long chunkId = -1;
        if (this.cache > 0) {
            chunkId = MaximumSame.biject(event.getLocation().getChunk());
            final Applicability applicability = this.last.get(chunkId);
            if (applicability != null && System.currentTimeMillis() - applicability.last < this.cache)
                return applicability.result;
        }

        final int maximum = (this.shared ? Math.max(this.least, this.maximum / this.policy.publisher.plugin.getServer().getOnlinePlayers().length) : this.maximum);
        final boolean result = this.maximumReached(maximum, event.getLocation().getChunk(), event.getEntityType());
        if (this.cache <= 0) return result;

        this.last.put(chunkId, new Applicability(System.currentTimeMillis(), result));
        return result;
    }

    /**
     * Increasing radius perimeters outwards from spawn location.
     */
    private boolean maximumReached(final int maximum, final Chunk spawn, final EntityType type) {
        int nearbyType = 0;
        for (int perimeter = 0; perimeter <= this.radius; perimeter++)
            for (int modX = -perimeter; modX <= perimeter ; modX++ )
                for (int modZ = -perimeter; modZ <= perimeter ; modZ++ ) {
                    if (Math.abs(modX) != perimeter && Math.abs(modZ) != perimeter) continue;

                    for (final Entity entity : spawn.getWorld().getChunkAt(spawn.getX() + modX, spawn.getZ() + modZ).getEntities())
                        if (entity.getType() == type) {
                            nearbyType++;
                            if (nearbyType == maximum) return true;
                        }

                }

        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": [reasons: " + this.reasons + "; types: " + this.types
                + "; maximum: " + this.maximum + "; radius: " + this.radius + "; shared: " + this.shared + "; cache: " + this.cache + "]";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(final ChunkUnloadEvent unload) {
        this.last.remove(MaximumSame.biject(unload.getChunk()));
    }

    private static long biject(final Chunk chunk) {
        return ((long) chunk.getX() << 32) + (chunk.getZ() - Integer.MIN_VALUE);
    }

    private class Applicability {

        long last;
        boolean result;

        Applicability(final long last, final boolean result) {
            this.last = last;
            this.result = result;
        }

    }

}
