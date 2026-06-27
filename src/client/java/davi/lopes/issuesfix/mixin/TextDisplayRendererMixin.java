package davi.lopes.issuesfix.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import davi.lopes.issuesfix.nametag.PlayerNameLabelMatcher;
import davi.lopes.issuesfix.nametag.ServerNameTagHealthCache;
import davi.lopes.issuesfix.nametag.TextDisplayNameTagState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayRenderer.TextDisplayRenderer.class)
public abstract class TextDisplayRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Display$TextDisplay;Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;F)V", at = @At("TAIL"))
    private void issuesfix$identifyPlayerNameTag(Display.TextDisplay display, TextDisplayEntityRenderState state, float partialTick, CallbackInfo callbackInfo) {
        Component text = state.textRenderState == null ? null : state.textRenderState.text();
        Player player = PlayerNameLabelMatcher.matchingByName(text);
        if (player == null) {
            player = PlayerNameLabelMatcher.matchingPlayerNear(text, display.getX(), display.getY(), display.getZ());
        }
        boolean playerNameTag = player != null;
        ((TextDisplayNameTagState) state).issuesfix$setPlayerNameTag(playerNameTag);
        if (playerNameTag) {
            ServerNameTagHealthCache.capture(player, text);
        }
    }

    @Inject(method = "submitInner(Lnet/minecraft/client/renderer/entity/state/TextDisplayEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IF)V", at = @At("HEAD"), cancellable = true)
    private void issuesfix$hidePlayerNameTag(TextDisplayEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int light, float opacity, CallbackInfo callbackInfo) {
        if (((TextDisplayNameTagState) state).issuesfix$isPlayerNameTag()) {
            callbackInfo.cancel();
        }
    }
}
