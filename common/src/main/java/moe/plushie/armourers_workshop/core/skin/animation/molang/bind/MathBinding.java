package moe.plushie.armourers_workshop.core.skin.animation.molang.bind;

import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.ACos;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.ASin;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.ATan;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.ATan2;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Abs;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Ceil;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Clamp;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Cos;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.DieRoll;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.DieRollInteger;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Exp;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Floor;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.HermiteBlend;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Lerp;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.LerpRotate;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Log;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Max;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Min;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.MinAngle;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Mod;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Pow;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Random;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.RandomInteger;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Round;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Sin;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Sqrt;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.ToDeg;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.ToRad;
import moe.plushie.armourers_workshop.core.skin.animation.molang.function.math.Truncate;

public class MathBinding extends ContextBinding {

    public MathBinding() {
        // Constant values
        constant("pi", Math.PI);
        constant("e", Math.E);

        // Rounding functions
        function("floor", Floor::new);
        function("round", Round::new);
        function("ceil", Ceil::new);
        function("trunc", Truncate::new);

        // Selection and limit functions
        function("clamp", Clamp::new);
        function("max", Max::new);
        function("min", Min::new);

        // Classical functions
        function("abs", Abs::new);
        function("acos", ACos::new);
        function("asin", ASin::new);
        function("atan", ATan::new);
        function("atan2", ATan2::new);
        function("cos", Cos::new); // degree
        function("sin", Sin::new); // degree
        function("exp", Exp::new);
        function("ln", Log::new);
        function("sqrt", Sqrt::new);
        function("mod", Mod::new);
        function("pow", Pow::new);

        // Utility functions
        function("lerp", Lerp::new);
        function("lerprotate", LerpRotate::new);
        function("hermite_blend", HermiteBlend::new);
        function("die_roll", DieRoll::new);
        function("die_roll_integer", DieRollInteger::new);
        function("random", Random::new);
        function("random_integer", RandomInteger::new);

        // Other functions
        function("to_deg", ToDeg::new);
        function("to_rad", ToRad::new);
        function("min_angle", MinAngle::new);

        // Geckolib functions
        function("randomi", RandomInteger::new);
        function("roll", DieRoll::new);
        function("rolli", DieRollInteger::new);
        function("hermite", HermiteBlend::new);
    }
}
