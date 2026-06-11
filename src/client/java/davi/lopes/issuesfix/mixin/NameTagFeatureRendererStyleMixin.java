package davi.lopes.issuesfix.mixin;

import davi.lopes.issuesfix.config.ConfigManager;
import davi.lopes.issuesfix.nametag.NameTagRenderGate;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(NameTagFeatureRenderer.class)
public abstract class NameTagFeatureRendererStyleMixin {
    @ModifyArgs(
        method = "render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/gui/Font;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)V")
    )
    private void issuesfix$styleCustomNameTagText(Args args) {
        if (!NameTagRenderGate.consumeCustom((Component) args.get(0))) {
            return;
        }

        args.set(3, ((Integer) args.get(3)) | 0xFF000000);
        if (ConfigManager.config().customNametagShadow) {
            args.set(4, true);
        }
        if (!ConfigManager.config().customNametagBackground) {
            args.set(8, 0);
        }
    }
}
