package moe.plushie.armourers_workshop.core.client.skinrender.plugin;

import moe.plushie.armourers_workshop.core.armature.ArmaturePlugin;
import moe.plushie.armourers_workshop.core.armature.ArmatureTransformerContext;
import moe.plushie.armourers_workshop.utils.math.OpenQuaternionf;
import moe.plushie.armourers_workshop.utils.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class FishingModelArmaturePlugin extends ArmaturePlugin {

    public FishingModelArmaturePlugin(ArmatureTransformerContext context) {
    }

    @Override
    public void activate(Entity entity, Context context) {
        var poseStack = context.getPoseStack();
        var rotation = Minecraft.getInstance().getCameraOrientation().toYXZ();
        poseStack.rotate(OpenQuaternionf.fromYXZ(rotation.getY(), 0, 0));
        poseStack.rotate(Vector3f.YP.rotationDegrees(180.0f));
        poseStack.translate(0.03125f, 0.1875f, 0); // 0.5, 3, 0
    }
}
