package moe.plushie.armourers_workshop.compatibility.forge;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.compatibility.core.AbstractRegistryManager;
import moe.plushie.armourers_workshop.init.environment.EnvironmentExecutor;
import moe.plushie.armourers_workshop.init.environment.EnvironmentType;
import moe.plushie.armourers_workshop.init.platform.EnvironmentManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;
import java.util.function.Predicate;

@Available("[1.21, )")
public class AbstractForgeRegistryManager extends AbstractRegistryManager {

    public static final AbstractForgeRegistryManager INSTANCE = new AbstractForgeRegistryManager();

    @Override
    protected ResourceLocation getItemKey0(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    @Override
    protected ResourceLocation getBlockKey0(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    @Override
    protected Predicate<ItemStack> getItemTag0(ResourceLocation key) {
        var tag = TagKey.create(Registries.ITEM, key);
        return itemStack -> itemStack.is(tag);
    }

    @Override
    protected Predicate<BlockState> getBlockTag0(ResourceLocation key) {
        var tag = TagKey.create(Registries.BLOCK, key);
        return blockState -> blockState.is(tag);
    }

    @Override
    protected Predicate<Biome> getBiomeTag0(ResourceLocation key) {
        var tag = TagKey.create(Registries.BIOME, key);
        return info -> info.getLevel().getBiome(info.getBlockPos()).is(tag);
    }

    @Override
    protected Function<ItemStack, Integer> getEnchantment0(ResourceLocation key) {
        var enchantment = getHolder0(Registries.ENCHANTMENT, key);
        if (enchantment != null) {
            return itemStack -> EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemStack);
        }
        return null;
    }

    @Override
    protected Function<LivingEntity, MobEffectInstance> getEffect0(ResourceLocation key) {
        var effect = getHolder0(Registries.MOB_EFFECT, key);
        if (effect != null) {
            return entity -> entity.getEffect(effect);
        }
        return null;
    }

    @Override
    protected Function<LivingEntity, Double> getAttribute0(ResourceLocation key) {
        var attribute = getHolder0(Registries.ATTRIBUTE, key);
        if (attribute != null) {
            return entity -> entity.getAttributeValue(attribute);
        }
        return null;
    }

    protected <E> Holder<E> getHolder0(ResourceKey<? extends Registry<? extends E>> registryKey, ResourceLocation rl) {
        var registryAccess = findRegistryAccess();
        if (registryAccess != null) {
            var registry = registryAccess.registry(registryKey);
            return registry.flatMap(it -> it.getHolder(rl)).orElse(null);
        }
        return null;
    }

    protected RegistryAccess findRegistryAccess() {
        // find registry access on the server.
        var server = EnvironmentManager.getServer();
        if (server != null) {
            return server.registryAccess();
        }
        // find registry access on the client.
        var client = EnvironmentExecutor.callOn(EnvironmentType.CLIENT, () -> () -> {
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                return connection.registryAccess();
            }
            return null;
        });
        return client.orElse(null);
    }
}
