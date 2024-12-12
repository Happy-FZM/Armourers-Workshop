package moe.plushie.armourers_workshop.core.skin.particle.component.emitter.shape;

import moe.plushie.armourers_workshop.core.skin.particle.SkinParticleComponent;
import moe.plushie.armourers_workshop.core.skin.particle.SkinParticleDirection;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IInputStream;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IOutputStream;
import moe.plushie.armourers_workshop.core.utils.OpenPrimitive;

import java.io.IOException;

public class EmitterDiscShape extends SkinParticleComponent {

    private final OpenPrimitive x;
    private final OpenPrimitive y;
    private final OpenPrimitive z;

    private final OpenPrimitive radius;

    private final OpenPrimitive planeNormalX;
    private final OpenPrimitive planeNormalY;
    private final OpenPrimitive planeNormalZ;

    private final SkinParticleDirection direction;

    private final boolean surfaceOnly;

    public EmitterDiscShape(OpenPrimitive x, OpenPrimitive y, OpenPrimitive z, OpenPrimitive radius, OpenPrimitive planeNormalX, OpenPrimitive planeNormalY, OpenPrimitive planeNormalZ, SkinParticleDirection direction, boolean surfaceOnly) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.planeNormalX = planeNormalX;
        this.planeNormalY = planeNormalY;
        this.planeNormalZ = planeNormalZ;
        this.direction = direction;
        this.surfaceOnly = surfaceOnly;
    }

    public EmitterDiscShape(IInputStream stream) throws IOException {
        this.x = stream.readPrimitiveObject();
        this.y = stream.readPrimitiveObject();
        this.z = stream.readPrimitiveObject();
        this.radius = stream.readPrimitiveObject();
        this.planeNormalX = stream.readPrimitiveObject();
        this.planeNormalY = stream.readPrimitiveObject();
        this.planeNormalZ = stream.readPrimitiveObject();
        this.direction = SkinParticleDirection.readFromStream(stream);
        this.surfaceOnly = stream.readBoolean();
    }

    @Override
    public void writeToStream(IOutputStream stream) throws IOException {
        stream.writePrimitiveObject(x);
        stream.writePrimitiveObject(y);
        stream.writePrimitiveObject(z);
        stream.writePrimitiveObject(radius);
        stream.writePrimitiveObject(planeNormalX);
        stream.writePrimitiveObject(planeNormalY);
        stream.writePrimitiveObject(planeNormalZ);
        direction.writeToStream(stream);
        stream.writeBoolean(surfaceOnly);
    }
}
