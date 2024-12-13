package moe.plushie.armourers_workshop.core.skin.particle.component.emitter.shape;

import moe.plushie.armourers_workshop.core.skin.particle.SkinParticleBuilder;
import moe.plushie.armourers_workshop.core.skin.particle.SkinParticleComponent;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IInputStream;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IOutputStream;
import moe.plushie.armourers_workshop.core.utils.OpenPrimitive;

import java.io.IOException;

public class EmitterPointShape extends SkinParticleComponent {

    private final OpenPrimitive x;
    private final OpenPrimitive y;
    private final OpenPrimitive z;
    private final EmitterShapeDirection direction;

    public EmitterPointShape(OpenPrimitive x, OpenPrimitive y, OpenPrimitive z, EmitterShapeDirection direction) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
    }

    public EmitterPointShape(IInputStream stream) throws IOException {
        this.x = stream.readPrimitiveObject();
        this.y = stream.readPrimitiveObject();
        this.z = stream.readPrimitiveObject();
        this.direction = EmitterShapeDirection.readFromStream(stream);
    }

    @Override
    public void writeToStream(IOutputStream stream) throws IOException {
        stream.writePrimitiveObject(x);
        stream.writePrimitiveObject(y);
        stream.writePrimitiveObject(z);
        direction.writeToStream(stream);
    }

    @Override
    public void applyToBuilder(SkinParticleBuilder builder) throws Exception {
        var x = builder.compile(this.x, 0.0);
        var y = builder.compile(this.y, 0.0);
        var z = builder.compile(this.z, 0.0);

        if (!direction.isBuiltin()) {
            // ..
        }

        builder.applyParticle((emitter, particle, context) -> {
            var tx = x.compute(context);
            var ty = y.compute(context);
            var tz = z.compute(context);
            // TODO: NO IMPL @SAGESSE
            //particle.position.x = tx;
            //particle.position.y = ty;
            //particle.position.z = tz;

//            if (this.direction instanceof ShapeDirection.Vector) {
//                this.direction.applyDirection(particle, particle.position.x, particle.position.y, particle.position.z);
//            }
        });
    }
}
