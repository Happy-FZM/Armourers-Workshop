package moe.plushie.armourers_workshop.core.skin.serializer.v20.coder.v1;

import moe.plushie.armourers_workshop.api.painting.IPaintColor;
import moe.plushie.armourers_workshop.api.skin.ISkinCube;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkOutputStream;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkPaletteData;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.coder.ChunkCubeEncoder;
import net.minecraft.core.Direction;

import java.io.IOException;
import java.util.LinkedHashMap;

public class ChunkCubeEncoderV1 extends ChunkCubeEncoder {

    private ISkinCube cube;
    private final LinkedHashMap<IPaintColor, Integer> values = new LinkedHashMap<>();

    @Override
    public int begin(ISkinCube cube) {
        // merge all values
        for (var dir : Direction.values()) {
            var value = cube.getPaintColor(dir);
            int face = values.getOrDefault(value, 0);
            face |= 1 << dir.get3DDataValue();
            values.put(value, face);
        }
        this.cube = cube;
        return values.size();
    }

    @Override
    public void end(ChunkPaletteData palette, ChunkOutputStream stream) throws IOException {
        // position(3B)
        var pos = cube.getPosition();
        stream.writeByte(pos.getX());
        stream.writeByte(pos.getY());
        stream.writeByte(pos.getZ());

        // face: <color ref>
        for (var entry : values.entrySet()) {
            stream.writeByte(entry.getValue());
            stream.writeVariable(palette.writeColor(entry.getKey()));
        }

        values.clear();
        cube = null;
    }
}
