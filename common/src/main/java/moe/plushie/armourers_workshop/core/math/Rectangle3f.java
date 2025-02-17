package moe.plushie.armourers_workshop.core.math;

import moe.plushie.armourers_workshop.api.core.IDataCodec;
import moe.plushie.armourers_workshop.api.core.math.IRectangle3f;
import moe.plushie.armourers_workshop.api.core.math.IRectangle3i;
import moe.plushie.armourers_workshop.api.core.math.IVector3f;
import moe.plushie.armourers_workshop.core.utils.Collections;
import moe.plushie.armourers_workshop.core.utils.Objects;

import java.util.List;

@SuppressWarnings("unused")
public class Rectangle3f implements IRectangle3f {

    public static final int BYTES = Float.BYTES * 6;

    public final static Rectangle3f ZERO = new Rectangle3f(0, 0, 0, 0, 0, 0);

    public static final IDataCodec<Rectangle3f> CODEC = IDataCodec.FLOAT.listOf().xmap(Rectangle3f::new, Rectangle3f::toList);

    private float x;
    private float y;
    private float z;
    private float width;
    private float height;
    private float depth;

    public Rectangle3f(IRectangle3i rect) {
        this(rect.getX(), rect.getY(), rect.getZ(), rect.getWidth(), rect.getHeight(), rect.getDepth());
    }

    public Rectangle3f(IRectangle3f rect) {
        this(rect.getX(), rect.getY(), rect.getZ(), rect.getWidth(), rect.getHeight(), rect.getDepth());
    }

    public Rectangle3f(float x, float y, float z, float width, float height, float depth) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public Rectangle3f(double x, double y, double z, double width, double height, double depth) {
        this((float) x, (float) y, (float) z, (float) width, (float) height, (float) depth);
    }

    public Rectangle3f(List<Float> list) {
        this(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4), list.get(5));
    }

    public void union(IRectangle3f rect) {
        float x1 = Math.min(getMinX(), rect.getMinX());
        float y1 = Math.min(getMinY(), rect.getMinY());
        float z1 = Math.min(getMinZ(), rect.getMinZ());
        float x2 = Math.max(getMaxX(), rect.getMaxX());
        float y2 = Math.max(getMaxY(), rect.getMaxY());
        float z2 = Math.max(getMaxZ(), rect.getMaxZ());
        x = x1;
        y = y1;
        z = z1;
        width = x2 - x1;
        height = y2 - y1;
        depth = z2 - z1;
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return this.z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getWidth() {
        return this.width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return this.height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getDepth() {
        return this.depth;
    }

    public void setDepth(float depth) {
        this.depth = depth;
    }

    public float getMinX() {
        return this.x;
    }

    public float getMinY() {
        return this.y;
    }

    public float getMinZ() {
        return this.z;
    }

    public float getMidX() {
        return this.x + this.width / 2;
    }

    public float getMidY() {
        return this.y + this.height / 2;
    }

    public float getMidZ() {
        return this.z + this.depth / 2;
    }

    public float getMaxX() {
        return this.x + this.width;
    }

    public float getMaxY() {
        return this.y + this.height;
    }

    public float getMaxZ() {
        return this.z + this.depth;
    }

    public Vector3f getCenter() {
        return new Vector3f(getMidX(), getMidY(), getMidZ());
    }

    public Vector3f getOrigin() {
        return new Vector3f(x, y, z);
    }

    public Rectangle3f getBounds() {
        return new Rectangle3f(-width / 2, -height / 2, -depth / 2, width, height, depth);
    }

    public Rectangle3f copy() {
        return new Rectangle3f(x, y, z, width, height, depth);
    }

    public Rectangle3f scale(float s) {
        return new Rectangle3f(x * s, y * s, z * s, width * s, height * s, depth * s);
    }

    public Rectangle3f offset(IVector3f point) {
        return offset(point.getX(), point.getY(), point.getZ());
    }

    public Rectangle3f offset(float dx, float dy, float dz) {
        return new Rectangle3f(x + dx, y + dy, z + dz, width, height, depth);
    }

    public Rectangle3f inflate(float value) {
        if (value == 0) {
            return this;
        }
        float v2 = value + value;
        return new Rectangle3f(x - value, y - value, z - value, width + v2, height + v2, depth + v2);
    }


//    public boolean intersects(AABB aABB) {
//        return this.intersects(aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ);
//    }
//
//    public boolean intersects(double d, double e, double f, double g, double h, double i) {
//        return this.minX < g && this.maxX > d && this.minY < h && this.maxY > e && this.minZ < i && this.maxZ > f;
//    }
//
//    public boolean intersects(Vec3 vec3, Vec3 vec32) {
//        return this.intersects(Math.min(vec3.x, vec32.x), Math.min(vec3.y, vec32.y), Math.min(vec3.z, vec32.z), Math.max(vec3.x, vec32.x), Math.max(vec3.y, vec32.y), Math.max(vec3.z, vec32.z));
//    }

    public void mul(OpenQuaternion3f quaternion) {
        mul(new OpenMatrix4f(quaternion));
    }

    public void mul(OpenMatrix4f matrix) {
        var vertexes = Collections.newList(
                new Vector4f(x, y, z, 1.0f),
                new Vector4f(x + width, y, z, 1.0f),
                new Vector4f(x + width, y + height, z, 1.0f),
                new Vector4f(x + width, y + height, z + depth, 1.0f),
                new Vector4f(x + width, y, z + depth, 1.0f),
                new Vector4f(x, y + height, z, 1.0f),
                new Vector4f(x, y + height, z + depth, 1.0f),
                new Vector4f(x, y, z + depth, 1.0f)
        );
        var iterator = vertexes.iterator();
        var point = iterator.next();
        point.transform(matrix);
        float minX = point.getX(), minY = point.getY(), minZ = point.getZ();
        float maxX = point.getX(), maxY = point.getY(), maxZ = point.getZ();
        while (iterator.hasNext()) {
            point = iterator.next();
            point.transform(matrix);
            minX = Math.min(minX, point.getX());
            minY = Math.min(minY, point.getY());
            minZ = Math.min(minZ, point.getZ());
            maxX = Math.max(maxX, point.getX());
            maxY = Math.max(maxY, point.getY());
            maxZ = Math.max(maxZ, point.getZ());
        }
        x = minX;
        y = minY;
        z = minZ;
        width = maxX - minX;
        height = maxY - minY;
        depth = maxZ - minZ;
    }

    public List<Float> toList() {
        return Collections.newList(x, y, z, width, height, depth);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rectangle3f that)) return false;
        return Float.compare(x, that.x) == 0 && Float.compare(y, that.y) == 0 && Float.compare(z, that.z) == 0 && Float.compare(width, that.width) == 0 && Float.compare(height, that.height) == 0 && Float.compare(depth, that.depth) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, width, height, depth);
    }

    @Override
    public String toString() {
        return String.format("(%g %g %g; %g %g %g)", x, y, z, width, height, depth);
    }
}
