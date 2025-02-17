package moe.plushie.armourers_workshop.core.math;

import moe.plushie.armourers_workshop.api.core.IDataCodec;
import moe.plushie.armourers_workshop.api.core.math.IMatrix3f;
import moe.plushie.armourers_workshop.api.core.math.IMatrix4f;
import moe.plushie.armourers_workshop.api.core.math.IVector3f;
import moe.plushie.armourers_workshop.api.core.math.IVector3i;
import moe.plushie.armourers_workshop.core.utils.Collections;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class Vector3f implements Comparable<Vector3f>, IVector3f {

    public static final int BYTES = Float.BYTES * 3;

    public static Vector3f ZERO = new Vector3f(0.0F, 0.0F, 0.0F);
    public static Vector3f ONE = new Vector3f(1.0f, 1.0F, 1.0F);

    public static Vector3f XN = new Vector3f(-1.0F, 0.0F, 0.0F);
    public static Vector3f XP = new Vector3f(1.0F, 0.0F, 0.0F);
    public static Vector3f YN = new Vector3f(0.0F, -1.0F, 0.0F);
    public static Vector3f YP = new Vector3f(0.0F, 1.0F, 0.0F);
    public static Vector3f ZN = new Vector3f(0.0F, 0.0F, -1.0F);
    public static Vector3f ZP = new Vector3f(0.0F, 0.0F, 1.0F);

    public static final IDataCodec<Vector3f> CODEC = IDataCodec.FLOAT.listOf().xmap(Vector3f::new, Vector3f::toList);

    private float x;
    private float y;
    private float z;

    public Vector3f() {
    }

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f(double x, double y, double z) {
        this((float) x, (float) y, (float) z);
    }

    public Vector3f(IVector3f pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vector3f(IVector3i pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vector3f(List<Float> values) {
        this(values.get(0), values.get(1), values.get(2));
    }

    public Vector3f(float[] values) {
        set(values);
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public float getZ() {
        return z;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vector3f pos) {
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
    }

    public void set(float[] values) {
        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
    }

    public void add(float tx, float ty, float tz) {
        x += tx;
        y += ty;
        z += tz;
    }

    public void add(Vector3f pos) {
        x += pos.x;
        y += pos.y;
        z += pos.z;
    }

    public void subtract(float tx, float ty, float tz) {
        x -= tx;
        y -= ty;
        z -= tz;
    }

    public void subtract(Vector3f pos) {
        x -= pos.x;
        y -= pos.y;
        z -= pos.z;
    }

    public void scale(float scale) {
        x *= scale;
        y *= scale;
        z *= scale;
    }

    public void scale(float sx, float sy, float sz) {
        x *= sx;
        y *= sy;
        z *= sz;
    }

    public void scale(Vector3f pos) {
        x *= pos.x;
        y *= pos.y;
        z *= pos.z;
    }

    public void transform(IMatrix3f mat) {
        float[] floats = {x, y, z};
        mat.multiply(floats);
        set(floats[0], floats[1], floats[2]);
    }

    public void transform(IMatrix4f mat) {
        float[] floats = {x, y, z, 1f};
        mat.multiply(floats);
        set(floats[0], floats[1], floats[2]);
    }

    public void transform(OpenQuaternion3f value) {
        var quaternion = new OpenQuaternion3f(value);
        quaternion.mul(new OpenQuaternion3f(x, y, z, 0.0F));
        var quaternion1 = new OpenQuaternion3f(value);
        quaternion1.conjugate();
        quaternion.mul(quaternion1);
        set(quaternion.x(), quaternion.y(), quaternion.z());
    }

    public void normalize() {
        var scalar = OpenMath.invsqrt(OpenMath.fma(x, x, OpenMath.fma(y, y, z * z)));
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
    }

    public void cross(Vector3f pos) {
        float ax = x;
        float ay = y;
        float az = z;
        float bx = pos.getX();
        float by = pos.getY();
        float bz = pos.getZ();
        this.x = ay * bz - az * by;
        this.y = az * bx - ax * bz;
        this.z = ax * by - ay * bx;
    }

    public void clamp(float minValue, float maxValue) {
        x = OpenMath.clamp(x, minValue, maxValue);
        y = OpenMath.clamp(y, minValue, maxValue);
        z = OpenMath.clamp(z, minValue, maxValue);
    }

    public void lerp(Vector3f pos, float f) {
        float f1 = 1.0F - f;
        this.x = x * f1 + pos.x * f;
        this.y = y * f1 + pos.y * f;
        this.z = z * f1 + pos.z * f;
    }

    public float dot(Vector3f pos) {
        return x * pos.x + y * pos.y + z * pos.z;
    }

    public float length() {
        return OpenMath.sqrt(OpenMath.fma(x, x, OpenMath.fma(y, y, z * z)));
    }

    /**
     * Computes distance of Vector3 vector to pos.
     */
    public float distanceTo(Vector3f pos) {
        return OpenMath.sqrt(distanceToSquared(pos));
    }

    /**
     * Computes squared distance of Vector3 vector to v.
     */
    public float distanceToSquared(Vector3f pos) {
        return distanceToSquared(pos.x, pos.y, pos.z);
    }

    public float distanceToSquared(float tx, float ty, float tz) {
        float dx = x - tx;
        float dy = y - ty;
        float dz = z - tz;
        return OpenMath.fma(dx, dx, OpenMath.fma(dy, dy, dz * dz));
    }

    public OpenQuaternion3f rotation(float f) {
        return new OpenQuaternion3f(this, f, false);
    }

    public OpenQuaternion3f rotationDegrees(float f) {
        return new OpenQuaternion3f(this, f, true);
    }

    public Vector3f adding(float tx, float ty, float tz) {
        var ret = copy();
        ret.add(tx, ty, tz);
        return ret;
    }

    public Vector3f adding(Vector3f pos) {
        var ret = copy();
        ret.add(pos);
        return ret;
    }

    public Vector3f subtracting(float tx, float ty, float tz) {
        var ret = copy();
        ret.subtract(tx, ty, tz);
        return ret;
    }

    public Vector3f subtracting(Vector3f pos) {
        var ret = copy();
        ret.subtract(pos);
        return ret;
    }

    public Vector3f scaling(float scale) {
        var ret = copy();
        ret.scale(scale);
        return ret;
    }

    public Vector3f scaling(float sx, float sy, float sz) {
        var ret = copy();
        ret.scale(sx, sy, sz);
        return ret;
    }

    public Vector3f scaling(Vector3f pos) {
        var ret = copy();
        ret.scale(pos);
        return ret;
    }

    public Vector3f transforming(IMatrix3f mat) {
        var ret = copy();
        ret.transform(mat);
        return ret;
    }

    public Vector3f transforming(IMatrix4f mat) {
        var ret = copy();
        ret.transform(mat);
        return ret;
    }

    public Vector3f transforming(OpenQuaternion3f value) {
        var ret = copy();
        ret.transform(value);
        return ret;
    }

    public Vector3f normalizing() {
        var ret = copy();
        ret.normalize();
        return ret;
    }

    public Vector3f crossing(Vector3f pos) {
        var ret = copy();
        ret.cross(pos);
        return ret;
    }

    public Vector3f clamping(float minValue, float maxValue) {
        var ret = copy();
        ret.clamp(minValue, maxValue);
        return ret;
    }

    public Vector3f copy() {
        return new Vector3f(x, y, z);
    }

    public List<Float> toList() {
        return Collections.newList(x, y, z);
    }

    @Override
    public int compareTo(Vector3f v) {
        int dy = Float.compare(getY(), v.getY());
        if (dy != 0) {
            return dy;
        }
        int dz = Float.compare(getZ(), v.getZ());
        if (dz != 0) {
            return dz;
        }
        return Float.compare(getX(), v.getX());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector3f that)) return false;
        return Float.compare(x, that.x) == 0 && Float.compare(y, that.y) == 0 && Float.compare(z, that.z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("(%g %g %g)", x, y, z);
    }
}

