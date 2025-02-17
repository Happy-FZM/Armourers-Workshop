package moe.plushie.armourers_workshop.compatibility.extensions.net.minecraft.world.entity.EntityDimensions;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.core.data.EntityCollisionShape;
import net.minecraft.world.entity.EntityDimensions;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

@Available("[1.16, )")
@Extension
public class CollisionShape {

    public static EntityDimensions withCollisionShape(@This EntityDimensions dimensions, EntityCollisionShape shape) {
        var size = shape.getSize();
        float newWidth = size.getWidth();
        float newHeight = size.getHeight();
        return EntityDimensions.scalable(newWidth, newHeight);
    }
}
