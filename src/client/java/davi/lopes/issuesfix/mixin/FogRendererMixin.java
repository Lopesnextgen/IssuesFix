package davi.lopes.issuesfix.mixin;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import davi.lopes.issuesfix.IssuesFix;
import davi.lopes.issuesfix.config.ConfigManager;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
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
        if (!ConfigManager.config().removeFog) {
            return;
        }

        callbackInfo.setReturnValue(emptyBuffer.slice(0L, FogRenderer.FOG_UBO_SIZE));
    }

    /**
     * Zero out the per-frame fog calculation so renderers that read fog state
     * from {@code setupFog} (Sodium, Iris, vanilla shader pipeline) all see
     * "no fog" without having to hook each of them individually.
     */
    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private void issuesfix$clearSetupFog(Camera camera, int renderDistance, DeltaTracker deltaTracker, float partialTick, ClientLevel level, CallbackInfoReturnable<Vector4f> callbackInfo) {
        if (!ConfigManager.config().removeFog) {
            return;
        }

        callbackInfo.setReturnValue(new Vector4f(0.0F, 0.0F, 0.0F, 0.0F));
    }
}
