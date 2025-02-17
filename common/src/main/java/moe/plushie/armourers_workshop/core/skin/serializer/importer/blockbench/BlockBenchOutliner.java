package moe.plushie.armourers_workshop.core.skin.serializer.importer.blockbench;

import moe.plushie.armourers_workshop.core.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class BlockBenchOutliner extends BlockBenchObject {

    private final boolean allowExport;

    private final Vector3f origin;
    private final Vector3f rotation;

    private final List<Object> children;

    public BlockBenchOutliner(String uuid, String name, Vector3f origin, Vector3f rotation, boolean allowExport, List<Object> children) {
        super(uuid, name);
        this.origin = origin;
        this.rotation = rotation;
        this.allowExport = allowExport;
        this.children = children;
    }

    public boolean allowExport() {
        return allowExport;
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public List<Object> getChildren() {
        return children;
    }

    protected static class Builder extends BlockBenchObject.Builder {

        private boolean allowExport = true;

        private Vector3f origin = Vector3f.ZERO;
        private Vector3f rotation = Vector3f.ZERO;

        private final List<Object> children = new ArrayList<>();

        public void origin(Vector3f origin) {
            this.origin = origin;
        }

        public void rotation(Vector3f rotation) {
            this.rotation = rotation;
        }

        public void export(boolean allowExport) {
            this.allowExport = allowExport;
        }

        public void addChild(Object obj) {
            this.children.add(obj);
        }

        public BlockBenchOutliner build() {
            return new BlockBenchOutliner(uuid, name, origin, rotation, allowExport, children);
        }
    }
}
