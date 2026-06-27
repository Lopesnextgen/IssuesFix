package davi.lopes.issuesfix.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
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

import java.util.List;
import java.util.Locale;

@Mixin(NameTagFeatureRenderer.Storage.class)
public abstract class NameTagFeatureStorageMixin {
    private static final List<String> HOLOGRAM_SIGNAL_WORDS = List.of(
        "welcome", "click", "spawn", "reward", "shop", "buy", "sell",
        "tp ", "teleport", "warp", "anunci", "aviso", "loja",
        "loot", "drop", "kit", "vote", "discord", "site"
    );

    @Inject(method = "add(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void issuesfix$blockOriginalPlayerNameTag(PoseStack poseStack, Vec3 attachment, int verticalOffset, Component component, boolean visibleThroughBlocks, int light, double distanceToCameraSq, CameraRenderState cameraRenderState, CallbackInfo callbackInfo) {
        if (NameTagRenderGate.allowed()) {
            return;
        }

        if (PlayerNameTagRenderScope.active()
            || PlayerNameLabelMatcher.matches(component)
            || PlayerNameLabelMatcher.matchesNearAttachment(attachment)) {
            callbackInfo.cancel();
            return;
        }

        if (looksLikePlayerLabel(component, distanceToCameraSq)) {
            callbackInfo.cancel();
        }
    }

    private static boolean looksLikePlayerLabel(Component component, double distanceToCameraSq) {
        if (component == null) {
            return false;
        }
        String text = component.getString();
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        if (trimmed.length() > 32 || trimmed.contains("\n")) {
            return false;
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        for (String signal : HOLOGRAM_SIGNAL_WORDS) {
            if (lower.contains(signal)) {
                return false;
            }
        }
        return distanceToCameraSq < 64.0 * 64.0;
    }
}
