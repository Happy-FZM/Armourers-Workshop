package moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk;

import moe.plushie.armourers_workshop.core.skin.sound.SkinSoundData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChunkSoundData {

    public static List<SkinSoundData> readFromStream(ChunkInputStream stream) throws IOException {
        var results = new ArrayList<SkinSoundData>();
        var count = stream.readVarInt();
        for (int i = 0; i < count; i++) {
//            var section = readSectionFromStream(stream);
//            results.add(section.animation);
        }
        return results;
    }

    public static void writeToStream(List<SkinSoundData> sounds, ChunkOutputStream stream) throws IOException {
        stream.writeVarInt(sounds.size());
        for (var sound : sounds) {
//            var section = new ChunkAnimationData.Section(animation);
//            writeSectionToStream(section, stream);
        }
    }
}

