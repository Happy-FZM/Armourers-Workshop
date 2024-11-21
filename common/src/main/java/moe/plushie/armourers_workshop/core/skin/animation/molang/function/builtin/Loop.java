package moe.plushie.armourers_workshop.core.skin.animation.molang.function.builtin;

import moe.plushie.armourers_workshop.core.skin.animation.molang.core.ExecutionContext;
import moe.plushie.armourers_workshop.core.skin.animation.molang.core.Expression;
import moe.plushie.armourers_workshop.core.skin.animation.molang.core.Result;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.Function;
import moe.plushie.armourers_workshop.core.skin.animation.molang.runtime.MathHelper;

import java.util.List;

// Parameters:
// - double:           How many times should we loop
// - CallableBinding:  The looped expressions

// loop(<count>, <expression>);
public final class Loop extends Function {

    private final Expression count;
    private final Expression body;

    public Loop(Expression name, List<Expression> arguments) {
        super(name, 2, arguments);
        this.count = arguments.get(0);
        this.body = arguments.get(1);
    }

    @Override
    public double compute(final ExecutionContext context) {
        return evaluate(context).getAsDouble();
    }

    @Override
    public Result evaluate(final ExecutionContext context) {
        var scope = context.stack().scope().beginEnumerate();
        // The maximum loop counter is (as of this document being written) 1024.
        int total = MathHelper.floor(MathHelper.clamp(count.compute(context), 0, 1024));
        for (int i = 0; i < total; i++) {
            body.evaluate(context);
            if (scope.isBreakOrReturn()) {
                break;
            }
        }
        return scope.endEnumerate();
    }

    public Expression count() {
        return count;
    }

    public Expression body() {
        return body;
    }
}
