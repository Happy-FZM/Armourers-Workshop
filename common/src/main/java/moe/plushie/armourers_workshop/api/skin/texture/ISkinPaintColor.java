package moe.plushie.armourers_workshop.api.skin.texture;

public interface ISkinPaintColor {

    int getRGB();

    int getRawValue();

    ISkinPaintType getPaintType();

    default int getRed() {
        return (getRGB() >> 16) & 0xff;
    }

    default int getGreen() {
        return (getRGB() >> 8) & 0xff;
    }

    default int getBlue() {
        return getRGB() & 0xff;
    }
}
