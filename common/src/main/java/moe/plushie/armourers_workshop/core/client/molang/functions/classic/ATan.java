package moe.plushie.armourers_workshop.core.client.molang.functions.classic;

import moe.plushie.armourers_workshop.core.client.molang.math.IValue;
import moe.plushie.armourers_workshop.core.client.molang.functions.Function;

/**
 * Absolute value function
 */
public class ATan extends Function {

    public ATan(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double get() {
        return Math.atan(this.getArg(0));
    }
}
