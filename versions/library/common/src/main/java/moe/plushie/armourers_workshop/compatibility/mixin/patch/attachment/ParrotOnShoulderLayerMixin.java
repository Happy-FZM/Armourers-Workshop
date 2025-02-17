package moe.plushie.armourers_workshop.compatibility.mixin.patch.attachment;


import com.mojang.blaze3d.vertex.PoseStack;
import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.init.client.ClientAttachmentHandler;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Available("[1.20, )")
@Mixin(ParrotOnShoulderLayer.class)
public class ParrotOnShoulderLayerMixin {

    // lambda$render$1
    @Inject(method = "method_17958", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", shift = At.Shift.AFTER))
    private void aw$renderOnShoulder(PoseStack poseStackIn, boolean bl, Player player, CompoundTag tag, MultiBufferSource bufferSourceIn, int i, float f, float g, float h, float j, EntityType<?> entityType, CallbackInfo ci) {
        ClientAttachmentHandler.onRenderParrot(player, bl, poseStackIn, bufferSourceIn);
    }
}
