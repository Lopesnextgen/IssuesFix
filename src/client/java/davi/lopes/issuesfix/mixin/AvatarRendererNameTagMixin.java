package davi.lopes.issuesfix.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import davi.lopes.issuesfix.config.ConfigManager;
import davi.lopes.issuesfix.debug.IssuesFixDebug;
import davi.lopes.issuesfix.lunar.LunarNameTagCleaner;
import davi.lopes.issuesfix.lunar.LunarNameTagTracker;
import davi.lopes.issuesfix.nametag.CustomNameTagFormatter;
import davi.lopes.issuesfix.nametag.NameTagFallbackSubmitter;
import davi.lopes.issuesfix.nametag.NameTagFormatter;
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

import java.util.List;

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

        if (ConfigManager.config().clearAllNametags) {
            boolean customQueued = issuesfix$queueCustomNameTag(avatar, state, partialTick, submitState);
            state.nameTag = null;
            state.scoreText = null;
            state.nameTagAttachment = null;
            IssuesFixDebug.logExtract(avatar, state, customQueued ? "all-cleared-custom-queued" : "all-cleared", customQueued, customQueued);
            return;
        }

        if (!ConfigManager.config().nametagFix) {
            IssuesFixDebug.logExtract(avatar, state, "disabled", false, false);
            return;
        }

        if (state.nameTag != null) {
            NameTagFormatter.remember(avatar, state.nameTag);
            IssuesFixDebug.logExtract(avatar, state, "vanilla-present", false, false);
            return;
        }

        if (ConfigManager.config().respectScoreboardNametags && state.scoreText != null) {
            NameTagFormatter.remember(avatar, avatar.getDisplayName());
        }

        boolean canRepair = NameTagVisibility.canRepair(avatar, state.distanceToCameraSq);
        if (canRepair) {
            if (LunarNameTagTracker.isPresent(avatar)) {
                IssuesFixDebug.logExtract(avatar, state, "lunar-present-age-" + LunarNameTagTracker.age(avatar), false, false);
                return;
            }

            if (!LunarNameTagTracker.shouldFallback(avatar)) {
                IssuesFixDebug.logExtract(avatar, state, "waiting-lunar-age-" + LunarNameTagTracker.age(avatar), true, false);
                return;
            }

            Vec3 attachment = issuesfix$nameTagAttachment(avatar, partialTick);
            List<net.minecraft.network.chat.Component> lines = LunarNameTagTracker.fallbackLines(avatar, NameTagFormatter.fallback(avatar));
            if (lines.isEmpty()) {
                IssuesFixDebug.logExtract(avatar, state, "fallback-empty", true, false);
                return;
            }

            state.nameTagAttachment = attachment;
            submitState.issuesfix$setFallbackNameTagLines(lines, attachment, true);
            IssuesFixDebug.logExtract(avatar, state, "fallback-queued", true, true);
            return;
        }

        IssuesFixDebug.logExtract(avatar, state, "cannot-repair", false, false);
    }

    @Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void issuesfix$cancelCleanedNameTag(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState, CallbackInfo callbackInfo) {
        if (LunarNameTagCleaner.enabled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("TAIL"))
    private void issuesfix$submitFallbackNameTag(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState, CallbackInfo callbackInfo) {
        NameTagFallbackSubmitter.submit(state, poseStack, collector, cameraRenderState, "avatar-submit");
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
