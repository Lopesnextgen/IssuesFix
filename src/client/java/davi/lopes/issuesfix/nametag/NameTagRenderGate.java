package davi.lopes.issuesfix.nametag;

import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class NameTagRenderGate {
    private static final ThreadLocal<Boolean> ALLOWED = ThreadLocal.withInitial(() -> false);
    private static final Set<Component> CUSTOM_COMPONENTS = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

    private NameTagRenderGate() {
    }

    public static boolean allowed() {
        return ALLOWED.get();
    }

    public static void runAllowed(Runnable runnable) {
        boolean previous = ALLOWED.get();
        ALLOWED.set(true);
        try {
            runnable.run();
        } finally {
            ALLOWED.set(previous);
        }
    }

    public static void markCustom(Component component) {
        if (component != null) {
            CUSTOM_COMPONENTS.add(component);
        }
    }

    public static boolean consumeCustom(Component component) {
        return component != null && CUSTOM_COMPONENTS.remove(component);
    }
}
