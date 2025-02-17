package moe.plushie.armourers_workshop.api.registry;

import moe.plushie.armourers_workshop.api.common.IItemGroup;
import moe.plushie.armourers_workshop.api.core.IRegistryHolder;
import moe.plushie.armourers_workshop.compatibility.client.AbstractItemStackRendererProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface IItemBuilder<T extends Item> extends IRegistryBuilder<T> {

    IItemBuilder<T> stacksTo(int i);

    IItemBuilder<T> durability(int i);

    IItemBuilder<T> craftRemainder(Item item);

    IItemBuilder<T> group(IRegistryHolder<IItemGroup> group);

    IItemBuilder<T> rarity(Rarity rarity);

    IItemBuilder<T> fireResistant();

    IItemBuilder<T> bind(Supplier<AbstractItemStackRendererProvider> provider);

}
