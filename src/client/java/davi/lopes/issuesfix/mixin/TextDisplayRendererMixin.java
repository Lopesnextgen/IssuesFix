package davi.lopes.issuesfix.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import davi.lopes.issuesfix.debug.IssuesFixDebug;
import davi.lopes.issuesfix.nametag.PlayerNameLabelMatcher;
import davi.lopes.issuesfix.nametag.TextDisplayNameTagState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayRenderer.TextDisplayRenderer.class)
public abstract class TextDisplayRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Display$TextDisplay;Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;F)V", at = @At("TAIL"))
    private void issuesfix$identifyPlayerNameTag(Display.TextDisplay display, TextDisplayEntityRenderState state, float partialTick, CallbackInfo callbackInfo) {
        Component text = state.textRenderState == null ? null : state.textRenderState.text();
        boolean playerNameTag = PlayerNameLabelMatcher.matchesNear(text, display.getX(), display.getY(), display.getZ());
        ((TextDisplayNameTagState) state).issuesfix$setPlayerNameTag(playerNameTag);
        if (playerNameTag) {
            IssuesFixDebug.logNametagBlock("text-display", text == null ? "" : text.getString());
        }
    }

    @Inject(method = "submitInner(Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IF)V", at = @At("HEAD"), cancellable = true)
    private void issuesfix$hidePlayerNameTag(TextDisplayEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int light, float opacity, CallbackInfo callbackInfo) {
        if (((TextDisplayNameTagState) state).issuesfix$isPlayerNameTag()) {
            callbackInfo.cancel();
        }
    }
}
