package moe.plushie.armourers_workshop.core.skin.animation.molang.function.query;

import moe.plushie.armourers_workshop.core.skin.animation.molang.bind.selector.EntitySelector;
import moe.plushie.armourers_workshop.core.skin.animation.molang.core.ExecutionContext;
import moe.plushie.armourers_workshop.core.skin.animation.molang.core.Expression;
import moe.plushie.armourers_workshop.core.skin.animation.molang.core.Result;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.EntityFunction;

import java.util.List;

public class RelativeBlockName extends EntityFunction {

    private final Expression offsetX;
    private final Expression offsetY;
    private final Expression offsetZ;

    public RelativeBlockName(Expression name, List<Expression> arguments) {
        super(name, 3, arguments);
        this.offsetX = arguments.get(0);
        this.offsetY = arguments.get(1);
        this.offsetZ = arguments.get(2);
    }

    @Override
    public double compute(final EntitySelector entity, final ExecutionContext context) {
        return 0;
    }

    @Override
    public Result evaluate(EntitySelector entity, ExecutionContext context) {
        int offsetX = this.offsetX.evaluate(context).getAsInt();
        int offsetY = this.offsetY.evaluate(context).getAsInt();
        int offsetZ = this.offsetZ.evaluate(context).getAsInt();
        // query limit
        if (Math.abs(offsetX) > 8 || Math.abs(offsetY) > 8 || Math.abs(offsetZ) > 8) {
            return Result.NULL; // too far
        }
        var block = entity.getRelativeBlock(offsetX, offsetY, offsetZ);
        if (block == null) {
            return Result.NULL; // can't found.
        }
        return Result.valueOf(block.getId());
    }
}
