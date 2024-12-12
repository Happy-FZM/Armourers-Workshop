package moe.plushie.armourers_workshop.api.skin.part;

import moe.plushie.armourers_workshop.api.core.math.IVector2i;
import moe.plushie.armourers_workshop.api.core.math.IVector3i;

public interface ISkinPartTypeTextured extends ISkinPartType {

    /**
     * Location of the texture in skin storage.
     */
    IVector2i getTextureSkinPos();

    /**
     * Size of the model the texture is used on.
     *
     * @return
     */
    IVector3i getTextureModelSize();
}
