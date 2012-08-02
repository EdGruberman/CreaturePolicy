package edgruberman.bukkit.creaturepolicy.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.CreatureSpawnEvent;

import edgruberman.bukkit.creaturepolicy.Policy;

/** Applicability determined by comparing material relative to spawn location */
public class RelativeMaterial extends ReasonType {

    /** Relative to spawn location */
    protected BlockFace relative = BlockFace.DOWN;

    /** Material Type IDs and associated byte data; If no data list items exist, any data will apply */
    protected final Map<Integer, List<Byte>> materials = new HashMap<Integer, List<Byte>>();

    public RelativeMaterial(final Policy policy, final ConfigurationSection config) {
        super(policy, config);

        this.materials.clear();
        for (String type: config.getStringList("materials")) {
            Byte data = null;
            if (type.contains(":")) {
                type = type.split(":")[0];
                data = Byte.parseByte(type.split(":")[1]);
            }

            final Material material;
            try {
                material = Material.valueOf(type);
            } catch (final Throwable t) {
                this.policy.publisher.plugin.getLogger().warning("Unable to identify Material type: " + type + "; " + t.getClass().getName() + ": " + t.getMessage());
                continue;
            }

            if (!this.materials.containsKey(material.getId())) this.materials.put(material.getId(), new ArrayList<Byte>());
            if (data == null) continue;

            this.materials.get(material.getId()).add(data);
        }

        try {
            this.relative = BlockFace.valueOf(config.getString("relative", this.relative.name()));
        } catch (final Throwable t) {
            this.policy.publisher.plugin.getLogger().warning("Unable to identify relative BlockFace: " + config.getString("relative") + "; Defaulting to " + this.relative.name() + "; " + t.getClass().getName() + ": " + t.getMessage());
        }
    }

    @Override
    public boolean isApplicable(final CreatureSpawnEvent event) {
        if (!super.isApplicable(event)) return false;

        final Location relativeLocation = event.getLocation().clone().add(this.relative.getModX(), this.relative.getModY(), this.relative.getModZ());
        final int relativeMaterialTypeId = relativeLocation.getWorld().getBlockTypeIdAt(relativeLocation);

        final List<Byte> matched = this.materials.get(relativeMaterialTypeId);
        if (matched == null) return false;

        if (matched.size() > 0)
            return matched.contains(relativeLocation.getWorld().getBlockAt(relativeLocation).getData());

        // Any data matches, if none defined
        return true;
    }

    @Override
    public String toString() {
        String materials = "";
        for (final Map.Entry<Integer, List<Byte>> entry : this.materials.entrySet()) {
            final String name = Material.getMaterial(entry.getKey()).name();

            if (entry.getValue().size() == 0) {
                if (materials.length() != 0) materials += ",";
                materials += name;
                continue;
            }

            for (final Byte data : entry.getValue()) {
                if (materials.length() != 0) materials += ",";
                materials += name + ":" + data.toString();
            }
        }

        return super.toString("reasons: " + this.reasons + "; types: " + this.types + "; materials: [" + materials + "]; relative: " + this.relative);
    }

}
