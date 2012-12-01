package edgruberman.bukkit.creaturepolicy.rules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.CreatureSpawnEvent;

import edgruberman.bukkit.creaturepolicy.Policy;

/** applicability determined by comparing material relative to spawn location */
public class RelativeMaterial extends Reason {

    /** block below */
    public final static BlockFace DEFAULT_RELATIVE = BlockFace.DOWN;

    /** no material will match */
    public final static Map<Integer, List<Byte>> DEFAULT_MATERIALS = Collections.emptyMap();



    /** relative to spawn location */
    protected final BlockFace relative;

    /** material Type IDs and associated byte data; If no data list items exist, any data will apply */
    protected final Map<Integer, List<Byte>> materials;

    public RelativeMaterial(final Policy policy, final ConfigurationSection config) {
        super(policy, config);

        // parse materials
        final Map<Integer, List<Byte>> materials = new HashMap<Integer, List<Byte>>();
        for (final String entry: config.getStringList("materials")) {
            String type = entry;
            Byte data = null;
            if (type.contains("/")) {
                type = entry.split("/")[0];
                try {
                    data = Byte.parseByte(entry.split("/")[1]);
                } catch (final NumberFormatException e) {
                    policy.publisher.plugin.getLogger().warning("Unable to identify Material data for: " + entry + "; " + e);
                    continue;
                }
            }

            final Material material;
            try {
                material = Material.valueOf(type);
            } catch (final Exception e) {
                policy.publisher.plugin.getLogger().warning("Unable to identify Material type: " + entry + "; " + e);
                continue;
            }

            if (!materials.containsKey(material.getId())) materials.put(material.getId(), new ArrayList<Byte>());
            if (data == null) continue;

            materials.get(material.getId()).add(data);
        }
        this.materials = Collections.unmodifiableMap(materials);

        // parse relative
        BlockFace relative = RelativeMaterial.DEFAULT_RELATIVE;
        try {
            relative = BlockFace.valueOf(config.getString("relative", relative.name()));
        } catch (final Exception e) {
            policy.publisher.plugin.getLogger().warning("Unable to identify relative BlockFace: " + config.getString("relative") + "; Defaulting to " + relative.name() + "; " + e);
        }
        this.relative = relative;
    }

    @Override
    public boolean isApplicable(final CreatureSpawnEvent event) {
        if (!super.isApplicable(event)) return false;

        final Location relativeLocation = event.getLocation().clone().add(this.relative.getModX(), this.relative.getModY(), this.relative.getModZ());
        final int relativeMaterialTypeId = relativeLocation.getWorld().getBlockTypeIdAt(relativeLocation);

        final List<Byte> matched = this.materials.get(relativeMaterialTypeId);
        if (matched == null) return false;

        // specific data defined must match at least one
        if (matched.size() > 0)
            return matched.contains(relativeLocation.getWorld().getBlockAt(relativeLocation).getData());

        // any data matches, if none defined
        return true;
    }

    @Override
    public String toString() {
        final List<String> materials = new ArrayList<String>();
        for (final Map.Entry<Integer, List<Byte>> entry : this.materials.entrySet()) {
            final String name = Material.getMaterial(entry.getKey()).name();

            if (entry.getValue().size() == 0) {
                materials.add(name);
                continue;
            }

            for (final Byte data : entry.getValue()) materials.add(name + "/" + data);
        }

        return super.toString("reasons: " + this.reasons + "; creatures: " + this.creatures + "; materials: " + materials + "; relative: " + this.relative);
    }

}
