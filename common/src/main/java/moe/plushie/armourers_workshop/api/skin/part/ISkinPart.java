package moe.plushie.armourers_workshop.api.skin.part;

import moe.plushie.armourers_workshop.api.core.math.ITransform;
import moe.plushie.armourers_workshop.api.skin.ISkinMarker;
import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometrySet;

import java.util.Collection;

public interface ISkinPart {

    /**
     * Gets the part type.
     */
    ISkinPartType getType();

    /**
     * Gets the transform.
     */
    ITransform getTransform();

    /**
     * Gets the geometry set.
     */
    ISkinGeometrySet<?> getGeometries();

    /**
     * Gets the children.
     */
    Collection<? extends ISkinPart> getChildren();

    /**
     * Gets the markers.
     */
    Collection<? extends ISkinMarker> getMarkers();
}
