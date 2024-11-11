package moe.plushie.armourers_workshop.init.platform.fabric.builder;

import moe.plushie.armourers_workshop.api.core.IRegistryHolder;
import moe.plushie.armourers_workshop.api.registry.ISoundEventBuilder;
import moe.plushie.armourers_workshop.compatibility.fabric.AbstractFabricRegistries;
import moe.plushie.armourers_workshop.core.utils.TypedRegistry;
import moe.plushie.armourers_workshop.init.ModConstants;
import net.minecraft.sounds.SoundEvent;

public class SoundEventBuilderImpl<T extends SoundEvent> implements ISoundEventBuilder<T> {

    @Override
    public IRegistryHolder<T> build(String name) {
        var registryName = ModConstants.key(name);
        var event = SoundEvent.createVariableRangeEvent(registryName.toLocation());
        AbstractFabricRegistries.SOUND_EVENTS.register(name, () -> event);
        return TypedRegistry.Entry.cast(registryName, () -> event);
    }
}
