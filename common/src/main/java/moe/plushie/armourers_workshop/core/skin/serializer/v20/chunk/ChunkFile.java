package moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk;

import io.netty.buffer.ByteBuf;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperties;
import org.jetbrains.annotations.Nullable;

public class ChunkFile {

    private final String name;
    private final SkinProperties properties;
    private final ByteBuf bytes;

    private final int type;

    public ChunkFile(int type, @Nullable String name, SkinProperties properties, ByteBuf bytes) {
        this.type = type;
        this.name = name;
        this.properties = properties;
        this.bytes = bytes;
    }

    public static ChunkFile image(@Nullable String name, ByteBuf bytes) {
        return image(name, SkinProperties.EMPTY, bytes);
    }

    public static ChunkFile image(@Nullable String name, SkinProperties properties, ByteBuf bytes) {
        return new ChunkFile(0, name, properties, bytes);
    }

    public static ChunkFile audio(@Nullable String name, ByteBuf bytes) {
        return audio(name, SkinProperties.EMPTY, bytes);
    }

    public static ChunkFile audio(@Nullable String name, SkinProperties properties, ByteBuf bytes) {
        return new ChunkFile(1, name, properties, bytes);
    }

    public static ChunkFile particle(@Nullable String name, ByteBuf bytes) {
        return particle(name, SkinProperties.EMPTY, bytes);
    }

    public static ChunkFile particle(@Nullable String name, SkinProperties properties, ByteBuf bytes) {
        return new ChunkFile(2, name, properties, bytes);
    }

    @Nullable
    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public SkinProperties getProperties() {
        return properties;
    }

    public ByteBuf getBytes() {
        return bytes;
    }
}
