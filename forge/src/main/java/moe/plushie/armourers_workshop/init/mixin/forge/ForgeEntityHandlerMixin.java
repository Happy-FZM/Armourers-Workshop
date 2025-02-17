package moe.plushie.armourers_workshop.init.mixin.forge;

import moe.plushie.armourers_workshop.api.common.IEntityHandler;
import moe.plushie.armourers_workshop.compatibility.forge.AbstractForgeEntity;
import moe.plushie.armourers_workshop.core.utils.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IEntityHandler.class)
public interface ForgeEntityHandlerMixin extends AbstractForgeEntity {

    @Override
    default ItemStack getPickedResult(HitResult target) {
        IEntityHandler handler = Objects.unsafeCast(this);
        return handler.getCustomPickResult(target);
    }
}
