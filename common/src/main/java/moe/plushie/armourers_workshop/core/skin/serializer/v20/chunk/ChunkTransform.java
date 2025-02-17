package moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk;

import moe.plushie.armourers_workshop.api.core.math.IPoseStack;
import moe.plushie.armourers_workshop.api.core.math.ITransform;
import moe.plushie.armourers_workshop.core.math.OpenMatrix3f;
import moe.plushie.armourers_workshop.core.math.OpenMatrix4f;
import moe.plushie.armourers_workshop.core.math.OpenPoseStack;
import moe.plushie.armourers_workshop.core.math.OpenTransform3f;
import moe.plushie.armourers_workshop.core.math.Vector3f;

import java.io.IOException;
import java.nio.FloatBuffer;

public class ChunkTransform {

    private static final float[] IDENTITY_MATRIX_BUFFER = new float[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 0,
    };

    private static final float[] IDENTITY_VECTOR_BUFFER = new float[]{
            0, 0, 0, // translate
            0, 0, 0, // rotation
            1, 1, 1, // scale
            0, 0, 0, // offset
            0, 0, 0, // pivot
    };

    private Vector3f translate;
    private Vector3f rotation;
    private Vector3f scale;
    private Vector3f afterTranslate;
    private Vector3f pivot;

    private FloatBuffer buffer;

    public ChunkTransform() {
    }

    public ChunkTransform(OpenTransform3f transform) {
        if (transform.isIdentity()) {
            this.setIdentity();
            return;
        }
        this.translate = transform.getTranslate();
        this.rotation = transform.getRotation();
        this.scale = transform.getScale();
        this.afterTranslate = transform.getAfterTranslate();
        this.pivot = transform.getPivot();
    }

    public ChunkTransform(FloatBuffer buffer) {
        this.buffer = buffer;
    }

    public static ChunkTransform of(ITransform transform) {
        if (transform instanceof OpenTransform3f) {
            return new ChunkTransform((OpenTransform3f) transform);
        }
        return flat(transform);
    }

    public static ChunkTransform flat(ITransform transform) {
        var buffer = FloatBuffer.allocate(16);
        var poseStack = new OpenPoseStack();
        transform.apply(poseStack);
        poseStack.last().pose().store(buffer);
        return new ChunkTransform(buffer);
    }


    public void readFromStream(ChunkDataInputStream stream) throws IOException {
        int flags = stream.readByte();
        if ((flags & 0x10) != 0) {
            setIdentity();
            return;
        }
        if ((flags & 0x20) != 0) {
            buffer = FloatBuffer.allocate(16);
            readZippedBuffer(stream, buffer, IDENTITY_MATRIX_BUFFER);
            return;
        }
        var buffer = FloatBuffer.allocate(IDENTITY_VECTOR_BUFFER.length);
        readZippedBuffer(stream, buffer, IDENTITY_VECTOR_BUFFER);
        translate = readVector(buffer, 0);
        rotation = readVector(buffer, 3);
        scale = readVector(buffer, 6);
        afterTranslate = readVector(buffer, 9);
        pivot = readVector(buffer, 12);
    }

    public void writeToStream(ChunkDataOutputStream stream) throws IOException {
        if (isIdentity()) {
            stream.writeByte(0x10);
            return;
        }
        if (buffer != null) {
            stream.writeByte(0x20);
            writeZippedBuffer(stream, buffer, IDENTITY_MATRIX_BUFFER);
            return;
        }
        stream.writeByte(0x40);
        var buffer = FloatBuffer.allocate(IDENTITY_VECTOR_BUFFER.length);
        buffer.put(translate.getX()).put(translate.getY()).put(translate.getZ());
        buffer.put(rotation.getX()).put(rotation.getY()).put(rotation.getZ());
        buffer.put(scale.getX()).put(scale.getY()).put(scale.getZ());
        buffer.put(afterTranslate.getX()).put(afterTranslate.getY()).put(afterTranslate.getZ());
        buffer.put(pivot.getX()).put(pivot.getY()).put(pivot.getZ());
        buffer.rewind();
        writeZippedBuffer(stream, buffer, IDENTITY_VECTOR_BUFFER);
    }

    public void setIdentity() {
        pivot = null;
        translate = null;
        scale = null;
        rotation = null;
        buffer = null;
    }

    public boolean isIdentity() {
        return buffer == null && scale == null;
    }

    public ITransform build() {
        if (isIdentity()) {
            return OpenTransform3f.IDENTITY;
        }
        if (buffer != null) {
            var pose = new OpenMatrix4f(buffer);
            var normal = new OpenMatrix3f(buffer);
            return new FlatTransform(pose, normal);
        }
        return OpenTransform3f.create(translate, rotation, scale, pivot, afterTranslate);
    }

    private static Vector3f readVector(FloatBuffer buffer, int offset) {
        float x = buffer.get(offset);
        float y = buffer.get(offset + 1);
        float z = buffer.get(offset + 2);
        if (x == 0 && y == 0 && z == 0) {
            return Vector3f.ZERO;
        }
        if (x == 1 && y == 1 && z == 1) {
            return Vector3f.ONE;
        }
        return new Vector3f(x, y, z);
    }


    private static void readZippedBuffer(ChunkDataInputStream stream, FloatBuffer bufferOut, float[] bufferDef) throws IOException {
        int flags = stream.readShort();
        for (int i = 0; i < bufferDef.length; ++i) {
            if ((flags & (1 << i)) == 0) {
                bufferOut.put(i, stream.readFloat());
            } else {
                bufferOut.put(i, bufferDef[i]);
            }
        }
        bufferOut.rewind();
    }

    private static void writeZippedBuffer(ChunkDataOutputStream stream, FloatBuffer bufferIn, float[] bufferDef) throws IOException {
        int flags = 0;
        for (int i = 0; i < bufferDef.length; ++i) {
            if (bufferDef[i] == bufferIn.get(i)) {
                flags |= 1 << i;
            }
        }
        stream.writeShort(flags);
        for (int i = 0; i < bufferDef.length; ++i) {
            if ((flags & (1 << i)) == 0) {
                stream.writeFloat(bufferIn.get(i));
            }
        }
    }

    public static class FlatTransform implements ITransform {

        private final OpenMatrix4f pose;
        private final OpenMatrix3f normal;

        public FlatTransform(OpenMatrix4f pose, OpenMatrix3f normal) {
            this.pose = pose;
            this.normal = normal;
        }

        @Override
        public void apply(IPoseStack poseStack) {
            poseStack.multiply(normal);
            poseStack.multiply(pose);
        }
    }
}
