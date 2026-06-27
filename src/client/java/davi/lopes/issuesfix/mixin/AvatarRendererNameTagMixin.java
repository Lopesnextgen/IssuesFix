package davi.lopes.issuesfix.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import davi.lopes.issuesfix.config.ConfigManager;
import davi.lopes.issuesfix.nametag.CustomNameTagFormatter;
import davi.lopes.issuesfix.nametag.NameTagSubmitState;
import davi.lopes.issuesfix.nametag.NameTagVisibility;
import davi.lopes.issuesfix.outline.PlayerOutlineFix;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererNameTagMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void issuesfix$repairPlayerNameTag(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo callbackInfo) {
        NameTagSubmitState submitState = (NameTagSubmitState) state;
        submitState.issuesfix$setFallbackNameTag(null, null, false);
        submitState.issuesfix$clearCustomNameTag();

        if (avatar instanceof AbstractClientPlayer player) {
            PlayerOutlineFix.apply(player, state);
        }

        issuesfix$queueCustomNameTag(avatar, state, partialTick, submitState);
        state.nameTag = null;
        state.scoreText = null;
        state.nameTagAttachment = null;
    }

    @Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void issuesfix$cancelOriginalNameTag(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState, CallbackInfo callbackInfo) {
        callbackInfo.cancel();
    }

    private Vec3 issuesfix$nameTagAttachment(Avatar avatar, float partialTick) {
        return avatar.getAttachments().get(EntityAttachment.NAME_TAG, 0, avatar.getYRot(partialTick));
    }

    private boolean issuesfix$queueCustomNameTag(Avatar avatar, AvatarRenderState state, float partialTick, NameTagSubmitState submitState) {
        if (!ConfigManager.config().customNametags || !NameTagVisibility.canRepair(avatar, state.distanceToCameraSq)) {
            return false;
        }

        Component component = CustomNameTagFormatter.format(avatar);
        if (component == null || component.getString().isBlank()) {
            return false;
        }

        Vec3 attachment = issuesfix$nameTagAttachment(avatar, partialTick);
        submitState.issuesfix$setCustomNameTag(component, attachment, true);
        return true;
    }
}
