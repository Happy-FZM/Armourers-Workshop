package moe.plushie.armourers_workshop.init.platform.fabric.builder;

import moe.plushie.armourers_workshop.api.common.ICapabilityType;
import moe.plushie.armourers_workshop.api.core.IRegistryHolder;
import moe.plushie.armourers_workshop.api.registry.ICapabilityTypeBuilder;
import moe.plushie.armourers_workshop.core.data.CapabilityStorage;
import moe.plushie.armourers_workshop.core.utils.TypedRegistry;
import moe.plushie.armourers_workshop.init.ModConstants;
import moe.plushie.armourers_workshop.init.ModLog;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.function.Function;

public class CapabilityTypeBuilderImpl<T> implements ICapabilityTypeBuilder<T> {

    private final Class<T> type;
    private final Function<Entity, Optional<T>> factory;

    public CapabilityTypeBuilderImpl(Class<T> type, Function<Entity, Optional<T>> factory) {
        this.type = type;
        this.factory = factory;
    }

    @Override
    public IRegistryHolder<ICapabilityType<T>> build(String name) {
        var registryName = ModConstants.key(name);
        var capabilityType = new ICapabilityType<T>() {
            @Override
            public Optional<T> get(Entity entity) {
                return CapabilityStorage.getCapability(entity, this);
            }
        };
        ModLog.debug("Registering Capability Type '{}'", registryName);
        CapabilityStorage.registerCapability(registryName, capabilityType, factory);
        return TypedRegistry.Entry.ofValue(registryName, capabilityType);
    }
}
