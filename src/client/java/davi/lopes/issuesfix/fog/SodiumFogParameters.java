package davi.lopes.issuesfix.fog;

import davi.lopes.issuesfix.IssuesFix;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Builds a {@code FogParameters} value that disables fog for the Sodium render
 * path. Tries the most resilient lookup first (the static {@code NONE} field
 * already provided by Sodium) and only falls back to constructor reflection
 * when the field cannot be accessed.
 */
public final class SodiumFogParameters {
    private static final String CLASS_NAME = "net.caffeinemc.mods.sodium.client.util.FogParameters";
    private static final String NONE_FIELD = "NONE";

    private static volatile boolean resolved;
    private static volatile Object disabled;

    private SodiumFogParameters() {
    }

    public static Object disabled() {
        if (!resolved) {
            resolve();
        }
        return disabled;
    }

    private static synchronized void resolve() {
        if (resolved) {
            return;

        }
        try {
            Class<?> type = Class.forName(CLASS_NAME);
            // Preferred path: reuse the existing NONE instance (already has start=MAX, end=-MAX).
            // This is the exact value the Sodium render pipeline uses to skip fog.
            Field noneField = type.getField(NONE_FIELD);
            disabled = noneField.get(null);
            if (disabled != null) {
                resolved = true;
                return;
            }
        } catch (ReflectiveOperationException primary) {
            IssuesFix.LOGGER.debug("Sodium FogParameters.NONE lookup failed, falling back to constructor", primary);
        }

        try {
            Class<?> type = Class.forName(CLASS_NAME);
            // Fallback: build a value with cull/start/end set to MAX so fog is skipped
            // by both the vanilla getBuffer path and the Sodium chunk renderer.
            float distance = Float.MAX_VALUE;
            Constructor<?> constructor = findConstructor(type);
            disabled = constructor.newInstance(distance, distance, distance, distance, distance,
                    distance, distance, distance, distance);
        } catch (ReflectiveOperationException fallback) {
            IssuesFix.LOGGER.warn("Sodium fog compatibility is unavailable", fallback);
            disabled = null;
        }

        resolved = true;
    }

    private static Constructor<?> findConstructor(Class<?> type) throws NoSuchMethodException {
        // 1.21.x preferred: 9 floats (r, g, b, a, envStart, envEnd, renderStart, renderEnd, cullDistance)
        try {
            return type.getConstructor(float.class, float.class, float.class, float.class,
                    float.class, float.class, float.class, float.class, float.class);
        } catch (NoSuchMethodException ignored) {
            // Older Sodium may only expose the 8-float variant
            return type.getConstructor(float.class, float.class, float.class, float.class,
                    float.class, float.class, float.class, float.class);
        }
    }
}
