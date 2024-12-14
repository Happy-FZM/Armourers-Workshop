package moe.plushie.armourers_workshop.core.client.animation;

import moe.plushie.armourers_workshop.core.client.bake.BakedSkin;
import moe.plushie.armourers_workshop.core.client.other.SkinRenderContext;
import moe.plushie.armourers_workshop.core.skin.molang.MolangVirtualMachine;
import moe.plushie.armourers_workshop.core.skin.molang.core.Expression;
import moe.plushie.armourers_workshop.core.skin.molang.runtime.SyntaxException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class AnimationEngine {

    private static final MolangVirtualMachine VM = new MolangVirtualMachine();

    public static void apply(@Nullable Object source, BakedSkin skin, SkinRenderContext context) {
        // If not found it means it is not any animation active.
        var animationContext = context.getAnimationManager().getAnimationContext(skin);
        if (animationContext == null) {
            return;
        }
        VM.beginVariableCaching();
        apply(source, skin.getId(), context.getPartialTicks(), context.getAnimationTicks(), animationContext);
        VM.endVariableCaching();
    }

    public static void apply(@Nullable Object source, int skinId, float partialTick, double animationTime, AnimationContext context) {
        context.beginUpdates(animationTime);
        var executionContext = context.getExecutionContext();
        for (var animationController : context.getAnimationControllers()) {
            // query the current play state of the animation controller.
            var playState = context.getPlayState(animationController);
            if (playState == null) {
                continue;
            }
            // we only bind it when transformer use the molang environment.
            var adjustedTime = playState.getAdjustedTime(animationTime);
            if (animationController.isRequiresVirtualMachine()) {
                executionContext.upload(skinId, playState.getTime(), adjustedTime, animationTime, partialTick);
            }

            // check/switch frames of animation and write to applier.
            animationController.process(adjustedTime, playState, executionContext);
        }
        context.commitUpdates();
    }


    public static Expression compile(String source) throws SyntaxException {
        return VM.compile(source);
    }
}

