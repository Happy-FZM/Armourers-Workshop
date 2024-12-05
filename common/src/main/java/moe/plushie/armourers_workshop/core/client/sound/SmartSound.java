package moe.plushie.armourers_workshop.core.client.sound;

import io.netty.buffer.ByteBuf;
import moe.plushie.armourers_workshop.api.core.IResourceLocation;
import moe.plushie.armourers_workshop.api.skin.sound.ISoundProvider;
import moe.plushie.armourers_workshop.core.client.other.SmartResourceManager;
import moe.plushie.armourers_workshop.core.utils.ReferenceCounted;
import moe.plushie.armourers_workshop.core.utils.OpenRandomSource;
import moe.plushie.armourers_workshop.init.ModConstants;
import moe.plushie.armourers_workshop.utils.DataContainer;
import moe.plushie.armourers_workshop.utils.RenderSystem;
import net.minecraft.sounds.SoundEvent;

import java.util.LinkedHashMap;
import java.util.Map;

public class SmartSound extends ReferenceCounted {

    private final String name;
    private final IResourceLocation location;
    private final Map<IResourceLocation, ByteBuf> soundBuffers;

    private SoundEvent soundEvent;

    public SmartSound(ISoundProvider provider) {
        this.name = provider.getName();
        this.location = ModConstants.key("sounds/dynamic/" + OpenRandomSource.nextInt(SmartSound.class) + ".ogg");
        this.soundBuffers = resolveSoundBuffers(location, provider);
    }

    public static SmartSound of(SoundEvent soundEvent) {
        return DataContainer.get(soundEvent, null);
    }

    @Override
    protected void init() {
        RenderSystem.safeCall(() -> {
            soundBuffers.forEach(SmartResourceManager.getInstance()::register);
            SmartSoundManager.getInstance().uploadSound(this);
        });
    }

    @Override
    protected void dispose() {
        RenderSystem.safeCall(() -> {
            SmartSoundManager.getInstance().releaseSound(this);
            soundBuffers.keySet().forEach(SmartResourceManager.getInstance()::unregister);
        });
    }

    public String getName() {
        return name;
    }

    public IResourceLocation getLocation() {
        return location;
    }

    public SoundEvent getSoundEvent() {
        if (soundEvent == null) {
            soundEvent = SoundEvent.createVariableRangeEvent(location.toLocation());
            DataContainer.set(soundEvent, this);
        }
        return soundEvent;
    }

    protected void unbind() {
        DataContainer.set(soundEvent, null);
        // when unbind the object, we must ensure that all resources release.
        while (refCnt() > 0) {
            release();
        }
    }

    private Map<IResourceLocation, ByteBuf> resolveSoundBuffers(IResourceLocation location, ISoundProvider provider) {
        var results = new LinkedHashMap<IResourceLocation, ByteBuf>();
        results.put(location, provider.getBuffer());
        return results;
    }
}
