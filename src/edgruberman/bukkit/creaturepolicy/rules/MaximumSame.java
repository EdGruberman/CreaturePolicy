package edgruberman.bukkit.creaturepolicy.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
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

import edgruberman.bukkit.creaturepolicy.Policy;

/** applicability determined by how many of the same type of entities are nearby */
public class MaximumSame extends Reason implements Listener {

    /** no limit */
    public final static int DEFAULT_MAXIMUM = -1;

    /** no least limit */
    public final static int DEFAULT_LEAST = Integer.MAX_VALUE;

    /** only count entities in the same chunk spawn is occurring in */
    public final static int DEFAULT_RADIUS = 0;

    /** treat maximum as per spawner, independent of player count */
    public final static boolean DEFAULT_SHARED = false;

    /** do not cache */
    public final static long DEFAULT_CACHE = -1;



    /** maximum count of same type of entities allowed within radius */
    protected final int maximum;

    /** least maximum possible */
    protected final int least;

    /** number of chunks outward from spawn location to count entities against maximum */
    protected final int radius;

    /** true to divide maximum by the total number of online players; false to use the maximum independently of player count */
    protected final boolean shared;

    /** milliseconds to cache last applicability calculation */
    protected final long cache;

    /** relates a chunk to the last time entities were counted for this rule */
    protected final Map<Long, Applicability> last = new HashMap<Long, Applicability>();

    public MaximumSame(final Policy policy, final ConfigurationSection config) {
        super(policy, config);

        this.maximum = config.getInt("maximum", MaximumSame.DEFAULT_MAXIMUM);
        this.least = config.getInt("least", MaximumSame.DEFAULT_LEAST);
        this.radius = config.getInt("radius", MaximumSame.DEFAULT_RADIUS);
        this.shared = config.getBoolean("shared", MaximumSame.DEFAULT_SHARED);
        this.cache = (config.getLong("cache", MaximumSame.DEFAULT_CACHE) > 0 ? TimeUnit.SECONDS.toMillis(config.getLong("cache")) : config.getLong("cache"));

        if (this.cache > 0) Bukkit.getPluginManager().registerEvents(this, policy.publisher.plugin);
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

        // use last applicable result if caching is enabled and cache is not stale
        long chunkId = -1;
        if (this.cache > 0) {
            chunkId = MaximumSame.biject(event.getLocation().getChunk());
            final Applicability applicability = this.last.get(chunkId);
            if (applicability != null && System.currentTimeMillis() - applicability.last < this.cache)
                return applicability.result;
        }

        final int maximum = (this.shared ? Math.max(this.least, this.maximum / Bukkit.getServer().getOnlinePlayers().length) : this.maximum);
        final boolean result = this.maximumReached(maximum, event.getLocation().getChunk(), event.getEntityType());
        if (this.cache <= 0) return result;

        this.last.put(chunkId, new Applicability(System.currentTimeMillis(), result));
        return result;
    }

    // increasing radius perimeters outwards from spawn location
    private boolean maximumReached(final int maximum, final Chunk spawn, final EntityType type) {
        int nearbyType = 0;
        for (int perimeter = 0; perimeter <= this.radius; perimeter++)
            for (int modX = -perimeter; modX <= perimeter ; modX++ )
                for (int modZ = -perimeter; modZ <= perimeter ; modZ++) {
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
        return super.toString("reasons: " + this.reasons + "; creatures: " + this.creatures
                + "; maximum: " + this.maximum + "; least: " + this.least + "; radius: " + this.radius
                + "; shared: " + this.shared + "; cache: " + (this.cache > 0 ? TimeUnit.MILLISECONDS.toSeconds(this.cache) : this.cache));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(final ChunkUnloadEvent unload) {
        this.last.remove(MaximumSame.biject(unload.getChunk()));
    }

    private static long biject(final Chunk chunk) {
        return ((long) chunk.getX() << 32) + (chunk.getZ() - Integer.MIN_VALUE);
    }



    private static final class Applicability {

        final long last;
        boolean result;

        Applicability(final long last, final boolean result) {
            this.last = last;
            this.result = result;
        }

    }

}
