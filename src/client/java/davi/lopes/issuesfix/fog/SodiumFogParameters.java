package davi.lopes.issuesfix.fog;

import davi.lopes.issuesfix.IssuesFix;

import java.lang.reflect.Constructor;

public final class SodiumFogParameters {
    private static final String CLASS_NAME = "net.caffeinemc.mods.sodium.client.util.FogParameters";
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
            Constructor<?> constructor = type.getConstructor(
                float.class,
                float.class,
                float.class,
                float.class,
                float.class,
                float.class,
                float.class,
                float.class,
                float.class
            );
            float distance = Float.MAX_VALUE;
            disabled = constructor.newInstance(0.0F, 0.0F, 0.0F, 0.0F, distance, distance, distance, distance, distance);
        } catch (ReflectiveOperationException exception) {
            IssuesFix.LOGGER.warn("Sodium fog compatibility is unavailable", exception);
        }

        resolved = true;
    }
}
