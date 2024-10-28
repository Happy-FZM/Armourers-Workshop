package moe.plushie.armourers_workshop.core.skin.serializer.document;

import moe.plushie.armourers_workshop.api.core.IDataCodec;
import moe.plushie.armourers_workshop.api.core.IDataSerializable;
import moe.plushie.armourers_workshop.api.core.IDataSerializer;
import moe.plushie.armourers_workshop.api.core.IDataSerializerKey;
import moe.plushie.armourers_workshop.core.skin.SkinDescriptor;

public class SkinDocumentAnimation implements IDataSerializable.Immutable {

    public static IDataCodec<SkinDocumentAnimation> CODEC = IDataCodec.COMPOUND_TAG.serializer(SkinDocumentAnimation::new);

    private final String name;
    private final SkinDescriptor descriptor;

    public SkinDocumentAnimation(String name, SkinDescriptor descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    public SkinDocumentAnimation(IDataSerializer serializer) {
        this.name = serializer.read(CodingKeys.NAME);
        this.descriptor = serializer.read(CodingKeys.SKIN);
    }

    @Override
    public void serialize(IDataSerializer serializer) {
        serializer.write(CodingKeys.NAME, name);
        serializer.write(CodingKeys.SKIN, descriptor); // extra link
    }

    public String getName() {
        return name;
    }

    public SkinDescriptor getDescriptor() {
        return descriptor;
    }

    private static class CodingKeys {

        public static final IDataSerializerKey<String> NAME = IDataSerializerKey.create("Name", IDataCodec.STRING, "");
        public static final IDataSerializerKey<SkinDescriptor> SKIN = IDataSerializerKey.create("Skin", SkinDescriptor.CODEC, SkinDescriptor.EMPTY);
    }
}
