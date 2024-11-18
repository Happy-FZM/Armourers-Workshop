package moe.plushie.armourers_workshop.core.skin.animation.molang.bind.holder;

import moe.plushie.armourers_workshop.core.skin.animation.molang.core.ExecutionContext;
import moe.plushie.armourers_workshop.core.skin.animation.molang.core.NamedObject;
import moe.plushie.armourers_workshop.core.skin.animation.molang.core.Result;
import moe.plushie.armourers_workshop.core.skin.animation.molang.core.Expression;

public abstract class ConstantHolder extends NamedObject implements Expression {

    @Override
    public abstract Result evaluate(final ExecutionContext context);
}
