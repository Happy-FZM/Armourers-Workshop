package moe.plushie.armourers_workshop.init.mixin.forge;

import moe.plushie.armourers_workshop.api.common.IBlockHandler;
import moe.plushie.armourers_workshop.compatibility.forge.AbstractForgeBlock;
import moe.plushie.armourers_workshop.core.utils.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IBlockHandler.class)
public interface ForgeBlockHandlerMixin extends AbstractForgeBlock {

    @Override
    default boolean isBed(BlockState state, BlockGetter level, BlockPos pos, LivingEntity entity) {
        IBlockHandler handler = Objects.unsafeCast(this);
        return handler.isCustomBed(level, pos, state, entity);
    }

    @Override
    default boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        IBlockHandler handler = Objects.unsafeCast(this);
        return handler.isCustomLadder(level, pos, state, entity);
    }
}
