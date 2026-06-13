package davi.lopes.issuesfix.particle;

import davi.lopes.issuesfix.config.ConfigManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;

public final class ExplosionParticleFilter {
    private ExplosionParticleFilter() {
    }

    public static boolean shouldBlock(ParticleOptions options) {
        if (!Boolean.TRUE.equals(ConfigManager.config().removeTntExplosionParticles) || options == null) {
            return false;
        }

        ParticleType<?> type = options.getType();
        return type == ParticleTypes.EXPLOSION_EMITTER || type == ParticleTypes.EXPLOSION;
    }
}
