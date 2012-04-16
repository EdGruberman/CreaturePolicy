package edgruberman.bukkit.creaturepolicy.rules;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class MaximumRule extends ReasonRule {

    protected static final int DEFAULT_MAXIMUM = -1;
    protected static final int DEFAULT_RADIUS = -1;
    protected static final int DEFAULT_REFRESH = -1;

    protected int maximum = MaximumRule.DEFAULT_MAXIMUM;
    protected int radius = MaximumRule.DEFAULT_RADIUS;
    protected long refresh = MaximumRule.DEFAULT_REFRESH;
    protected long lastApplicable = -1;

    @Override
    public void load(final ConfigurationSection config) {
        if (config == null) return;

        super.load(config);

        this.maximum = config.getInt("maximum", this.maximum);
        this.radius = config.getInt("radius", this.radius);
        this.refresh = config.getLong("refresh", this.refresh);
    }

    @Override
    public boolean isApplicable(final CreatureSpawnEvent event) {
        if (!super.isApplicable(event)) return false;

        // Do not recalculate applicability if recent result is still fresh
        if (System.currentTimeMillis() - this.lastApplicable < this.refresh) return true;

        // Do not apply if total entities nearby is less than maximum
        final List<Entity> nearby = event.getEntity().getNearbyEntities(this.radius, this.radius, this.radius);
        if (nearby.size() < this.maximum) return false;

        // Do not apply if total entities of same type nearby is less than maximum
        int nearbyType = 0;
        for (final Entity entity : nearby) if (entity.getType() == event.getEntityType()) nearbyType++;
        if (nearbyType < this.maximum) return false;

        // Cache result
        this.lastApplicable = System.currentTimeMillis();
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": [entities: " + this.entities + "; reasons: " + this.reasons + "; maximum: " + this.maximum + "; radius: " + this.radius + "; refresh: " + this.refresh + "]";
    }

}
