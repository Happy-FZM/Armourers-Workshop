package moe.plushie.armourers_workshop.core.math;

import moe.plushie.armourers_workshop.api.core.math.IPoseStack;
import moe.plushie.armourers_workshop.api.core.math.ITransform;
import moe.plushie.armourers_workshop.api.core.math.ITransform3f;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IInputStream;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IOutputStream;
import moe.plushie.armourers_workshop.core.utils.Objects;

import java.io.IOException;

public class OpenTransform3f implements ITransform3f, ITransform {

    public static final int BYTES = Vector3f.BYTES * 5 + Integer.BYTES;

    public static final OpenTransform3f IDENTITY = new OpenTransform3f();

    private Vector3f translate = Vector3f.ZERO;
    private Vector3f rotation = Vector3f.ZERO;
    private Vector3f scale = Vector3f.ONE;
    private Vector3f afterTranslate = Vector3f.ZERO;
    private Vector3f pivot = Vector3f.ZERO;

    public static OpenTransform3f create(Vector3f translate, Vector3f rotation, Vector3f scale) {
        return create(translate, rotation, scale, Vector3f.ZERO, Vector3f.ZERO);
    }

    public static OpenTransform3f create(Vector3f translate, Vector3f rotation, Vector3f scale, Vector3f pivot, Vector3f afterTranslate) {
        //
        if (translate.equals(Vector3f.ZERO) && rotation.equals(Vector3f.ZERO) && scale.equals(Vector3f.ONE) && pivot.equals(Vector3f.ZERO) && afterTranslate.equals(Vector3f.ZERO)) {
            return IDENTITY;
        }
        var transform = new OpenTransform3f();
        transform.translate = optimize(translate, Vector3f.ZERO);
        transform.rotation = optimize(rotation, Vector3f.ZERO);
        transform.scale = optimize(scale, Vector3f.ONE);
        transform.afterTranslate = optimize(afterTranslate, Vector3f.ZERO);
        transform.pivot = optimize(pivot, Vector3f.ZERO);
        return transform;
    }

    public static OpenTransform3f createRotationTransform(Vector3f rotation) {
        if (!rotation.equals(Vector3f.ZERO)) {
            var transform = new OpenTransform3f();
            transform.rotation = rotation;
            return transform;
        }
        return IDENTITY;
    }

    public static OpenTransform3f createScaleTransform(float sx, float sy, float sz) {
        if (sx != 1 || sy != 1 || sz != 1) {
            var transform = new OpenTransform3f();
            transform.scale = new Vector3f(sx, sy, sz);
            return transform;
        }
        return IDENTITY;
    }

    public static OpenTransform3f createScaleTransform(Vector3f scale) {
        if (!scale.equals(Vector3f.ONE)) {
            var transform = new OpenTransform3f();
            transform.scale = scale;
            return transform;
        }
        return IDENTITY;
    }

    public static OpenTransform3f createTranslateTransform(float tx, float ty, float tz) {
        if (tx != 0 || ty != 0 || tz != 0) {
            var transform = new OpenTransform3f();
            transform.translate = new Vector3f(tx, ty, tz);
            return transform;
        }
        return IDENTITY;
    }

    public static OpenTransform3f createTranslateTransform(Vector3f offset) {
        if (!offset.equals(Vector3f.ZERO)) {
            var transform = new OpenTransform3f();
            transform.translate = offset;
            return transform;
        }
        return IDENTITY;
    }

    @Override
    public void apply(IPoseStack poseStack) {
        if (this == IDENTITY) {
            return;
        }
        if (translate != Vector3f.ZERO) {
            poseStack.translate(translate.getX(), translate.getY(), translate.getZ());
        }
        if (rotation != Vector3f.ZERO) {
            if (pivot != Vector3f.ZERO) {
                poseStack.translate(pivot.getX(), pivot.getY(), pivot.getZ());
            }
            poseStack.rotate(OpenQuaternion3f.fromZYX(rotation, true));
            if (pivot != Vector3f.ZERO) {
                poseStack.translate(-pivot.getX(), -pivot.getY(), -pivot.getZ());
            }
        }
        if (scale != Vector3f.ONE) {
            poseStack.scale(scale.getX(), scale.getY(), scale.getZ());
        }
        if (afterTranslate != Vector3f.ZERO) {
            poseStack.translate(afterTranslate.getX(), afterTranslate.getY(), afterTranslate.getZ());
        }
    }

    public void readFromStream(IInputStream stream) throws IOException {
        int flags = stream.readInt();
        translate = optimize(stream.readVector3f(), Vector3f.ZERO);
        rotation = optimize(stream.readVector3f(), Vector3f.ZERO);
        scale = optimize(stream.readVector3f(), Vector3f.ONE);
        afterTranslate = optimize(stream.readVector3f(), Vector3f.ZERO);
        pivot = optimize(stream.readVector3f(), Vector3f.ZERO);
    }

    public void writeToStream(IOutputStream stream) throws IOException {
        stream.writeInt(0);
        stream.writeVector3f(translate);
        stream.writeVector3f(rotation);
        stream.writeVector3f(scale);
        stream.writeVector3f(afterTranslate);
        stream.writeVector3f(pivot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpenTransform3f that)) return false;
        return translate.equals(that.translate) && rotation.equals(that.rotation) && scale.equals(that.scale) && pivot.equals(that.pivot) && afterTranslate.equals(that.afterTranslate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(translate, rotation, scale, pivot, afterTranslate);
    }

    @Override
    public String toString() {
        return Objects.toString(this, "translate", translate, "rotation", rotation, "scale", scale, "pivot", pivot, "offset", afterTranslate);
    }

    @Override
    public boolean isIdentity() {
        return this == IDENTITY;
    }

    public OpenTransform3f copy() {
        if (this == IDENTITY) {
            return IDENTITY;
        }
        var transform = new OpenTransform3f();
        transform.translate = translate;
        transform.rotation = translate;
        transform.scale = translate;
        transform.pivot = translate;
        transform.afterTranslate = translate;
        return transform;
    }

    @Override
    public Vector3f getTranslate() {
        return translate;
    }

    @Override
    public Vector3f getRotation() {
        return rotation;
    }

    @Override
    public Vector3f getScale() {
        return scale;
    }

    @Override
    public Vector3f getAfterTranslate() {
        return afterTranslate;
    }

    @Override
    public Vector3f getPivot() {
        return pivot;
    }

    private static <T> T optimize(T value, T targetValue) {
        if (value.equals(targetValue)) {
            return targetValue;
        }
        return value;
    }
}
