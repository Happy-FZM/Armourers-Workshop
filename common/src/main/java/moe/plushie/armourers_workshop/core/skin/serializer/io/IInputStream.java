package moe.plushie.armourers_workshop.core.skin.serializer.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import moe.plushie.armourers_workshop.api.core.IRegistryEntry;
import moe.plushie.armourers_workshop.core.math.OpenTransform3f;
import moe.plushie.armourers_workshop.core.math.Rectangle3f;
import moe.plushie.armourers_workshop.core.math.Rectangle3i;
import moe.plushie.armourers_workshop.core.math.Vector3f;
import moe.plushie.armourers_workshop.core.math.Vector3i;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperties;
import moe.plushie.armourers_workshop.core.skin.texture.TextureAnimation;
import moe.plushie.armourers_workshop.core.skin.texture.TextureProperties;
import moe.plushie.armourers_workshop.core.utils.TagSerializer;
import net.minecraft.nbt.CompoundTag;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

public interface IInputStream {

    static IInputStream of(InputStream stream) {
        if (stream instanceof DataInputStream dataInputStream) {
            return of(dataInputStream);
        }
        return of(new DataInputStream(stream));
    }

    static IInputStream of(DataInputStream stream) {
        return () -> stream;
    }

    DataInputStream getInputStream();

    default void skipBytes(int len) throws IOException {
        getInputStream().skipBytes(len);
    }

    default void read(byte[] b) throws IOException {
        getInputStream().readFully(b);
    }

    default void read(byte[] b, int off, int len) throws IOException {
        getInputStream().readFully(b, off, len);
    }

    default void read(FloatBuffer buffer) throws IOException {
        var stream = getInputStream();
        int position = buffer.position();
        int limit = buffer.limit();
        for (int index = position; index < limit; ++index) {
            buffer.put(index, stream.readFloat());
        }
    }

    default ByteBuf readBytes(int limit) throws IOException {
        // we can't directly create a big buffers, it's easy to be hacked.
        var inputStream = getInputStream();
        var buffers = new ArrayList<byte[]>();
        int remaining = limit;
        while (remaining > 0) {
            var bytes = new byte[Math.min(remaining, 16384)]; // 16k
            inputStream.readFully(bytes);
            buffers.add(bytes);
            remaining -= bytes.length;
        }
        return Unpooled.wrappedBuffer(buffers.toArray(new byte[0][]));
    }

    default byte readByte() throws IOException {
        return getInputStream().readByte();
    }

    default boolean readBoolean() throws IOException {
        return getInputStream().readBoolean();
    }

    default short readShort() throws IOException {
        return getInputStream().readShort();
    }

    default int readInt() throws IOException {
        return getInputStream().readInt();
    }

    default long readLong() throws IOException {
        return getInputStream().readLong();
    }

    default float readFloat() throws IOException {
        return getInputStream().readFloat();
    }

    default double readDouble() throws IOException {
        return getInputStream().readDouble();
    }

    default int readFixedInt(int usedBytes) throws IOException {
        if (usedBytes == 4) {
            return readInt();
        }
        int value = 0;
        for (int i = 0; i < usedBytes; i++) {
            int ch = readByte() & 0xff;
            value = (value << 8) | ch;
        }
        return value;
    }

    default float readFixedFloat(int usedBytes) throws IOException {
        return Float.intBitsToFloat(readFixedInt(usedBytes));
    }

    default String readString() throws IOException {
        int size = getInputStream().readUnsignedShort();
        return readString(size);
    }

    default String readString(int len) throws IOException {
        if (len <= 0) {
            return "";
        }
        byte[] bytes = new byte[len];
        getInputStream().readFully(bytes, 0, len);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    default int readVarInt() throws IOException {
        var inputStream = getInputStream();
        byte b;
        int i = 0;
        int j = 0;
        do {
            b = inputStream.readByte();
            i |= (b & 0x7F) << j++ * 7;
            if (j <= 5) {
                continue;
            }
            throw new RuntimeException("VarInt too big");
        } while ((b & 0x80) != 0);
        return i;
    }

    default float[] readFloatArray(int count) throws IOException {
        var results = new float[count];
        for (int i = 0; i < count; i++) {
            results[i] = readFloat();
        }
        return results;
    }

    default <T extends Enum<?>> T readEnum(Class<T> clazz) throws IOException {
        return clazz.getEnumConstants()[readVarInt()];
    }

    default Optional<String> readOptionalString() throws IOException {
        // 0 is null string.
        var len = readVarInt();
        if (len == 0) {
            return Optional.empty();
        }
        // 1 is empty string.
        var value = readString(len - 1);
        return Optional.of(value);
    }

    default Vector3i readVector3i() throws IOException {
        var stream = getInputStream();
        int x = stream.readInt();
        int y = stream.readInt();
        int z = stream.readInt();
        return new Vector3i(x, y, z);
    }

    default Vector3f readVector3f() throws IOException {
        var stream = getInputStream();
        float x = stream.readFloat();
        float y = stream.readFloat();
        float z = stream.readFloat();
        return new Vector3f(x, y, z);
    }

    default Rectangle3i readRectangle3i() throws IOException {
        var stream = getInputStream();
        int x = stream.readInt();
        int y = stream.readInt();
        int z = stream.readInt();
        int width = stream.readInt();
        int height = stream.readInt();
        int depth = stream.readInt();
        return new Rectangle3i(x, y, z, width, height, depth);
    }

    default Rectangle3f readRectangle3f() throws IOException {
        var stream = getInputStream();
        float x = stream.readFloat();
        float y = stream.readFloat();
        float z = stream.readFloat();
        float width = stream.readFloat();
        float height = stream.readFloat();
        float depth = stream.readFloat();
        return new Rectangle3f(x, y, z, width, height, depth);
    }

    default OpenTransform3f readTransformf() throws IOException {
        var transform = new OpenTransform3f();
        transform.readFromStream(this);
        if (!transform.equals(OpenTransform3f.IDENTITY)) {
            return transform;
        }
        return OpenTransform3f.IDENTITY;
    }

    default <T extends IRegistryEntry> T readType(Function<String, T> transform) throws IOException {
        var name = readString();
        return transform.apply(name);
    }

    default SkinProperties readSkinProperties() throws IOException {
        var properties = new SkinProperties();
        properties.readFromStream(this);
        return properties;
    }

    default TextureAnimation readTextureAnimation() throws IOException {
        var animation = new TextureAnimation();
        animation.readFromStream(this);
        return animation;
    }

    default TextureProperties readTextureProperties() throws IOException {
        var properties = new TextureProperties();
        properties.readFromStream(this);
        return properties;
    }

    default CompoundTag readCompoundTag() throws IOException {
        return TagSerializer.readFromStream(getInputStream());
    }
}
