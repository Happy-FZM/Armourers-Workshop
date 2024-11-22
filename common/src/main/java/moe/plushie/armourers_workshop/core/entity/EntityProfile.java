package moe.plushie.armourers_workshop.core.entity;

import moe.plushie.armourers_workshop.api.core.IDataCodec;
import moe.plushie.armourers_workshop.api.core.IDataSerializable;
import moe.plushie.armourers_workshop.api.core.IDataSerializer;
import moe.plushie.armourers_workshop.api.core.IDataSerializerKey;
import moe.plushie.armourers_workshop.api.core.IResourceLocation;
import moe.plushie.armourers_workshop.core.menu.SkinSlotType;
import moe.plushie.armourers_workshop.core.utils.Collections;
import moe.plushie.armourers_workshop.core.utils.Objects;
import moe.plushie.armourers_workshop.init.ModConfig;
import moe.plushie.armourers_workshop.utils.DataSerializers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class EntityProfile implements IDataSerializable.Immutable {

    public static final IDataCodec<EntityProfile> CODEC = IDataCodec.COMPOUND_TAG.serializer(EntityProfile::new);

    private final IResourceLocation registryName;
    private final SupportMap supports;
    private final List<IResourceLocation> transformers;
    private final boolean locked;

    public EntityProfile(IResourceLocation registryName, Map<SkinSlotType, String> supports, List<IResourceLocation> transformers, boolean locked) {
        this.registryName = registryName;
        this.supports = new SupportMap(supports);
        this.transformers = transformers;
        this.locked = locked;
    }

    public EntityProfile(IDataSerializer serializer) {
        this.registryName = serializer.read(CodingKeys.NAME);
        this.supports = serializer.read(CodingKeys.SLOTS);
        this.transformers = serializer.read(CodingKeys.TRANSFORMERS);
        this.locked = serializer.read(CodingKeys.LOCKED);
    }

    @Override
    public void serialize(IDataSerializer serializer) {
        serializer.write(CodingKeys.NAME, registryName);
        serializer.write(CodingKeys.SLOTS, supports);
        serializer.write(CodingKeys.TRANSFORMERS, transformers);
        serializer.write(CodingKeys.LOCKED, locked);
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isSupported(SkinSlotType slotType) {
        return supports.hasProvider(slotType);
    }

    public int getMaxCount(SkinSlotType slotType) {
        var provider = supports.getProvider(slotType);
        if (provider != null) {
            return provider.apply(slotType);
        }
        if (slotType == SkinSlotType.DEFAULT) {
            return slotType.getMaxSize();
        }
        return 0;
    }

    public Collection<SkinSlotType> getSlots() {
        return supports.getSlots();
    }

    public List<IResourceLocation> getTransformers() {
        return transformers;
    }

    public IResourceLocation getRegistryName() {
        return registryName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityProfile that)) return false;
        return Objects.equals(registryName, that.registryName) && locked == that.locked && Objects.equals(supports, that.supports) && Objects.equals(transformers, that.transformers);
    }

    @Override
    public int hashCode() {
        return registryName.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toString(this, "name", registryName, "locked", locked, "transformers", transformers);
    }

    public static boolean same(EntityProfile lhs, EntityProfile rhs) {
        return lhs.locked == rhs.locked && Objects.equals(lhs.supports, rhs.supports) && Objects.equals(lhs.transformers, rhs.transformers);
    }

    private static class CodingKeys {

        public static final IDataSerializerKey<IResourceLocation> NAME = IDataSerializerKey.create("Name", DataSerializers.RESOURCE_LOCATION, null);
        public static final IDataSerializerKey<Boolean> LOCKED = IDataSerializerKey.create("Locked", IDataCodec.BOOL, false);
        public static final IDataSerializerKey<List<IResourceLocation>> TRANSFORMERS = IDataSerializerKey.create("Transformers", DataSerializers.RESOURCE_LOCATION.listOf(), Collections.emptyList());

        public static final IDataSerializerKey<SupportMap> SLOTS = IDataSerializerKey.create("Slots", SupportMap.CODEC, new SupportMap(new HashMap<>()));

        public static final Map<SkinSlotType, IDataSerializerKey<String>> ALL_SLOTS = Collections.immutableMap(builder -> {
            for (var slotType : SkinSlotType.values()) {
                var name = slotType.getName();
                var key = IDataSerializerKey.create(name, IDataCodec.STRING, null);
                builder.put(slotType, key);
            }
        });
    }

    private static class SupportMap implements IDataSerializable.Immutable {

        public static final IDataCodec<SupportMap> CODEC = IDataCodec.COMPOUND_TAG.serializer(SupportMap::new);

        private final Map<SkinSlotType, String> supports;

        private Map<SkinSlotType, Function<SkinSlotType, Integer>> providers;

        public SupportMap(Map<SkinSlotType, String> supports) {
            this.supports = supports;
        }

        public SupportMap(IDataSerializer serializer) {
            this.supports = Collections.immutableMap(builder -> {
                for (var entry : CodingKeys.ALL_SLOTS.entrySet()) {
                    var value = serializer.read(entry.getValue());
                    if (value != null) {
                        builder.put(entry.getKey(), value);
                    }
                }
            });
        }

        @Override
        public void serialize(IDataSerializer serializer) {
            for (var entry : CodingKeys.ALL_SLOTS.entrySet()) {
                var value = supports.get(entry.getKey());
                if (value != null) {
                    serializer.write(entry.getValue(), value);
                }
            }
        }

        public Collection<SkinSlotType> getSlots() {
            return supports.keySet();
        }

        public boolean hasProvider(SkinSlotType slotType) {
            return supports.containsKey(slotType);
        }

        public Function<SkinSlotType, Integer> getProvider(SkinSlotType slotType) {
            if (providers != null) {
                return providers.get(slotType);
            }
            providers = Collections.immutableMap(builder -> supports.forEach((slotType1, name) -> {
                var provider = getProviderByName(name);
                builder.put(slotType1, provider);
            }));
            return providers.get(slotType);
        }

        private Function<SkinSlotType, Integer> getProviderByName(String name) {
            if (name.equals("default_mob_slots")) {
                return it -> ModConfig.Common.prefersWardrobeMobSlots;
            }
            if (name.equals("default_player_slots")) {
                return it -> ModConfig.Common.prefersWardrobePlayerSlots;
            }
            try {
                int count = Integer.parseInt(name);
                return it -> count;
            } catch (Exception e) {
                e.printStackTrace();
                return it -> 0;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SupportMap that)) return false;
            return supports.equals(that.supports);
        }

        @Override
        public int hashCode() {
            return supports.hashCode();
        }
    }
}
