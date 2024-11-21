package moe.plushie.armourers_workshop.core.skin.animation.engine.bind;

import moe.plushie.armourers_workshop.compatibility.core.AbstractRegistryManager;
import moe.plushie.armourers_workshop.core.skin.animation.molang.bind.selector.BlockSelector;
import net.minecraft.world.level.block.state.BlockState;

public class BlockSelectorImpl implements BlockSelector {

    protected BlockState blockState;

    public BlockSelectorImpl apply(BlockState blockState) {
        this.blockState = blockState;
        return this;
    }

    @Override
    public String getId() {
        return AbstractRegistryManager.getBlockKey(blockState.getBlock());
    }

    @Override
    public boolean hasTag(String name) {
        return AbstractRegistryManager.hasBlockTag(blockState, name);
    }
}
