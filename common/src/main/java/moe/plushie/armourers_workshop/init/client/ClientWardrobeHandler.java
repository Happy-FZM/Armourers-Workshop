package moe.plushie.armourers_workshop.init.client;

import com.mojang.blaze3d.vertex.PoseStack;
import moe.plushie.armourers_workshop.api.skin.ISkinToolType;
import moe.plushie.armourers_workshop.compatibility.client.AbstractBufferSource;
import moe.plushie.armourers_workshop.compatibility.client.AbstractPoseStack;
import moe.plushie.armourers_workshop.core.armature.Armatures;
import moe.plushie.armourers_workshop.core.client.bake.BakedArmature;
import moe.plushie.armourers_workshop.core.client.bake.BakedFirstPersonArmature;
import moe.plushie.armourers_workshop.core.client.bake.SkinBakery;
import moe.plushie.armourers_workshop.core.client.other.EntityRenderData;
import moe.plushie.armourers_workshop.core.client.other.EntitySlot;
import moe.plushie.armourers_workshop.core.client.other.FindableSkinManager;
import moe.plushie.armourers_workshop.core.client.other.SkinItemProperties;
import moe.plushie.armourers_workshop.core.client.other.SkinItemSource;
import moe.plushie.armourers_workshop.core.client.other.SkinRenderContext;
import moe.plushie.armourers_workshop.core.client.other.SkinRenderHelper;
import moe.plushie.armourers_workshop.core.client.other.SkinRenderTesselator;
import moe.plushie.armourers_workshop.core.client.render.ExtendedItemRenderer;
import moe.plushie.armourers_workshop.core.client.skinrender.SkinRenderer;
import moe.plushie.armourers_workshop.core.client.skinrender.patch.FallbackEntityRenderPatch;
import moe.plushie.armourers_workshop.core.client.skinrender.patch.LivingEntityRenderPatch;
import moe.plushie.armourers_workshop.core.data.ticket.Tickets;
import moe.plushie.armourers_workshop.core.entity.MannequinEntity;
import moe.plushie.armourers_workshop.core.math.Vector3f;
import moe.plushie.armourers_workshop.core.skin.SkinDescriptor;
import moe.plushie.armourers_workshop.core.skin.SkinTypes;
import moe.plushie.armourers_workshop.core.utils.OpenItemDisplayContext;
import moe.plushie.armourers_workshop.core.utils.TickUtils;
import moe.plushie.armourers_workshop.init.ModConfig;
import moe.plushie.armourers_workshop.init.ModDebugger;
import moe.plushie.armourers_workshop.init.ModItems;
import moe.plushie.armourers_workshop.utils.EmbeddedSkinStack;
import moe.plushie.armourers_workshop.utils.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ClientWardrobeHandler {

    private static Runnable INVENTORY_RENDER_POST_EVENT = null;
    public static ItemStack RENDERING_GUI_ITEM = null;

    public static final float SCALE = 1 / 16f;

    public static void init() {
    }

    public static void tick(Entity entity) {
        var renderData = EntityRenderData.of(entity);
        if (renderData != null) {
            renderData.tick(entity);
        }
        for (var passenger : entity.getPassengers()) {
            tick(passenger);
        }
    }

    public static void startRenderGuiItem(ItemStack itemStack) {
        RENDERING_GUI_ITEM = itemStack;
    }

    public static void endRenderGuiItem(ItemStack itemStack) {
        RENDERING_GUI_ITEM = null;
    }

    public static void onRenderSpecificHand(LivingEntity entity, float partialTicks, int packedLight, OpenItemDisplayContext displayContext, PoseStack poseStackIn, MultiBufferSource buffersIn, Runnable cancelHandler) {
        var renderData = EntityRenderData.of(entity);
        if (renderData == null) {
            return;
        }
        var renderingTasks = renderData.getArmorSkins();
        if (renderingTasks.isEmpty()) {
            return;
        }
        var poseStack = AbstractPoseStack.wrap(poseStackIn);
        var bufferSource = AbstractBufferSource.wrap(buffersIn);
        var armature = BakedFirstPersonArmature.defaultBy(displayContext);

        poseStack.pushPose();
        poseStack.scale(-SCALE, -SCALE, SCALE);

        var overrideHandModel = renderData.getOverriddenManager().overrideHandModel(displayContext);
        var context = SkinRenderContext.alloc(renderData, packedLight, partialTicks, displayContext);

        context.setOverlay(OverlayTexture.NO_OVERLAY);
        context.setLightmap(packedLight);
        context.setPartialTicks(partialTicks);
        context.setAnimationTicks(TickUtils.animationTicks());

        context.setPoseStack(poseStack);
        context.setBufferSource(bufferSource);
        context.setModelViewStack(AbstractPoseStack.create(RenderSystem.getExtendedModelViewStack()));

        context.setOutlineColor(0); // no show in head?

        int count = render(entity, armature, context, renderData.getArmorSkins());
        if (count != 0 && overrideHandModel && !ModDebugger.handOverride) {
            cancelHandler.run();
        }
        context.release();

        poseStack.popPose();
    }

    public static void onRenderEntityPre(Entity entity, float partialTicks, PoseStack poseStackIn, MultiBufferSource bufferSourceIn, int packedLight, EntityRenderer<?> entityRenderer) {
        FallbackEntityRenderPatch.activate(entity, partialTicks, packedLight, poseStackIn, entityRenderer, null);
    }

    public static void onRenderEntity(Entity entity, float partialTicks, PoseStack poseStackIn, MultiBufferSource bufferSourceIn, int packedLight, EntityRenderer<?> entityRenderer) {
        FallbackEntityRenderPatch.apply(entity, poseStackIn, bufferSourceIn, null);
    }

    public static void onRenderEntityPost(Entity entity, float partialTicks, PoseStack poseStackIn, MultiBufferSource bufferSourceIn, int packedLight, EntityRenderer<?> entityRenderer) {
        FallbackEntityRenderPatch.deactivate(entity, null);
    }


    public static void onRenderLivingEntityPre(LivingEntity entity, float partialTicks, int packedLight, PoseStack poseStackIn, MultiBufferSource bufferSourceIn, LivingEntityRenderer<?, ?> entityRenderer) {
        LivingEntityRenderPatch.activate(entity, partialTicks, packedLight, poseStackIn, entityRenderer, null);
    }

    public static void onRenderLivingEntity(LivingEntity entity, float partialTicks, int packedLight, PoseStack poseStackIn, MultiBufferSource bufferSourceIn, LivingEntityRenderer<?, ?> entityRenderer) {
        LivingEntityRenderPatch.apply(entity, poseStackIn, bufferSourceIn, null);
    }

    public static void onRenderLivingEntityPost(LivingEntity entity, float partialTicks, int packedLight, PoseStack poseStackIn, MultiBufferSource bufferSourceIn, LivingEntityRenderer<?, ?> entityRenderer) {
        LivingEntityRenderPatch.deactivate(entity, null);
    }

    @Nullable
    public static EmbeddedSkinStack getEmbeddedSkinStack(@Nullable LivingEntity entity, @Nullable Level level, ItemStack itemStack, OpenItemDisplayContext itemDisplayContext, BakedModel bakedModel) {
        // when the wardrobe has override skin of the item,
        // we easily got a conclusion of the needs embedded skin.
        if (RENDERING_GUI_ITEM != itemStack) {
            var renderData = EntityRenderData.of(entity);
            if (renderData != null) {
                for (var entry : renderData.getItemSkins(itemStack, entity instanceof MannequinEntity)) {
                    return new EmbeddedSkinStack(0, entry);
                }
            }
        }
        // Try to get skin descriptor from item stack.
        var descriptor = SkinDescriptor.of(itemStack);
        if (descriptor.isEmpty()) {
            // Try to get skin descriptor from item model config.
            descriptor = FindableSkinManager.getInstance().getSkin(bakedModel);
            if (!descriptor.isEmpty()) {
                return new EmbeddedSkinStack(1, descriptor, itemStack);
            }
            return null;
        }
        // when the item is a skin item itself,
        // we easily got a conclusion of the needs embedded skin.
        if (itemStack.is(ModItems.SKIN.get())) {
            return new EmbeddedSkinStack(2, descriptor, itemStack);
        }
        // we allow server manually control the item whether to use the embedded renderer.
        if (descriptor.getOptions().getEmbeddedItemRenderer() != 0) {
            if (descriptor.getOptions().getEmbeddedItemRenderer() == 2) {
                return new EmbeddedSkinStack(1, descriptor, itemStack);
            }
            return null;
        }
        // when the skin item, we no required enable of embed skin option in the config.
        if (ModConfig.enableEmbeddedSkinRenderer() || descriptor.getType() == SkinTypes.ITEM) {
            return new EmbeddedSkinStack(1, descriptor, itemStack);
        }
        return null;
    }

    public static void renderEmbeddedSkin(@Nullable LivingEntity entity, @Nullable Level level, ItemStack itemStack, EmbeddedSkinStack embeddedStack, @Nullable SkinItemProperties embeddedProperties, OpenItemDisplayContext itemDisplayContext, boolean leftHandHackery, PoseStack poseStackIn, MultiBufferSource buffersIn, BakedModel bakedModel, int packedLight, int overlay, CallbackInfo callback) {
        int counter = 0;
        switch (itemDisplayContext) {
            case GUI:
            case GROUND:
            case FIXED: {
                counter = _renderEmbeddedSkinInBox(embeddedStack, embeddedProperties, itemDisplayContext, leftHandHackery, poseStackIn, buffersIn, packedLight, overlay, 0);
                break;
            }
            case THIRD_PERSON_LEFT_HAND:
            case THIRD_PERSON_RIGHT_HAND:
            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND: {
                // first person can't support render outline.
                var outlineColor = 0;
                if (entity != null && itemDisplayContext.isThirdPerson()) {
                    outlineColor = entity.getOutlineColor();
                }

                // in special case, entity hold item type skin.
                // so we need replace it to custom renderer.
                var embeddedSlot = embeddedStack.getEntry();
                if (embeddedSlot == null) {
                    if (shouldRenderInBox(embeddedStack)) {
                        counter = _renderEmbeddedSkinInBox(embeddedStack, embeddedProperties, itemDisplayContext, leftHandHackery, poseStackIn, buffersIn, packedLight, overlay, outlineColor);
                    } else {
                        // use this case:
                        //  YDM's Weapon Master
                        counter = _renderEmbeddedSkin(embeddedStack, embeddedProperties, itemDisplayContext, leftHandHackery, poseStackIn, buffersIn, packedLight, overlay, outlineColor);
                    }
                    break;
                }
                // the backpack skin can't apply into hand item renderer by the wardrobe,
                // it only rendering in the entity back by third-party mods:
                //   Sophisticated Backpacks
                //   Traveler's Backpack
                if (embeddedSlot.getSkinType() == SkinTypes.ITEM_BACKPACK && embeddedSlot.getSlotType() == EntitySlot.Type.IN_WARDROBE) {
                    return;
                }
                var renderData = EntityRenderData.of(entity);
                if (renderData != null) {
//                    poseStack.translate(0, 1, -2);
//                    RenderUtils.drawPoint(poseStack, null, 2, buffers);
                    var poseStack = AbstractPoseStack.wrap(poseStackIn);
                    var bufferSource = AbstractBufferSource.wrap(buffersIn);
                    var armature = BakedArmature.defaultBy(Armatures.ANY);

                    poseStack.pushPose();
                    poseStack.scale(-SCALE, -SCALE, SCALE);

                    var context = SkinRenderContext.alloc(renderData, packedLight, 0, itemDisplayContext);

                    context.setOverlay(OverlayTexture.NO_OVERLAY);
                    context.setLightmap(packedLight);
                    context.setPartialTicks(0);
                    context.setAnimationTicks(TickUtils.animationTicks());

                    context.setPoseStack(poseStack);
                    context.setBufferSource(bufferSource);
                    context.setModelViewStack(AbstractPoseStack.create(RenderSystem.getExtendedModelViewStack()));

                    context.setOutlineColor(outlineColor);

                    context.setItemSource(SkinItemSource.create(800, itemStack, itemDisplayContext, embeddedProperties));
                    context.setUseItemTransforms(true);
                    counter = render(entity, armature, context, Collections.singleton(embeddedSlot));
                    context.release();

                    poseStack.popPose();
                }
                break;
            }
            default: {
                // we not support unknown operates.
                break;
            }
        }
        if (counter != 0 && !ModDebugger.itemOverride) {
            callback.cancel();
        }
    }

    private static int _renderEmbeddedSkinInBox(EmbeddedSkinStack embeddedStack, @Nullable SkinItemProperties embeddedProperties, OpenItemDisplayContext itemDisplayContext, boolean leftHandHackery, PoseStack poseStackIn, MultiBufferSource buffersIn, int packedLight, int overlay, int outlineColor) {
        int count = 0;
        var descriptor = embeddedStack.getDescriptor();
        var bakedSkin = SkinBakery.getInstance().loadSkin(descriptor, Tickets.INVENTORY);
        if (bakedSkin == null) {
            return count;
        }
        var poseStack = AbstractPoseStack.wrap(poseStackIn);
        var buffers = AbstractBufferSource.wrap(buffersIn);
        var rotation = Vector3f.ZERO;
        var scale = Vector3f.ONE;

        poseStack.pushPose();

        var itemSource = SkinItemSource.create(descriptor.sharedItemStack());
        itemSource.setScale(scale);
        itemSource.setRotation(rotation);
        itemSource.setDisplayContext(itemDisplayContext);

        var scheme = descriptor.getPaintScheme();
        count = ExtendedItemRenderer.renderSkinInBox(bakedSkin, scheme, 0, packedLight, outlineColor, itemSource, poseStack, buffers);

        poseStack.popPose();

        return count;
    }

    private static int _renderEmbeddedSkin(EmbeddedSkinStack embeddedStack, @Nullable SkinItemProperties embeddedProperties, OpenItemDisplayContext itemDisplayContext, boolean leftHandHackery, PoseStack poseStackIn, MultiBufferSource buffersIn, int packedLight, int overlay, int outlineColor) {
        int count = 0;
        var descriptor = embeddedStack.getDescriptor();
        var tesselator = SkinRenderTesselator.create(descriptor, Tickets.INVENTORY);
        if (tesselator == null) {
            return count;
        }
        var poseStack = AbstractPoseStack.wrap(poseStackIn);
        var bufferSource = AbstractBufferSource.wrap(buffersIn);

        poseStack.pushPose();
        poseStack.scale(-SCALE, -SCALE, SCALE);

        tesselator.setRenderData(EntityRenderData.of(tesselator.getMannequin()));

        tesselator.setPartialTicks(0);
        tesselator.setLightmap(packedLight);

        tesselator.setPoseStack(poseStack);
        tesselator.setBufferSource(bufferSource);
        tesselator.setModelViewStack(AbstractPoseStack.create(RenderSystem.getExtendedModelViewStack()));

        tesselator.setColorScheme(descriptor.getPaintScheme());
        tesselator.setItemSource(SkinItemSource.create(800, embeddedStack.getItemStack(), itemDisplayContext, embeddedProperties));
        tesselator.setUseItemTransforms(true);
        tesselator.setDisplayBox(null);
        tesselator.setDisplayContext(itemDisplayContext);
        tesselator.setOutlineColor(outlineColor);

        count = tesselator.draw();

        poseStack.popPose();
        return count;
    }

    public static void onRenderInventoryEntityPre(LivingEntity entity, int x, int y, int scale, float mouseX, float mouseY) {
        if (!ModConfig.Client.enableEntityInInventoryClip) {
            return;
        }
        int left, top, width, height;
        switch (scale) {
            case 20: // in creative container screen
                width = 32;
                height = 43;
                left = x - width / 2 + 1;
                top = y - height + 4;
                break;

            case 30: // in survival container screen
                width = 49;
                height = 70;
                left = x - width / 2 - 1;
                top = y - height + 3;
                break;

            default:
                return;
        }
        RenderSystem.addClipRect(left, top, width, height);
        INVENTORY_RENDER_POST_EVENT = RenderSystem::removeClipRect;
    }

    public static void onRenderInventoryEntityPost(LivingEntity entity) {
        if (INVENTORY_RENDER_POST_EVENT != null) {
            INVENTORY_RENDER_POST_EVENT.run();
            INVENTORY_RENDER_POST_EVENT = null;
        }
    }

    public static int render(Entity entity, BakedArmature bakedArmature, SkinRenderContext context, Iterable<EntitySlot> entries) {
        int r = 0;
        for (var entry : entries) {
            var bakedSkin = entry.getSkin();
            var itemSource = context.getItemSource();
            var itemStack = itemSource.getItem();
            if (itemStack.isEmpty()) {
                itemStack = entry.getItemStack();
            }
            if (itemSource == SkinItemSource.EMPTY) {
                itemSource = SkinItemSource.create(itemStack);
            }
            itemSource.setItem(itemStack);
            itemSource.setRenderPriority(entry.getRenderPriority());
            context.setItemSource(itemSource);
            context.setOverlay(entry.getOverrideOverlay(entity));
            bakedSkin.setupAnim(entity, bakedArmature, context);
            var paintScheme = bakedSkin.resolve(entity, entry.getPaintScheme());
            if (context.isUseItemTransforms()) {
                var itemTransform = bakedSkin.getItemTransform();
                itemTransform.apply(context.getPoseStack(), entity, bakedSkin, context);
            }
            SkinRenderer.render(entity, bakedArmature, bakedSkin, paintScheme, context);
            r += SkinRenderHelper.getRenderCount(bakedSkin);
        }
        return r;
    }

    private static boolean shouldRenderInBox(EmbeddedSkinStack embeddedStack) {
        // for the item required render to box.
        if (embeddedStack.getMode() == 2) {
            return true;
        }
        var skinType = embeddedStack.getDescriptor().getType();
        if (skinType == SkinTypes.BOAT || skinType == SkinTypes.ITEM_FISHING || skinType == SkinTypes.HORSE) {
            return true;
        }
        // for the tool type skin, don't render in the box.
        if (skinType instanceof ISkinToolType) {
            return false;
        }
        // for the item type skin, don't render in the box.
        if (skinType == SkinTypes.ITEM) {
            return false;
        }
        return true;
    }
}
