package moe.plushie.armourers_workshop.core.data;

import moe.plushie.armourers_workshop.api.core.IDataCodec;
import moe.plushie.armourers_workshop.api.core.IDataSerializable;
import moe.plushie.armourers_workshop.api.core.IDataSerializer;
import moe.plushie.armourers_workshop.api.core.IDataSerializerKey;
import moe.plushie.armourers_workshop.core.blockentity.SkinnableBlockEntity;
import moe.plushie.armourers_workshop.core.math.OpenMatrix4f;
import moe.plushie.armourers_workshop.core.math.OpenQuaternion3f;
import moe.plushie.armourers_workshop.core.math.Rectangle3f;
import moe.plushie.armourers_workshop.core.math.Rectangle3i;
import moe.plushie.armourers_workshop.core.math.Vector3f;
import moe.plushie.armourers_workshop.core.math.Vector3i;
import moe.plushie.armourers_workshop.core.math.Vector4f;
import moe.plushie.armourers_workshop.core.skin.Skin;
import moe.plushie.armourers_workshop.core.skin.SkinDescriptor;
import moe.plushie.armourers_workshop.core.skin.SkinLoader;
import moe.plushie.armourers_workshop.core.skin.SkinMarker;
import moe.plushie.armourers_workshop.core.skin.SkinTypes;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperties;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperty;
import moe.plushie.armourers_workshop.core.utils.Collections;
import moe.plushie.armourers_workshop.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SkinBlockPlaceContext extends BlockPlaceContext {

    private Vector3f rotations = Vector3f.ZERO;
    private SkinDescriptor skin = SkinDescriptor.EMPTY;
    private ArrayList<Part> parts = new ArrayList<>();

    private SkinProperties properties;

    public SkinBlockPlaceContext(UseOnContext context) {
        super(context);
        this.loadElements(SkinLoader.getInstance()::loadSkin);
    }

    public SkinBlockPlaceContext(Player player, InteractionHand hand, ItemStack itemStack, BlockHitResult traceResult) {
        super(player.getLevel(), player, hand, itemStack, traceResult);
        this.loadElements(SkinLoader.getInstance()::getSkin);
    }

    public static SkinBlockPlaceContext of(BlockPos pos) {
        if (pos instanceof AttachedBlockPos pos1) {
            return pos1.context;
        }
        return null;
    }

    protected void transform(Vector3f r) {
        for (var part : parts) {
            part.transform(r);
        }
    }

    protected void loadElements(Function<String, Skin> provider) {
        var itemStack = getItemInHand();
        var descriptor = SkinDescriptor.of(itemStack);
        if (descriptor.isEmpty()) {
            return;
        }
        var skin = provider.apply(descriptor.getIdentifier());
        if (skin == null) {
            return;
        }
        var parts = new ArrayList<Part>();
        var parentParts = new ArrayList<ParentPart>();
        var blockPosList = new ArrayList<BlockPos>();
        skin.getBlockBounds().forEach((pos, shape) -> {
            var rect = new Rectangle3i(shape);
            if (pos.equals(Vector3i.ZERO)) {
                var part = new ParentPart(BlockPos.ZERO, rect, descriptor, skin);
                parts.add(part);
                parentParts.add(part);
            } else {
                var part = new Part(new BlockPos(pos.getX(), pos.getY(), pos.getZ()), rect);
                parts.add(part);
            }
        });
        this.skin = descriptor;
        this.parts = parts;
        this.properties = skin.getProperties();
        var state = ModBlocks.SKINNABLE.get().getStateForPlacement(this);
        if (state != null) {
            this.rotations = SkinnableBlockEntity.getRotations(state);
            this.transform(rotations);
        }
        // copy all transformed block pose into list.
        for (var part : parts) {
            blockPosList.add(part.getOffset());
        }
        parentParts.forEach(it -> it.setReferences(blockPosList));
    }

    public <V> V getProperty(SkinProperty<V> property) {
        if (properties != null && !properties.isEmpty()) {
            return properties.get(property);
        }
        return property.getDefaultValue();
    }

    public boolean canPlace(Part part) {
        if (skin.isEmpty()) {
            return false;
        }
        if (skin.getType() != SkinTypes.BLOCK) {
            return false;
        }
        BlockPos pos = super.getClickedPos().offset(part.getOffset());
        return this.getLevel().getBlockState(pos).canBeReplaced(this);
    }

    @Override
    public boolean canPlace() {
        return parts != null && parts.stream().allMatch(this::canPlace) && super.canPlace();
    }

    @Override
    public BlockPos getClickedPos() {
        return new AttachedBlockPos(this, super.getClickedPos());
    }

    public SkinDescriptor getSkin() {
        return skin;
    }

    public ArrayList<Part> getParts() {
        return parts;
    }


    // same the SkinnableBlockEntity.CodingKeys
    private static class CodingKeys {

        public static final IDataSerializerKey<BlockPos> REFERENCE = IDataSerializerKey.create("Refer", IDataCodec.BLOCK_POS, BlockPos.ZERO);
        public static final IDataSerializerKey<Rectangle3i> SHAPE = IDataSerializerKey.create("Shape", Rectangle3i.CODEC, Rectangle3i.ZERO);
        public static final IDataSerializerKey<BlockPos> LINKED_POS = IDataSerializerKey.create("LinkedPos", IDataCodec.BLOCK_POS, null);
        public static final IDataSerializerKey<SkinDescriptor> SKIN = IDataSerializerKey.create("Skin", SkinDescriptor.CODEC, SkinDescriptor.EMPTY);
        public static final IDataSerializerKey<SkinProperties> SKIN_PROPERTIES = IDataSerializerKey.create("SkinProperties", SkinProperties.CODEC, SkinProperties.EMPTY, SkinProperties.EMPTY::copy);
        public static final IDataSerializerKey<List<BlockPos>> REFERENCES = IDataSerializerKey.create("Refers", IDataCodec.BLOCK_POS.listOf(), Collections.emptyList());
        public static final IDataSerializerKey<List<SkinMarker>> MARKERS = IDataSerializerKey.create("Markers", SkinMarker.CODEC.listOf(), Collections.emptyList());
    }

    public static class Part implements IDataSerializable.Immutable {

        private BlockPos offset;
        private Rectangle3i shape;

        public Part() {
            this(BlockPos.ZERO, Rectangle3i.ZERO);
        }

        public Part(BlockPos offset, Rectangle3i shape) {
            this.offset = offset;
            this.shape = shape;
        }

        @Override
        public void serialize(IDataSerializer serializer) {
            serializer.write(CodingKeys.REFERENCE, offset);
            serializer.write(CodingKeys.SHAPE, shape);
        }

        public void transform(Vector3f r) {
            OpenQuaternion3f q = new OpenQuaternion3f(r.getX(), r.getY(), r.getZ(), true);

            Vector4f f = new Vector4f(offset.getX(), offset.getY(), offset.getZ(), 1.0f);
            f.transform(q);
            offset = new BlockPos(Math.round(f.getX()), Math.round(f.getY()), Math.round(f.getZ()));

            Rectangle3f of = new Rectangle3f(shape);
            of.mul(q);
            shape = new Rectangle3i(0, 0, 0, 0, 0, 0);
            shape.setX(Math.round(of.getX()));
            shape.setY(Math.round(of.getY()));
            shape.setZ(Math.round(of.getZ()));
            shape.setWidth(Math.round(of.getWidth()));
            shape.setHeight(Math.round(of.getHeight()));
            shape.setDepth(Math.round(of.getDepth()));
        }

        public BlockPos getOffset() {
            return offset;
        }

        public Rectangle3i getShape() {
            return shape;
        }
    }

    public static class ParentPart extends Part {

        private final SkinDescriptor descriptor;
        private final SkinProperties properties;

        private List<BlockPos> references = Collections.emptyList();
        private List<SkinMarker> markerList;

        public ParentPart(BlockPos offset, Rectangle3i shape, SkinDescriptor descriptor, Skin skin) {
            super(offset, shape);
            this.descriptor = descriptor;
            this.properties = skin.getProperties();
            this.markerList = Collections.newList(skin.getMarkers());
        }

        @Override
        public void serialize(IDataSerializer serializer) {
            super.serialize(serializer);
            serializer.write(CodingKeys.REFERENCES, references);
            serializer.write(CodingKeys.MARKERS, markerList);
            serializer.write(CodingKeys.SKIN, descriptor);
            serializer.write(CodingKeys.SKIN_PROPERTIES, properties);
        }

        @Override
        public void transform(Vector3f r) {
            super.transform(r);

            var q = new OpenQuaternion3f(r.getX(), r.getY(), r.getZ(), true);
            var newMarkerList = new ArrayList<SkinMarker>();
            for (var marker : markerList) {
                var f = new Vector4f(marker.x, marker.y, marker.z, 1.0f);
                f.transform(OpenMatrix4f.createScaleMatrix(-1, -1, 1));
                f.transform(q);
                int x = Math.round(f.getX());
                int y = Math.round(f.getY());
                int z = Math.round(f.getZ());
                marker = new SkinMarker((byte) x, (byte) y, (byte) z, marker.meta);
                newMarkerList.add(marker);
            }
            this.markerList = newMarkerList;
        }

        public void setReferences(List<BlockPos> blockPosList) {
            this.references = blockPosList;
        }

        public List<BlockPos> getReferences() {
            return references;
        }
    }

    public static class AttachedBlockPos extends BlockPos {

        protected final SkinBlockPlaceContext context;

        public AttachedBlockPos(SkinBlockPlaceContext context, BlockPos pos) {
            super(pos);
            this.context = context;
        }
    }
}
