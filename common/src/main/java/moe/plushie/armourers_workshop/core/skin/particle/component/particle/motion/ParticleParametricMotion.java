package moe.plushie.armourers_workshop.core.skin.particle.component.particle.motion;

import moe.plushie.armourers_workshop.core.skin.particle.SkinParticleBuilder;
import moe.plushie.armourers_workshop.core.skin.particle.SkinParticleComponent;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IInputStream;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IOutputStream;
import moe.plushie.armourers_workshop.core.utils.OpenPrimitive;

import java.io.IOException;

public class ParticleParametricMotion extends SkinParticleComponent {

    private final OpenPrimitive relativePositionX;
    private final OpenPrimitive relativePositionY;
    private final OpenPrimitive relativePositionZ;
    private final OpenPrimitive directionX;
    private final OpenPrimitive directionY;
    private final OpenPrimitive directionZ;
    private final OpenPrimitive rotation;

    public ParticleParametricMotion(OpenPrimitive relativePositionX, OpenPrimitive relativePositionY, OpenPrimitive relativePositionZ, OpenPrimitive directionX, OpenPrimitive directionY, OpenPrimitive directionZ, OpenPrimitive rotation) {
        this.relativePositionX = relativePositionX;
        this.relativePositionY = relativePositionY;
        this.relativePositionZ = relativePositionZ;
        this.directionX = directionX;
        this.directionY = directionY;
        this.directionZ = directionZ;
        this.rotation = rotation;
    }

    public ParticleParametricMotion(IInputStream stream) throws IOException {
        this.relativePositionX = stream.readPrimitiveObject();
        this.relativePositionY = stream.readPrimitiveObject();
        this.relativePositionZ = stream.readPrimitiveObject();
        this.directionX = stream.readPrimitiveObject();
        this.directionY = stream.readPrimitiveObject();
        this.directionZ = stream.readPrimitiveObject();
        this.rotation = stream.readPrimitiveObject();
    }

    @Override
    public void writeToStream(IOutputStream stream) throws IOException {
        stream.writePrimitiveObject(relativePositionX);
        stream.writePrimitiveObject(relativePositionY);
        stream.writePrimitiveObject(relativePositionZ);
        stream.writePrimitiveObject(directionX);
        stream.writePrimitiveObject(directionY);
        stream.writePrimitiveObject(directionZ);
        stream.writePrimitiveObject(rotation);
    }

    @Override
    public void applyToBuilder(SkinParticleBuilder builder) throws Exception {
        var relativePositionX = builder.compile(this.relativePositionX, 0.0);
        var relativePositionY = builder.compile(this.relativePositionY, 0.0);
        var relativePositionZ = builder.compile(this.relativePositionZ, 0.0);
        var directionX = builder.compile(this.directionX, 0.0);
        var directionY = builder.compile(this.directionY, 0.0);
        var directionZ = builder.compile(this.directionZ, 0.0);
        var rotation = builder.compile(this.rotation, 0.0);
        builder.applyParticle((emitter, particle, context) -> {
            // TODO: NO IMPL @SAGESSE
//            Vector3f position = new Vector3f((float) this.position[0].get(), (float) this.position[1].get(), (float) this.position[2].get());
//
//        particle.manual = true;
//        particle.initialPosition.set(particle.position);
//
//        particle.matrix.transform(position);
//        particle.position.x = particle.initialPosition.x + position.x;
//        particle.position.y = particle.initialPosition.y + position.y;
//        particle.position.z = particle.initialPosition.z + position.z;
//        particle.rotation = (float) this.rotation.get();
        });
        builder.updateParticle((emitter, particle, context) -> {
            // TODO: NO IMPL @SAGESSE
//            Vector3f position = new Vector3f((float) this.position[0].get(), (float) this.position[1].get(), (float) this.position[2].get());
//
//        particle.matrix.transform(position);
//        particle.position.x = particle.initialPosition.x + position.x;
//        particle.position.y = particle.initialPosition.y + position.y;
//        particle.position.z = particle.initialPosition.z + position.z;
//        particle.rotation = (float) this.rotation.get();

        });
    }
}
