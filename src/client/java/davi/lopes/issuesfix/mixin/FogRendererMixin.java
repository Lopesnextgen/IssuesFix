package davi.lopes.issuesfix.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import davi.lopes.issuesfix.config.ConfigManager;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Shadow
    @Final
    private GpuBuffer emptyBuffer;

    @Inject(method = "getBuffer", at = @At("HEAD"), cancellable = true)
    private void issuesfix$disableFog(FogRenderer.FogMode fogMode, CallbackInfoReturnable<GpuBufferSlice> callbackInfo) {
        if (ConfigManager.config().removeFog) {
            callbackInfo.setReturnValue(emptyBuffer.slice(0L, FogRenderer.FOG_UBO_SIZE));
        }
    }
}
