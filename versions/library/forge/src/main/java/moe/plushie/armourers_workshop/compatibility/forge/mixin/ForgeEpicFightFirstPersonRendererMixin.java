package moe.plushie.armourers_workshop.compatibility.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.compatibility.forge.AbstractForgeEpicFightHandler;
import moe.plushie.armourers_workshop.core.client.layer.SkinWardrobeLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.FirstPersonRenderer;
import yesman.epicfight.client.renderer.patched.layer.EmptyLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Available("[1.18, )")
@Pseudo
@Mixin(FirstPersonRenderer.class)
public abstract class ForgeEpicFightFirstPersonRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/player/LocalPlayer;Lyesman/epicfight/client/world/capabilites/entitypatch/player/LocalPlayerPatch;Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;IF)V", at = @At("HEAD"), remap = false)
    public void aw2$renderPre(LocalPlayer entityIn, LocalPlayerPatch entitypatch, LivingEntityRenderer<?, ?> renderer, MultiBufferSource bufferSourceIn, PoseStack poseStackIn, int packedLightIn, float partialTicks, CallbackInfo ci) {
        AbstractForgeEpicFightHandler.onRenderPre(entityIn, packedLightIn, partialTicks, true, poseStackIn, bufferSourceIn, renderer);
    }

    @Redirect(method = "render(Lnet/minecraft/client/player/LocalPlayer;Lyesman/epicfight/client/world/capabilites/entitypatch/player/LocalPlayerPatch;Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;IF)V", at = @At(value = "INVOKE", target = "Lyesman/epicfight/client/mesh/HumanoidMesh;drawModelWithPose(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IFFFFILyesman/epicfight/api/model/Armature;[Lyesman/epicfight/api/utils/math/OpenMatrix4f;)V", remap = false), remap = false)
    public void aw2$renderEntity(HumanoidMesh mesh, PoseStack poseStackIn, VertexConsumer builder, int packedLightIn, float r, float g, float b, float a, int overlayCoord, Armature armature, OpenMatrix4f[] poses, LocalPlayer entityIn) {
        var cir = new CallbackInfoReturnable<>("poses", true, poses);
        AbstractForgeEpicFightHandler.onRenderEntity(entityIn, armature, packedLightIn, 0, poseStackIn, null, cir);
        mesh.drawModelWithPose(poseStackIn, builder, packedLightIn, r, g, b, a, overlayCoord, armature, cir.getReturnValue());
    }

    @Inject(method = "render(Lnet/minecraft/client/player/LocalPlayer;Lyesman/epicfight/client/world/capabilites/entitypatch/player/LocalPlayerPatch;Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;IF)V", at = @At("RETURN"), remap = false)
    public void aw2$renderPost(LocalPlayer entityIn, LocalPlayerPatch entitypatch, LivingEntityRenderer<?, ?> renderer, MultiBufferSource bufferSourceIn, PoseStack poseStackIn, int packedLightIn, float partialTicks, CallbackInfo ci) {
        AbstractForgeEpicFightHandler.onRenderPost(entityIn, packedLightIn, partialTicks, poseStackIn, bufferSourceIn, renderer);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    public void aw2$init(CallbackInfo callbackInfo) {
        var renderer = FirstPersonRenderer.class.cast(this);
        renderer.addPatchedLayer(SkinWardrobeLayer.class, new EmptyLayer<>() {
            @Override
            protected void renderLayer(LocalPlayerPatch entityPatch, LocalPlayer entityIn, RenderLayer<LocalPlayer, PlayerModel<LocalPlayer>> layer, PoseStack poseStack, MultiBufferSource buffer, int packedLightIn, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
                layer.render(poseStack, buffer, packedLightIn, entityIn, partialTicks, 0, partialTicks, packedLightIn, xRot, yRot);
            }
        });
    }
}
