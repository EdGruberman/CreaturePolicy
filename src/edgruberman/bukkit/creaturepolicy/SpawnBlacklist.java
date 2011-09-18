package edgruberman.bukkit.creaturepolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.CreatureType;

/**
 * Criteria to deny spawns.
 */
final class SpawnBlacklist {
    
    static final int DEFAULT_SAFE_RADIUS = -1;
    
    static Map<World, SpawnBlacklist> worlds = new HashMap<World, SpawnBlacklist>();
    
    World world;
    int safeRadiusSquared;
    Map<CreatureType, List<SpawnDefinition>> creatures = new HashMap<CreatureType, List<SpawnDefinition>>();
    
    SpawnBlacklist(final World world) {
        this.world = world;
        this.setSafeRadius(DEFAULT_SAFE_RADIUS);
        SpawnBlacklist.worlds.put(world, this);
    }
    
    void setSafeRadius(final int radius) {
        if (radius <= 0) {
            this.safeRadiusSquared = radius;
            return;
        }
        
        this.safeRadiusSquared = (int) Math.pow(radius, 2);
    }
    
    int getSafeRadius() {
        if (this.safeRadiusSquared <= 0)
            return this.safeRadiusSquared;
        
        return (int) Math.sqrt(this.safeRadiusSquared);
    }
}