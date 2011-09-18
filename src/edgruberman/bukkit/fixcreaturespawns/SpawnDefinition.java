package edgruberman.bukkit.fixcreaturespawns;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.material.MaterialData;

/**
 * Defines criterion for matching a spawn.
 */
final class SpawnDefinition {
    
    static final SpawnReason DEFAULT_REASON = null;
    static final MaterialData DEFAULT_MATERIAL = null;
    static final BlockFace DEFAULT_RELATIVE = BlockFace.SELF;
    
    static final byte MATERIAL_DATA_ANY = (byte) 0xff;
    
    SpawnReason reason;
    MaterialData material;
    BlockFace relative;
    
    SpawnDefinition(final SpawnReason reason, final MaterialData material, final BlockFace relative) {
        this.reason = reason;
        this.material = material;
        this.relative = (relative != null ? relative : SpawnDefinition.DEFAULT_RELATIVE);
    }
    
    boolean isMatch(final CreatureSpawnEvent event) {
        if ((this.reason != null) && !(this.reason == event.getSpawnReason())) return false;
        
        if (this.material != null) return this.isMaterialMatch(event.getLocation());
        
        return true;
    }
    
    @Override
    public String toString() {
        return "Reason: " + this.reason + "; Material: " + this.material + "; Relative: " + this.relative;
    }
    
    /**
     * Determine if material matches at relative.
     * 
     * @param location reference location for relatives
     * @return true if relative material matches
     */
    private boolean isMaterialMatch(final Location location) {
        Location relLoc;
        if (this.relative == BlockFace.SELF) {
            relLoc = location;
        } else {
            relLoc = location.clone().add(this.relative.getModX(), this.relative.getModY(), this.relative.getModZ());
        }
        
        // Material ID must match at least
        if (this.material.getItemTypeId() != relLoc.getWorld().getBlockTypeIdAt(relLoc))
            return false;
        
        // Any data matches, only check ID, avoid expensive getBlock() call
        if (this.material.getData() == SpawnDefinition.MATERIAL_DATA_ANY)
            return true;

        // ID and data must both explicitly match
        if (!this.material.equals(relLoc.getBlock().getState().getData()))
            return false;
        
        return true;
    }
}