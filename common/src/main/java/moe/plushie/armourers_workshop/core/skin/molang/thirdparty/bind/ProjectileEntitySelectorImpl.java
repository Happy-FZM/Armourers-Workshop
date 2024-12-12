package moe.plushie.armourers_workshop.core.skin.molang.thirdparty.bind;

import moe.plushie.armourers_workshop.core.skin.molang.runtime.bind.selector.ProjectileEntitySelector;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SpectralArrow;

public class ProjectileEntitySelectorImpl<T extends Projectile> extends EntitySelectorImpl<T> implements ProjectileEntitySelector {

    @Override
    public ProjectileEntitySelectorImpl<T> apply(T entity, ContextSelectorImpl context) {
        super.apply(entity, context);
        return this;
    }

    @Override
    public double getOnGroundTime() {
        return 0; // NO IMPL.
    }

    @Override
    public boolean isSpectral() {
        return entity instanceof SpectralArrow;
    }

    @Override
    public Object getOwner() {
        return entity.getOwner();
    }
}
