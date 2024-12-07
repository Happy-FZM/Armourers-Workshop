package moe.plushie.armourers_workshop.core.client.animation;

import moe.plushie.armourers_workshop.api.core.IDataCodec;
import moe.plushie.armourers_workshop.api.core.IDataSerializable;
import moe.plushie.armourers_workshop.api.core.IDataSerializer;
import moe.plushie.armourers_workshop.api.core.IDataSerializerKey;
import moe.plushie.armourers_workshop.core.client.animation.bind.ClientExecutionContextImpl;
import moe.plushie.armourers_workshop.core.client.bake.BakedSkin;
import moe.plushie.armourers_workshop.core.client.other.BlockEntityRenderData;
import moe.plushie.armourers_workshop.core.client.other.EntityRenderData;
import moe.plushie.armourers_workshop.core.data.EntityAction;
import moe.plushie.armourers_workshop.core.data.EntityActionSet;
import moe.plushie.armourers_workshop.core.data.EntityActionTarget;
import moe.plushie.armourers_workshop.core.data.EntityActions;
import moe.plushie.armourers_workshop.core.math.OpenMath;
import moe.plushie.armourers_workshop.core.skin.SkinDescriptor;
import moe.plushie.armourers_workshop.core.skin.animation.SkinAnimationLoop;
import moe.plushie.armourers_workshop.core.utils.Collections;
import moe.plushie.armourers_workshop.core.utils.Objects;
import moe.plushie.armourers_workshop.core.utils.TagSerializer;
import moe.plushie.armourers_workshop.init.ModConfig;
import moe.plushie.armourers_workshop.init.ModLog;
import moe.plushie.armourers_workshop.utils.TickUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class AnimationManager {

    public static final AnimationManager NONE = new AnimationManager(null);

    private final HashMap<BakedSkin, Entry> allEntries = new HashMap<>();
    private final HashMap<BakedSkin, Entry> activeEntries = new HashMap<>();

    private final ArrayList<Entry> triggerableEntries = new ArrayList<>();

    private final ArrayList<Pair<AnimationPlayState, Runnable>> removeOnCompletion = new ArrayList<>();

    private final HashMap<String, PlayAction> lastActions = new HashMap<>();

    private EntityActionSet lastActionSet;
    private float lastAnimationTicks = 0;

    private final ClientExecutionContextImpl executionContext;

    public AnimationManager(Object entity) {
        this.executionContext = new ClientExecutionContextImpl(entity);
    }

    public static AnimationManager of(Entity entity) {
        var renderData = EntityRenderData.of(entity);
        if (renderData != null) {
            return renderData.getAnimationManager();
        }
        return null;
    }

    public static AnimationManager of(BlockEntity blockEntity) {
        var renderData = BlockEntityRenderData.of(blockEntity);
        if (renderData != null) {
            return renderData.getAnimationManager();
        }
        return null;
    }

    public void load(Map<SkinDescriptor, BakedSkin> skins) {
        var expiredEntries = new HashMap<>(allEntries);
        skins.forEach((key, skin) -> {
            expiredEntries.remove(skin);
            allEntries.computeIfAbsent(skin, Entry::new);
        });
        expiredEntries.forEach((key, entry) -> {
            allEntries.remove(key);
            entry.stop();
        });
        rebuildTriggerableEntities();
        setChanged();
    }

    public void active(Map<SkinDescriptor, BakedSkin> skins) {
        var expiredEntries = new HashMap<>(activeEntries);
        skins.forEach((key, skin) -> {
            var entry = expiredEntries.remove(skin);
            if (entry != null) {
                return; // no change, ignore.
            }
            entry = allEntries.get(skin);
            if (entry == null) {
                return; // no found, ignore.
            }
            activeEntries.put(skin, entry);
            resumeState(entry);
        });
        expiredEntries.forEach((key, entry) -> {
            entry.getAnimationControllers().forEach(it -> lastActions.remove(it.getName()));
            activeEntries.remove(key);
            entry.stop();
        });
    }

    public void tick(Object source, float animationTicks) {
        // clear invalid animation.
        if (!removeOnCompletion.isEmpty()) {
            var iterator = removeOnCompletion.iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                var state = entry.getKey();
                state.tick(animationTicks);
                if (state.isCompleted()) {
                    iterator.remove();
                    entry.getValue().run();
                }
            }
        }
        // play triggerable animation by the state.
        if (!triggerableEntries.isEmpty() && source instanceof Entity entity) {
            var actionSet = entity.getActionSet();
            if (actionSet != null && !actionSet.equals(lastActionSet)) {
                debugLog("{} action did change: {}", entity, actionSet);
                triggerableEntries.forEach(entry -> entry.autoplay(actionSet, animationTicks));
                lastActionSet = actionSet.copy();
            }
        }
        lastAnimationTicks = animationTicks;
    }

    public void play(String name, float atTime, CompoundTag tag) {
        lastActions.put(name, new PlayAction(name, atTime, tag));
        for (var entry : activeEntries.values()) {
            for (var animationController : entry.getAnimationControllers()) {
                if (name.equals(animationController.getName())) {
                    entry.play(animationController, atTime, tag);
                }
            }
        }
    }

    public void stop(String name) {
        if (name.isEmpty()) {
            lastActions.clear();
        } else {
            lastActions.remove(name);
        }
        for (var entry : activeEntries.values()) {
            for (var animationController : entry.getAnimationControllers()) {
                if (name.isEmpty() || name.equals(animationController.getName())) {
                    entry.stop(animationController);
                }
            }
        }
    }

    public void map(String from, String to) {
        allEntries.forEach((skin, entry) -> entry.map(from, to));
        rebuildTriggerableEntities();
        setChanged();
    }

    @Nullable
    public AnimationContext getAnimationContext(BakedSkin skin) {
        return allEntries.get(skin);
    }

    private void setChanged() {
        lastActionSet = null;
    }

    private void resumeState(Entry entry) {
        entry.autoplay();
        lastActions.forEach((name, action) -> {
            for (var animationController : entry.getAnimationControllers()) {
                if (name.equals(animationController.getName())) {
                    action.resume(entry, animationController);
                }
            }
        });
    }

    private void rebuildTriggerableEntities() {
        triggerableEntries.clear();
        triggerableEntries.addAll(Collections.filter(allEntries.values(), Entry::hasTriggerableAnimation));
    }

    protected void debugLog(String message, Object... arguments) {
        if (ModConfig.Client.enableAnimationDebug) {
            ModLog.debug(message, arguments);
        }
    }

    protected class Entry extends AnimationContext {

        protected final List<TriggerableController> triggerableControllers = new ArrayList<>();

        protected final HashMap<String, String> actionToName = new HashMap<>();

        protected TriggerableController playing;
        protected boolean isLocking;
        protected boolean isFirstTransitionAnimation = true;

        public Entry(BakedSkin skin) {
            super(AnimationManager.this.executionContext, skin.getAnimationControllers());
            this.rebuildTriggerableControllers();
        }

        public void map(String action, String newName) {
            if (action.equals(newName) || newName.isEmpty()) {
                actionToName.remove(action);
            } else {
                actionToName.put(action, newName);
            }
            rebuildTriggerableControllers();
        }

        public void autoplay() {
            animationControllers.stream().filter(AnimationController::isParallel).forEach(it -> {
                // autoplay the parallel animation.
                startPlay(it, TickUtils.animationTicks(), 1, 0);
            });
        }

        public void autoplay(EntityActionSet actionSet, float time) {
            // If it's already locked, we won't switch.
            if (isLocking && playing != null) {
                return;
            }
            var newValue = findTriggerableController(actionSet);
            if (newValue != null && newValue != playing) {
                play(newValue, playing, time, 1, newValue.getPlayCount(), false);
            }
        }

        public void play(AnimationController animationController, float time, CompoundTag tag) {
            // play parallel animation (simple).
            var properties = new Properties(new TagSerializer(tag));
            if (animationController.isParallel()) {
                startPlay(animationController, time, properties.speed, properties.playCount);
                return;
            }
            // play triggerable animation (lock).
            var newValue = findTriggerableController(animationController);
            if (newValue != null && newValue != playing) {
                play(newValue, playing, time, properties.speed, properties.playCount, properties.needsLock);
            }
        }

        public void stop(AnimationController animationController) {
            var playState = getPlayState(animationController);
            if (playState == null) {
                return; // ignore non-playing animation.
            }
            // stop parallel animation (simple).
            if (animationController.isParallel()) {
                stopPlayIfNeeded(animationController);
                return;
            }
            // ignore, when not found.
            var oldValue = playing;
            if (oldValue == null || oldValue.animationController != animationController) {
                return;
            }
            playing = null;
            isLocking = false;
            stopPlayIfNeeded(animationController);
            setChanged();
        }

        public void stop() {
            animationControllers.forEach(this::stop);
        }


        public boolean hasTriggerableAnimation() {
            return !triggerableControllers.isEmpty();
        }

        private void play(TriggerableController newValue, @Nullable TriggerableController oldValue, float time, float speed, int playCount, boolean needLock) {
            var toAnimationController = newValue.animationController;
            var fromAnimationController = Objects.flatMap(oldValue, it -> it.animationController);

            // clear prev play state.
            if (fromAnimationController != null) {
                stopPlayIfNeeded(fromAnimationController);
            }

            // set next play state.
            startPlay(toAnimationController, time, speed, playCount);

            isLocking = needLock;
            playing = newValue;

            // TODO: @SAGESSE Add transition duration support.
            var duration = newValue.getTransitionDuration();
            applyTransiting(fromAnimationController, toAnimationController, time, speed, duration);
        }

        private void startPlay(AnimationController animationController, float time, float speed, int playCount) {
            stopPlayIfNeeded(animationController);
            var newPlayState = AnimationPlayState.create(time, playCount, speed, animationController);
            addPlayState(animationController, newPlayState);
            debugLog("start play {}", animationController);
            if (newPlayState.getLoopCount() > 0) {
                removeOnCompletion.add(Pair.of(newPlayState, () -> stop(animationController)));
            }
        }

        private void stopPlayIfNeeded(AnimationController animationController) {
            var oldPlayState = removePlayState(animationController);
            if (oldPlayState != null) {
                debugLog("stop play {}", animationController);
                oldPlayState.reset();
                removeOnCompletion.removeIf(it -> it.getLeft() == oldPlayState);
            }
        }

        private void applyTransiting(AnimationController fromAnimationController, AnimationController toAnimationController, float time, float speed, float duration) {
            // we need to ignore the first transition animation, because have some very strange effects.
            if (isFirstTransitionAnimation) {
                isFirstTransitionAnimation = false;
                return;
            }
            // delay the animation start time.
            var playState = getPlayState(toAnimationController);
            if (playState != null) {
                playState.setTime(playState.getTime() + duration);
            }
            addAnimation(fromAnimationController, toAnimationController, time, speed, duration);
        }

        private String resolveMappingName(String name) {
            // map idle sit/idle
            for (var entry : actionToName.entrySet()) {
                if (entry.getValue().equals(name)) {
                    return entry.getKey(); // name to action.
                }
                if (entry.getKey().equals(name)) {
                    return "redirected:" + name;  // name is action, but it was redirected.
                }
            }
            return name;
        }

        private TriggerableController findTriggerableController(EntityActionSet tracker) {
            for (var entry : triggerableControllers) {
                if (entry.isIdle || entry.test(tracker)) {
                    return entry;
                }
            }
            return null;
        }

        private TriggerableController findTriggerableController(AnimationController animationController) {
            for (var entry : triggerableControllers) {
                if (entry.animationController == animationController) {
                    return entry;
                }
            }
            return null;
        }

        private void rebuildTriggerableControllers() {
            var newValues = new ArrayList<TriggerableController>();
            for (var animationController : animationControllers) {
                if (!animationController.isParallel()) {
                    var name = resolveMappingName(animationController.getName());
                    var controller = new TriggerableController(name, animationController);
                    newValues.add(controller);
                }
            }
            newValues.sort(Comparator.comparingDouble(TriggerableController::getPriority).reversed());
            triggerableControllers.clear();
            triggerableControllers.addAll(newValues);
            if (playing == null) {
                return;
            }
            playing = findTriggerableController(playing.animationController);
        }
    }

    protected static class Properties implements IDataSerializable.Immutable {

        public static final IDataSerializerKey<Float> SPEED = IDataSerializerKey.create("speed", IDataCodec.FLOAT, 1.0f);
        public static final IDataSerializerKey<Integer> REPEAT = IDataSerializerKey.create("repeat", IDataCodec.INT, 0);
        public static final IDataSerializerKey<Boolean> LOCK = IDataSerializerKey.create("lock", IDataCodec.BOOL, true);

        public static final IDataCodec<Properties> CODEC = IDataCodec.COMPOUND_TAG.serializer(Properties::new);

        private final float speed;
        private final int playCount;
        private final boolean needsLock;

        public Properties(IDataSerializer serializer) {
            this.speed = OpenMath.clamp(serializer.read(SPEED), 0.0001f, 1000.0f);
            this.playCount = OpenMath.clamp(serializer.read(REPEAT), -1, 1000);
            this.needsLock = serializer.read(LOCK);
        }

        @Override
        public void serialize(IDataSerializer serializer) {
            serializer.write(SPEED, speed);
            serializer.write(REPEAT, playCount);
            serializer.write(LOCK, needsLock);
        }
    }

    protected static class TriggerableController {

        private final String name;
        private final EntityActionTarget target;
        private final AnimationController animationController;
        private final boolean isIdle;

        public TriggerableController(String name, AnimationController animationController) {
            this.name = name;
            this.target = EntityActions.by(name);
            this.animationController = animationController;
            this.isIdle = target.getActions().contains(EntityAction.IDLE);
        }

        public boolean test(EntityActionSet actionSet) {
            int hit = 0;
            for (var action : target.getActions()) {
                if (!actionSet.contains(action)) {
                    return false;
                }
                hit += 1;
            }
            return hit != 0;
        }

        public String getName() {
            return name;
        }

        public double getPriority() {
            return target.getPriority();
        }

        public float getTransitionDuration() {
            return target.getTransitionDuration();
        }

        public int getPlayCount() {
            return target.getPlayCount();
        }

        @Override
        public String toString() {
            return animationController.toString();
        }
    }

    protected class PlayAction {

        private final float time;
        private final String name;
        private final CompoundTag tag;

        public PlayAction(String name, float time, CompoundTag tag) {
            this.name = name;
            this.time = time;
            this.tag = tag;
        }

        public void resume(Entry entry, AnimationController animationController) {
            // check it still playing.
            if (animationController.getLoop() == SkinAnimationLoop.NONE) {
                float endTime = time + animationController.getDuration();
                if (endTime < lastAnimationTicks) {
                    return; // can't play
                }
            }
            debugLog("resume animation {}", name);
            entry.play(animationController, time, tag);
        }
    }
}
