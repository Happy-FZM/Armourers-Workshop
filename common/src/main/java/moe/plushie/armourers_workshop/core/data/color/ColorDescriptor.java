package moe.plushie.armourers_workshop.core.data.color;

import moe.plushie.armourers_workshop.api.skin.paint.ISkinPaintColor;
import moe.plushie.armourers_workshop.api.skin.paint.ISkinPaintType;
import moe.plushie.armourers_workshop.core.skin.paint.SkinPaintColor;
import moe.plushie.armourers_workshop.core.skin.paint.SkinPaintTypes;

import java.util.HashMap;
import java.util.Set;

public class ColorDescriptor {

    private final HashMap<ISkinPaintType, Channel> channels = new HashMap<>();

    public void add(ISkinPaintColor color) {
        var paintType = color.getPaintType();
        if (shouldRecordChannel(paintType)) {
            var ch = channels.computeIfAbsent(paintType, k -> new Channel());
            ch.red += color.getRed();
            ch.green += color.getGreen();
            ch.blue += color.getBlue();
            ch.total += 1;
            ch.setChanged();
        }
    }

    public void add(ColorDescriptor descriptor) {
        descriptor.channels.forEach((paintType, otherChannel) -> {
            var ch = channels.computeIfAbsent(paintType, k -> new Channel());
            ch.red += otherChannel.red;
            ch.green += otherChannel.green;
            ch.blue += otherChannel.blue;
            ch.total += otherChannel.total;
            ch.setChanged();
        });
    }

    public boolean isEmpty() {
        return channels.isEmpty();
    }

    public ISkinPaintColor getAverageColor(ISkinPaintType paintType) {
        var channel = channels.get(paintType);
        if (channel != null) {
            return channel.getResolvedColor();
        }
        return null;
    }

    public Set<ISkinPaintType> getPaintTypes() {
        return channels.keySet();
    }

    private boolean shouldRecordChannel(ISkinPaintType paintType) {
        if (paintType == SkinPaintTypes.RAINBOW) {
            return true;
        }
        if (paintType == SkinPaintTypes.TEXTURE) {
            return true;
        }
        return paintType.getDyeType() != null;
    }

    private static class Channel {
        int total = 0;
        int red = 0;
        int green = 0;
        int blue = 0;
        ISkinPaintColor resolvedColor;

        void setChanged() {
            resolvedColor = null;
        }

        ISkinPaintColor getResolvedColor() {
            if (resolvedColor != null) {
                return resolvedColor;
            }
            if (total == 0) {
                resolvedColor = SkinPaintColor.CLEAR;
                return resolvedColor;
            }
            int r = red / total;
            int g = green / total;
            int b = blue / total;
            resolvedColor = SkinPaintColor.of(r, g, b, SkinPaintTypes.NORMAL);
            return resolvedColor;
        }
    }
}
