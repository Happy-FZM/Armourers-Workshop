package moe.plushie.armourers_workshop.core.skin.part.wings;

import moe.plushie.armourers_workshop.api.core.math.IPoseStack;
import moe.plushie.armourers_workshop.api.core.math.ITransform;
import moe.plushie.armourers_workshop.api.skin.part.ISkinPartType;
import moe.plushie.armourers_workshop.api.skin.part.features.ICanRotation;
import moe.plushie.armourers_workshop.core.math.Vector3f;
import moe.plushie.armourers_workshop.core.skin.SkinMarker;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperties;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperty;
import moe.plushie.armourers_workshop.core.utils.OpenDirection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class WingPartTransform implements ITransform {

    private double animationTime = 0.0;
    private boolean isFallFlying = false;

    private final boolean isMirror;

    private final SkinMarker marker;
    private final SkinProperties properties;

    public WingPartTransform(ISkinPartType partType, SkinProperties properties, SkinMarker marker) {
        this.marker = marker;
        this.properties = properties;
        this.isMirror = partType instanceof ICanRotation rotatableType && rotatableType.isMirror();
    }

    public static boolean isFlying(LivingEntity entity) {
        // the player maybe is in creative flying.
        if (entity instanceof Player player && player.getAbilities().flying) {
            return true;
        }
        return entity.isFallFlying();
    }

    public void setup(@Nullable Entity entity, double animationTime) {
        this.animationTime = animationTime;
        this.isFallFlying = entity instanceof LivingEntity livingEntity && isFlying(livingEntity);
    }

    @Override
    public void apply(IPoseStack poseStack) {
        var dir = marker.getDirection();
        if (dir == null) {
            return;
        }
        var point = marker.getPosition();
        var angle = (float) getRotationDegrees();
        var offset = new Vector3f(point.getX() + 0.5f, point.getY() + 0.5f, point.getZ() + 0.5f);
        if (!isMirror) {
            angle = -angle;
        }

        poseStack.translate(offset.getX(), offset.getY(), offset.getZ());
        poseStack.rotate(getRotationMatrix(dir).rotationDegrees(angle));
        poseStack.translate(-offset.getX(), -offset.getY(), -offset.getZ());
    }

    private double getRotationDegrees() {
        if (properties == null) {
            return 0;
        }

        var maxAngle = properties.get(SkinProperty.WINGS_MAX_ANGLE);
        var minAngle = properties.get(SkinProperty.WINGS_MIN_ANGLE);
        var movementTypeName = properties.get(SkinProperty.WINGS_MOVMENT_TYPE);
        var movementType = SkinProperty.MovementType.valueOf(movementTypeName);

        var flapTime = properties.get(SkinProperty.WINGS_IDLE_SPEED);
        if (isFallFlying) {
            flapTime = properties.get(SkinProperty.WINGS_FLYING_SPEED);
        }

        var finalFlapTime = Math.max(flapTime / 1000f, 0.1f);
        var angle = animationTime % finalFlapTime;

        if (movementType == SkinProperty.MovementType.EASE) {
            angle = Math.sin(angle / finalFlapTime * Math.PI * 2);
        }
        if (movementType == SkinProperty.MovementType.LINEAR) {
            angle = angle / finalFlapTime;
        }

        var fullAngle = maxAngle - minAngle;
        if (movementType == SkinProperty.MovementType.LINEAR) {
            return fullAngle * angle;
        }

        return -minAngle - fullAngle * ((angle + 1D) / 2);
    }

    private Vector3f getRotationMatrix(OpenDirection direction) {
        return switch (direction) {
            case UP -> Vector3f.YP;
            case DOWN -> Vector3f.YN;
            case SOUTH -> Vector3f.ZN;
            case NORTH -> Vector3f.ZP;
            case EAST -> Vector3f.XP;
            case WEST -> Vector3f.XN;
        };
    }
}
