package moe.plushie.armourers_workshop.core.skin.part.chest;

import moe.plushie.armourers_workshop.api.core.math.ITexturePos;
import moe.plushie.armourers_workshop.api.core.math.IVector3i;
import moe.plushie.armourers_workshop.api.skin.part.ISkinPartTypeTextured;
import moe.plushie.armourers_workshop.core.math.Rectangle3i;
import moe.plushie.armourers_workshop.core.math.TexturePos;
import moe.plushie.armourers_workshop.core.math.Vector3i;
import moe.plushie.armourers_workshop.core.skin.part.SkinPartType;

public class LeftArmPartType extends SkinPartType implements ISkinPartTypeTextured {

    public LeftArmPartType() {
        super();
        this.buildingSpace = new Rectangle3i(-19, -28, -16, 24, 44, 32);
        this.guideSpace = new Rectangle3i(-3, -10, -2, 4, 12, 4);
        this.offset = new Vector3i(30, -1, 0);
        this.renderOffset = new Vector3i(5, 2, 0);
        this.renderPolygonOffset = 4;
    }

    @Override
    public boolean isTextureMirrored() {
        return true;
    }

    @Override
    public ITexturePos getTextureSkinPos() {
        return new TexturePos(40, 16);
    }

    @Override
    public ITexturePos getTextureBasePos() {
        return new TexturePos(32, 48);
    }

    @Override
    public ITexturePos getTextureOverlayPos() {
        return new TexturePos(48, 48);
    }

    @Override
    public IVector3i getTextureModelSize() {
        return new Vector3i(4, 12, 4);
    }
}
