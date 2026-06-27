package davi.lopes.issuesfix.lunar;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LunarNameTagCleaner {
    private static final String KYORI_COMPONENT = "net.kyori.adventure.text.Component";
    private static final String SET_LINES_METHOD = "RHHRRCHOCOIIHCCCCHIICRRCIOCHRI";
    private static final String SET_CURRENT_LINE_METHOD = "CCIHOHIRROIHRRORIOIHICHRRHRHCC";
    private static final ConcurrentMap<String, Optional<Method>> METHODS = new ConcurrentHashMap<>();
    private static final AtomicBoolean FAILURE_LOGGED = new AtomicBoolean();
    private static volatile Object emptyComponent;

    private LunarNameTagCleaner() {
    }

    public static boolean enabled() {
        return true;
    }

    public static void clear(Object event, List<?> lines) {
        if (!enabled()) {
            return;
        }

        try {
            if (lines != null) {
                lines.clear();
            }

            Method setLines = method(event.getClass(), SET_LINES_METHOD, List.class);
            if (setLines != null) {
                setLines.invoke(event, List.of());
            }

            setCurrentLine(event, emptyComponent());
            cancel(event);
        } catch (Throwable ignored) {
            FAILURE_LOGGED.compareAndSet(false, true);
        }
    }

    public static void clear(Object event) {
        if (!enabled()) {
            return;
        }

        try {
            Method getLines = method(event.getClass(), "getLines");
            Object lines = getLines == null ? null : getLines.invoke(event);
            clear(event, lines instanceof List<?> list ? list : null);
        } catch (Throwable ignored) {
            FAILURE_LOGGED.compareAndSet(false, true);
        }
    }

    public static Object clearComponent(Object current) {
        if (!enabled()) {
            return current;
        }

        return emptyComponent();
    }

    private static void setCurrentLine(Object event, Object component) throws ReflectiveOperationException {
        Class<?> componentClass = Class.forName(KYORI_COMPONENT);
        Method method = method(event.getClass(), SET_CURRENT_LINE_METHOD, componentClass);
        if (method != null) {
            method.invoke(event, component);
        }
    }

    private static Object emptyComponent() {
        Object component = emptyComponent;
        if (component != null) {
            return component;
        }

        try {
            Class<?> componentClass = Class.forName(KYORI_COMPONENT);
            component = componentClass.getMethod("empty").invoke(null);
            emptyComponent = component;
            return component;
        } catch (Throwable throwable) {
            FAILURE_LOGGED.compareAndSet(false, true);
            return null;
        }
    }

    private static void cancel(Object event) {
        Method setCancelled = method(event.getClass(), "setCancelled", boolean.class);
        if (setCancelled != null) {
            try {
                setCancelled.invoke(event, true);
            } catch (Exception ignored) {
            }
        }
    }

    private static Method method(Class<?> type, String name, Class<?>... parameters) {
        StringBuilder key = new StringBuilder(type.getName()).append('#').append(name);
        for (Class<?> parameter : parameters) {
            key.append(':').append(parameter.getName());
        }
        return METHODS.computeIfAbsent(key.toString(), ignored -> findMethod(type, name, parameters)).orElse(null);
    }

    private static Optional<Method> findMethod(Class<?> type, String name, Class<?>... parameters) {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(name, parameters);
                method.setAccessible(true);
                return Optional.of(method);
            } catch (Exception ignored) {
                current = current.getSuperclass();
            }
        }

        try {
            Method method = type.getMethod(name, parameters);
            method.setAccessible(true);
            return Optional.of(method);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
