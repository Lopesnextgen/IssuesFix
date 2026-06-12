package davi.lopes.issuesfix.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import davi.lopes.issuesfix.debug.IssuesFixDebug;
import davi.lopes.issuesfix.nametag.NameTagRenderGate;
import davi.lopes.issuesfix.nametag.PlayerNameLabelMatcher;
import davi.lopes.issuesfix.nametag.PlayerNameTagRenderScope;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NameTagFeatureRenderer.Storage.class)
public abstract class NameTagFeatureStorageMixin {
    @Inject(method = "add(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void issuesfix$blockOriginalPlayerNameTag(PoseStack poseStack, Vec3 attachment, int verticalOffset, Component component, boolean visibleThroughBlocks, int light, double distanceToCameraSq, CameraRenderState cameraRenderState, CallbackInfo callbackInfo) {
        if (NameTagRenderGate.allowed()) {
            return;
        }

        if (PlayerNameTagRenderScope.active() || PlayerNameLabelMatcher.matches(component)) {
            IssuesFixDebug.logNametagBlock("feature-storage", component == null ? "" : component.getString());
            callbackInfo.cancel();
        }
    }
}
