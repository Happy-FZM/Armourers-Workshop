package moe.plushie.armourers_workshop.compatibility.core.data;

import com.mojang.serialization.DynamicOps;
import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.core.IDataSerializer;
import moe.plushie.armourers_workshop.api.core.IDataSerializerKey;
import moe.plushie.armourers_workshop.core.utils.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

@Available("[1.21, )")
public class AbstractDataSerializer implements IDataSerializer {

    private final CompoundTag tag;
    private final HolderLookup.Provider provider;
    private final DynamicOps<Tag> ops;

    public AbstractDataSerializer(CompoundTag tag, HolderLookup.Provider provider) {
        this.tag = tag;
        this.provider = provider;
        if (provider != null) {
            this.ops = provider.createSerializationContext(NbtOps.INSTANCE);
        } else {
            this.ops = NbtOps.INSTANCE;
        }
    }

    public static AbstractDataSerializer wrap(CompoundTag tag) {
        return new AbstractDataSerializer(tag, null);
    }

    public static AbstractDataSerializer wrap(CompoundTag tag, @Nullable Entity entity) {
        return wrap(tag, Objects.flatMap(entity, Entity::level));
    }

    public static AbstractDataSerializer wrap(CompoundTag tag, @Nullable BlockEntity blockEntity) {
        return wrap(tag, Objects.flatMap(blockEntity, BlockEntity::getLevel));
    }

    public static AbstractDataSerializer wrap(CompoundTag tag, @Nullable Level level) {
        return new AbstractDataSerializer(tag, Objects.flatMap(level, Level::registryAccess));
    }

    public static AbstractDataSerializer wrap(CompoundTag tag, @Nullable HolderLookup.Provider provider) {
        return new AbstractDataSerializer(tag, provider);
    }


    @Override
    public <T> T read(IDataSerializerKey<T> key) {
        var name = key.getName();
        if (tag != null && tag.contains(name)) {
            var codec = key.getCodec().codec();
            var value = codec.decode(ops, tag.get(key.getName())).result();
            if (value.isPresent()) {
                T value2 = value.get().getFirst();
                if (value2 != null) {
                    return value2;
                }
            }
        }
        var constructor = key.getConstructor();
        if (constructor != null) {
            return constructor.get();
        }
        return key.getDefault();
    }

    @Override
    public <T> void write(IDataSerializerKey<T> key, T value) {
        if (tag == null) {
            return;
        }
        var defaultValue = key.getDefault();
        if (defaultValue == value || Objects.equals(defaultValue, value)) {
            return;
        }
        var name = key.getName();
        var codec = key.getCodec().codec();
        codec.encodeStart(ops, value).result().ifPresent(it -> {
            // we need to merge new value into the item.
            tag.put(name, it);
        });
    }
}
