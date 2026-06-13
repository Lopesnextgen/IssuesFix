package davi.lopes.issuesfix.mixin;

import davi.lopes.issuesfix.particle.ExplosionParticleFilter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelParticleMixin {
    @Inject(method = "doAddParticle", at = @At("HEAD"), cancellable = true)
    private void issuesfix$hideExplosionParticle(ParticleOptions options, boolean force, boolean decreased, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfo callbackInfo) {
        if (ExplosionParticleFilter.shouldBlock(options)) {
            callbackInfo.cancel();
        }
    }
}
