package moe.plushie.armourers_workshop.compatibility.forge;


import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.core.IResourceLocation;
import moe.plushie.armourers_workshop.init.ModConstants;
import moe.plushie.armourers_workshop.core.utils.TypedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Available("[1.21, )")
public class AbstractForgeRegistry {

    public static <T> TypedRegistry<T> create(String name, Class<?> type, ResourceKey<Registry<T>> registryKey) {
        return TypedRegistry.factory(name, type, build(DeferredRegister.create(registryKey, ModConstants.MOD_ID)));
    }

    public static <T> TypedRegistry<T> create(String name, Class<?> type, Registry<T> registry) {
        return TypedRegistry.create(name, type, registry::getKey, registry::get, build(DeferredRegister.create(registry, ModConstants.MOD_ID)));
    }

    private static <T> TypedRegistry.RegisterProvider<T> build(DeferredRegister<T> register) {
        // auto register
        register.register(AbstractForgeInitializer.getModEventBus());
        return new TypedRegistry.RegisterProvider<T>() {
            @Override
            public <I extends T> Supplier<I> register(IResourceLocation registryName, Supplier<? extends I> provider) {
                return register.register(registryName.getPath(), provider);
            }
        };
    }
}
