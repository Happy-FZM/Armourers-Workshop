package moe.plushie.armourers_workshop.builder.blockentity;

import moe.plushie.armourers_workshop.api.client.IBlockEntityExtendedRenderer;
import moe.plushie.armourers_workshop.api.common.IPaintable;
import moe.plushie.armourers_workshop.api.data.IDataSerializer;
import moe.plushie.armourers_workshop.api.skin.paint.ISkinPaintColor;
import moe.plushie.armourers_workshop.builder.block.ArmourerBlock;
import moe.plushie.armourers_workshop.core.blockentity.UpdatableBlockEntity;
import moe.plushie.armourers_workshop.core.data.color.BlockPaintColor;
import moe.plushie.armourers_workshop.core.skin.paint.SkinPaintColor;
import moe.plushie.armourers_workshop.core.skin.paint.SkinPaintTypes;
import moe.plushie.armourers_workshop.utils.BlockUtils;
import moe.plushie.armourers_workshop.utils.DataSerializerKey;
import moe.plushie.armourers_workshop.utils.DataTypeCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class SkinCubeBlockEntity extends UpdatableBlockEntity implements IPaintable, IBlockEntityExtendedRenderer {

    private static final DataSerializerKey<CompoundTag> COLORS_KEY = DataSerializerKey.create("Color", DataTypeCodecs.COMPOUND_TAG, new CompoundTag());

    protected BlockPaintColor colors = new BlockPaintColor(SkinPaintColor.WHITE);
    protected boolean customRenderer = false;

    public SkinCubeBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public void readAdditionalData(IDataSerializer serializer) {
        colors.deserializeNBT(serializer.read(COLORS_KEY));
        customRenderer = checkRendererFromColors();
    }

    public void writeAdditionalData(IDataSerializer serializer) {
        serializer.write(COLORS_KEY, colors.serializeNBT());
//        // we must need to tracking the facing at the save it,
//        // because we need to get the colors based facing from copied NBT.
//        // we can know the direction has been changed when the load copied NBT.
//        nbt.putString(Constants.NBT.FACING, getDirection().name());
    }

    private boolean checkRendererFromColors() {
        for (var color : colors.values()) {
            if (color.getPaintType() != SkinPaintTypes.NORMAL) {
                return true;
            }
        }
        return false;
    }

    private Direction getResolvedDirection(Direction dir) {
        return switch (getDirection()) {
            case SOUTH -> Rotation.CLOCKWISE_180.rotate(dir); // rotate 180° get facing north direction.
            case WEST -> Rotation.CLOCKWISE_90.rotate(dir); // rotate 90° get facing north direction.
            case EAST -> Rotation.COUNTERCLOCKWISE_90.rotate(dir);// rotate -90° get facing north direction.
            default -> dir;
        };
    }

    @Override
    public ISkinPaintColor getColor(Direction direction) {
        return colors.getOrDefault(getResolvedDirection(direction), SkinPaintColor.WHITE);
    }

    @Override
    public void setColor(Direction direction, ISkinPaintColor color) {
        this.colors.put(getResolvedDirection(direction), color);
        this.customRenderer = checkRendererFromColors();
        BlockUtils.combine(this, this::sendBlockUpdates);
    }

    @Override
    public void setColors(Map<Direction, ISkinPaintColor> colors) {
        colors.forEach((direction, color) -> this.colors.put(getResolvedDirection(direction), color));
        this.customRenderer = checkRendererFromColors();
        BlockUtils.combine(this, this::sendBlockUpdates);
    }

    public Direction getDirection() {
        return getBlockState().getOptionalValue(ArmourerBlock.FACING).orElse(Direction.NORTH);
    }

    @Override
    public boolean shouldUseExtendedRenderer() {
        return customRenderer;
    }
}
