package moe.plushie.armourers_workshop.core.skin.animation.molang.function.query;

import moe.plushie.armourers_workshop.core.skin.animation.molang.bind.selector.LivingEntitySelector;
import moe.plushie.armourers_workshop.core.skin.animation.molang.core.ExecutionContext;
import moe.plushie.armourers_workshop.core.skin.animation.molang.core.Expression;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.LivingEntityFunction;

import java.util.List;
import java.util.stream.Collectors;

public class ItemHasAllTags extends LivingEntityFunction {

    private final Expression slot;
    private final List<Expression> tags;

    public ItemHasAllTags(Expression name, List<Expression> arguments) {
        super(name, 2, arguments);
        this.slot = arguments.get(0);
        this.tags = arguments.stream().skip(1).collect(Collectors.toList());
    }

    @Override
    public double compute(final LivingEntitySelector entity, final ExecutionContext context) {
        var item = entity.getEquippedItem(this.slot.evaluate(context).getAsString());
        if (item == null) {
            return 0; // can't found item.
        }
        for (var tag : this.tags) {
            if (!item.hasTag(tag.evaluate(context).getAsString())) {
                return 0; // missing match
            }
        }
        return 1;
    }
}

