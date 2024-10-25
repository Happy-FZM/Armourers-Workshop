package moe.plushie.armourers_workshop.core.skin.geometry.mesh;

import moe.plushie.armourers_workshop.api.skin.geometry.ISkinGeometryType;
import moe.plushie.armourers_workshop.api.skin.paint.texture.ITextureKey;
import moe.plushie.armourers_workshop.core.math.OpenTransform3f;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryFace;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryTypes;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryVertex;

import java.util.List;

public class SkinMeshFace extends SkinGeometryFace {

    protected List<SkinGeometryVertex> vertices;

    public SkinMeshFace(int id, OpenTransform3f transform, ITextureKey textureKey, List<SkinGeometryVertex> vertices) {
        this.id = id;
        this.transform = transform;
        this.textureKey = textureKey;
        this.vertices = vertices;
    }

    @Override
    public ISkinGeometryType getType() {
        return SkinGeometryTypes.MESH;
    }

    @Override
    public List<SkinGeometryVertex> getVertices() {
        return vertices;
    }
}
