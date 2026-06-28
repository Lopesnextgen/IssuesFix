package davi.lopes.issuesfix.mixin;

import davi.lopes.issuesfix.IssuesFix;
import davi.lopes.issuesfix.config.ConfigManager;
import davi.lopes.issuesfix.fog.SodiumFogParameters;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Runs after Sodium adds {@code sodium$getFogParameters} (Sodium default priority is 1000),
 * so this inject can find the accessor it needs to override.
 */
@Mixin(value = FogRenderer.class, priority = 1500)
public abstract class SodiumFogRendererMixin {
    @Inject(
        method = "sodium$getFogParameters()Lnet/caffeinemc/mods/sodium/client/util/FogParameters;",
        at = @At("HEAD"),
        cancellable = true,
        require = 0,
        remap = false
    )
    private void issuesfix$disableSodiumFog(CallbackInfoReturnable<Object> callbackInfo) {
        if (!ConfigManager.config().removeFog) {
            return;
        }

        Object parameters = SodiumFogParameters.disabled();
        if (parameters == null) {
            IssuesFix.LOGGER.warn("Sodium fog override skipped: disabled parameters could not be resolved");
            return;
        }

        callbackInfo.setReturnValue(parameters);
    }
}
