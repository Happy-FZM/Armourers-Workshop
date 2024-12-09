package moe.plushie.armourers_workshop.core.skin.animation.engine.bind;

import moe.plushie.armourers_workshop.core.skin.animation.molang.bind.selector.ContextSelector;
import moe.plushie.armourers_workshop.core.skin.animation.molang.bind.selector.LevelSelector;
import net.minecraft.world.entity.Entity;

public class ContextSelectorImpl implements ContextSelector {

    private int id = 0;

    private float lifeTime = 0;
    private float animTime = 0;

    private float animationTicks = 0;
    private float partialTicks = 0;

    public void upload(int id, float lifeTime, float animTime, float animationTicks, float partialTicks) {
        this.id = id;
        this.lifeTime = lifeTime;
        this.animTime = animTime;
        this.animationTicks = animationTicks;
        this.partialTicks = partialTicks;
    }

    public int getId() {
        return id;
    }

    @Override
    public float getAnimationTicks() {
        return animationTicks;
    }

    @Override
    public float getPartialTick() {
        return partialTicks;
    }

    @Override
    public float getAnimTime() {
        return animTime;
    }

    @Override
    public float getLifeTime() {
        return lifeTime;
    }

    @Override
    public double getFPS() {
        return -1;
    }


    @Override
    public LevelSelector getLevel() {
        return null;
    }

    public double getCameraDistanceFormEntity(Entity entity) {
        return 0;
    }

    @Override
    public int getEntityCount() {
        return 0;
    }

    @Override
    public boolean isRenderingInInventory() {
        return false;
    }

    @Override
    public boolean isRenderingInFirstPersonMod() {
        return false;
    }

    @Override
    public boolean isFirstPerson() {
        return false;
    }
}
