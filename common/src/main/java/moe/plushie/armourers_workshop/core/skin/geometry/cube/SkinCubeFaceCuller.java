package moe.plushie.armourers_workshop.core.skin.geometry.cube;

import moe.plushie.armourers_workshop.api.skin.part.ISkinPartType;
import moe.plushie.armourers_workshop.core.math.Rectangle3i;
import moe.plushie.armourers_workshop.core.math.Vector3i;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryFace;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometrySet;
import moe.plushie.armourers_workshop.core.skin.geometry.SkinGeometryTypes;
import moe.plushie.armourers_workshop.core.skin.part.SkinPartTypes;
import moe.plushie.armourers_workshop.core.utils.Collections;
import moe.plushie.armourers_workshop.core.utils.OpenDirection;
import moe.plushie.armourers_workshop.init.ModConfig;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class SkinCubeFaceCuller {

    private static final int DIRECTION_SIZE = OpenDirection.values().length;

    // joints array:
    private static final Map<ISkinPartType, Partition> PARTITIONS2 = Collections.immutableMap(builder -> {
        builder.put(SkinPartTypes.BIPPED_HAT, new Simple(SkinPartTypes.BIPPED_HAT));
        builder.put(SkinPartTypes.BIPPED_HEAD, new Simple(SkinPartTypes.BIPPED_HEAD));
        builder.put(SkinPartTypes.BIPPED_CHEST, new Limb(SkinPartTypes.BIPPED_CHEST, SkinPartTypes.BIPPED_TORSO, 6));
        builder.put(SkinPartTypes.BIPPED_LEFT_ARM, new Limb(SkinPartTypes.BIPPED_LEFT_ARM, SkinPartTypes.BIPPED_LEFT_HAND, 4));
        builder.put(SkinPartTypes.BIPPED_RIGHT_ARM, new Limb(SkinPartTypes.BIPPED_RIGHT_ARM, SkinPartTypes.BIPPED_RIGHT_HAND, 4));
        builder.put(SkinPartTypes.BIPPED_SKIRT, new Simple(SkinPartTypes.BIPPED_SKIRT));
        builder.put(SkinPartTypes.BIPPED_LEFT_THIGH, new Limb(SkinPartTypes.BIPPED_LEFT_THIGH, SkinPartTypes.BIPPED_LEFT_LEG, 6));
        builder.put(SkinPartTypes.BIPPED_RIGHT_THIGH, new Limb(SkinPartTypes.BIPPED_RIGHT_THIGH, SkinPartTypes.BIPPED_RIGHT_LEG, 6));
        builder.put(SkinPartTypes.BIPPED_LEFT_FOOT, new Simple(SkinPartTypes.BIPPED_LEFT_FOOT));
        builder.put(SkinPartTypes.BIPPED_RIGHT_FOOT, new Simple(SkinPartTypes.BIPPED_RIGHT_FOOT));
        builder.put(SkinPartTypes.BIPPED_LEFT_WING, new Simple(SkinPartTypes.BIPPED_LEFT_WING));
        builder.put(SkinPartTypes.BIPPED_RIGHT_WING, new Simple(SkinPartTypes.BIPPED_RIGHT_WING));
    });

    interface Partition {
        Collection<SearchResult> subdivide(Rectangle3i rect);
    }

    private static Partition getPartition(ISkinPartType partType) {
        if (ModConfig.Client.enablePartSubdivide) {
            var partition = PARTITIONS2.get(partType);
            if (partition != null) {
                return partition;
            }
        }
        return new Simple(partType);
    }

    public static Collection<SearchResult> cullFaces2(SkinGeometrySet<?> geometries, Rectangle3i bounds, ISkinPartType partType) {
        // The texture cube does not support static cull,
        // the slices are designed to contain multiple cube types,
        // but the skin culler can't support it now.
        var supportedTypes = geometries.getSupportedTypes();
        if (supportedTypes != null && (supportedTypes.contains(SkinGeometryTypes.CUBE) || supportedTypes.contains(SkinGeometryTypes.MESH))) {
            return allFaces(geometries, bounds, partType);
        }
        var partition = getPartition(partType);
        var indexedMap = new IndexedMap(geometries, bounds);
        var results = partition.subdivide(bounds);
        for (var result : results) {
            result.cull(geometries, indexedMap);
        }
        for (int i = 0; i < geometries.size(); ++i) {
            SkinCube geometry = null;
            for (var dir : OpenDirection.values()) {
                for (var result : results) {
                    if (result.flags.get(i * DIRECTION_SIZE + dir.get3DDataValue())) {
                        if (geometry == null) {
                            geometry = (SkinCube) geometries.get(i);
                        }
                        var face = geometry.getFace(dir);
                        if (face != null) {
                            result.add(face);
                        }
                    }
                }
            }
        }
        return results;
    }

    public static Collection<SearchResult> allFaces(SkinGeometrySet<?> geometries, Rectangle3i bounds, ISkinPartType partType) {
        var result = new SearchResult(partType, bounds, Vector3i.ZERO);
        for (int i = 0; i < geometries.size(); ++i) {
            var geometry = geometries.get(i);
            for (var face : geometry.getFaces()) {
                result.add(face);
            }
        }
        return Collections.singleton(result);
    }

    public static ArrayList<SkinGeometryFace> cullFaces(SkinGeometrySet<?> geometries, Rectangle3i bounds) {
        var indexedMap = new IndexedMap(geometries, bounds);
        var rect = new Rectangle3i(0, 0, 0, bounds.getWidth(), bounds.getHeight(), bounds.getDepth());
        var flags = cullFaceFlags(geometries, indexedMap, rect);
        var faces = new ArrayList<SkinGeometryFace>();
        for (int i = 0; i < geometries.size(); ++i) {
            SkinCube geometry = null;
            for (var dir : OpenDirection.values()) {
                if (flags.get(i * DIRECTION_SIZE + dir.get3DDataValue())) {
                    if (geometry == null) {
                        geometry = (SkinCube) geometries.get(i);
                    }
                    var face = geometry.getFace(dir);
                    if (face != null) {
                        faces.add(face);
                    }
                }
            }
        }
        return faces;
    }

    private static BitSet cullFaceFlags(SkinGeometrySet<?> geometries, IndexedMap map, Rectangle3i rect) {
        var flags = new BitSet(geometries.size() * DIRECTION_SIZE);
        var searchArea = new Rectangle3i(rect.getX() - 1, rect.getY() - 1, rect.getZ() - 1, rect.getWidth() + 2, rect.getHeight() + 2, rect.getDepth() + 2);
        var closedSet = new HashSet<Vector3i>();
        var openList = new ArrayDeque<Vector3i>();
        var start = searchArea.getOrigin();
        openList.add(start);
        closedSet.add(start);
        map.limit(rect);
        while (!openList.isEmpty()) {
            var pendingList = new ArrayList<Vector3i>();
            var pos = openList.poll();
            for (var advance : OpenDirection.values()) {
                var pos1 = pos.relative(advance, 1);
                int targetIndex = map.get(pos1);
                if (targetIndex == -1) {
                    pendingList.add(pos1);
                    continue;
                }
                var isBlank = false;
                var targetGeometryType = geometries.get(targetIndex).getType();
                if (SkinGeometryTypes.isGlass(targetGeometryType)) {
                    pendingList.add(pos1);
                    // when source cube and target cube is linked glass, ignore.
                    int sourceIndex = map.get(pos);
                    if (sourceIndex != -1) {
                        isBlank = SkinGeometryTypes.isGlass(geometries.get(sourceIndex).getType());
                    }
                }
                // first, when not any rotation of the cube, it's always facing north.
                // then advance direction always opposite to cube facing direction,
                // so theory we should always use `advance.getOpposite()` get cube facing direction.
                // but actually we reset the cube origin in the indexes, which causes the relationship
                // between the forward direction and the facing is changed.
                var facing = advance;
                if (advance.getAxis() == OpenDirection.Axis.Z) {
                    facing = advance.getOpposite();
                }
                flags.set(targetIndex * DIRECTION_SIZE + facing.get3DDataValue(), !isBlank);
            }
            for (var pos1 : pendingList) {
                if (searchArea.contains(pos1) && !closedSet.contains(pos1)) {
                    closedSet.add(pos1);
                    openList.add(pos1);
                }
            }
        }
        return flags;
    }

    static class Simple implements Partition {

        final ISkinPartType partType;

        public Simple(ISkinPartType partType) {
            this.partType = partType;
        }

        @Override
        public Collection<SearchResult> subdivide(Rectangle3i rect) {
            var box = new Rectangle3i(0, 0, 0, rect.getWidth(), rect.getHeight(), rect.getDepth());
            return Collections.singleton(new SearchResult(partType, box, Vector3i.ZERO));
        }
    }

    static class Limb extends Simple {

        final ISkinPartType upperPartType;
        final ISkinPartType lowerPartType;

        final int yClip;

        public Limb(ISkinPartType upperPartType, ISkinPartType lowerPartType, int yClip) {
            super(upperPartType);
            this.upperPartType = upperPartType;
            this.lowerPartType = lowerPartType;
            this.yClip = yClip;
        }

        @Override
        public Collection<SearchResult> subdivide(Rectangle3i rect) {
            int upper = yClip - rect.getMinY();
            int lower = rect.getMaxY() - yClip;
            if (lower > 0 && upper > 0) {
                var upperBox = new Rectangle3i(0, 0, 0, rect.getWidth(), upper, rect.getDepth());
                var lowerBox = new Rectangle3i(0, upper, 0, rect.getWidth(), lower, rect.getDepth());
                // ..
                var upperResult = new SearchResult(upperPartType, upperBox, Vector3i.ZERO);
                var lowerResult = new SearchResult(lowerPartType, lowerBox, new Vector3i(0, -yClip, 0));
                return Collections.newList(upperResult, lowerResult);
            }
            return super.subdivide(rect);
        }
    }

    public static class SearchResult {

        protected final ISkinPartType partType;
        protected final Rectangle3i bounds;
        protected final Vector3i origin;

        protected BitSet flags;
        protected ArrayList<SkinGeometryFace> faces;

        public SearchResult(ISkinPartType partType, Rectangle3i bounds, Vector3i origin) {
            this.faces = new ArrayList<>();
            this.partType = partType;
            this.bounds = bounds;
            this.origin = origin;
        }

        public void add(SkinGeometryFace face) {
            this.faces.add(face);
        }

        public void cull(SkinGeometrySet<?> geometries, IndexedMap map) {
            this.flags = cullFaceFlags(geometries, map, bounds);
        }

        public ISkinPartType getPartType() {
            return partType;
        }

        public ArrayList<SkinGeometryFace> getFaces() {
            return faces;
        }

        public Vector3i getOrigin() {
            return origin;
        }

        public Rectangle3i getBounds() {
            return bounds;
        }
    }

    public static class IndexedMap {


        public final int x;
        public final int y;
        public final int z;
        public final int width;
        public final int height;
        public final int depth;

        private final int[][][] indexes;

        private int minX;
        private int minY;
        private int minZ;
        private int maxX;
        private int maxY;
        private int maxZ;

        public IndexedMap(SkinGeometrySet<?> geometries, Rectangle3i bounds) {
            this.x = bounds.getX();
            this.y = bounds.getY();
            this.z = bounds.getZ();
            this.width = bounds.getWidth();
            this.height = bounds.getHeight();
            this.depth = bounds.getDepth();
            this.indexes = new int[this.depth][this.height][this.width];
            int size = geometries.size();
            for (int i = 0; i < size; i++) {
                var geometry = (SkinCube) geometries.get(i);
                var blockPos = geometry.getBlockPos();
                int x = blockPos.getX() - this.x;
                int y = blockPos.getY() - this.y;
                int z = blockPos.getZ() - this.z;
                this.indexes[z][y][x] = i + 1;
            }
            this.limit(new Rectangle3i(0, 0, 0, this.width, this.height, this.depth));
        }

        public void limit(Rectangle3i limit) {
            this.minX = Math.max(limit.getMinX(), 0);
            this.minY = Math.max(limit.getMinY(), 0);
            this.minZ = Math.max(limit.getMinZ(), 0);
            this.maxX = Math.min(limit.getMaxX(), width);
            this.maxY = Math.min(limit.getMaxY(), height);
            this.maxZ = Math.min(limit.getMaxZ(), depth);
        }

        public int get(Vector3i pos) {
            return get(pos.getX(), pos.getY(), pos.getZ());
        }

        public int get(int x, int y, int z) {
            if (x < minX || x >= maxX) {
                return -1;
            }
            if (y < minY || y >= maxY) {
                return -1;
            }
            if (z < minZ || z >= maxZ) {
                return -1;
            }
            return indexes[z][y][x] - 1;
        }
    }
}
