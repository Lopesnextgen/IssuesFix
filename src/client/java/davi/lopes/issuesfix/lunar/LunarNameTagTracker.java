package davi.lopes.issuesfix.lunar;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import davi.lopes.issuesfix.config.ConfigManager;
import davi.lopes.issuesfix.debug.IssuesFixDebug;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LunarNameTagTracker {
    private static final String EVENT_ENTITY_METHOD = "RCIRRCRCIHHROOOHRORROOCCOOOOIH";
    private static final String BRIDGE_UUID_METHOD = "bridge$getUniqueID";
    private static final String KYORI_COMPONENT = "net.kyori.adventure.text.Component";
    private static final String GSON_SERIALIZER = "net.kyori.adventure.text.serializer.gson.GsonComponentSerializer";
    private static final String PLAIN_SERIALIZER = "net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer";
    private static final String LUNAR_UTIL = "com.moonsworth.lunar.client.util.CIIORCCORHHHHCHCCOHIROICORHCHR";
    private static final String LUNAR_ROOT_METHOD = "CHRICHIHCOICOHRCHOHHOOIIHIOHCI";
    private static final String LUNAR_NAMETAG_STORE_METHOD = "RIIHORRRIROICHIRRRIRRRRCCCIROC";
    private static final String LUNAR_NAMETAG_MAP_METHOD = "CIHICHHIOCORICCOOOHHIOICRCCCRC";
    private static final ConcurrentMap<UUID, Long> LAST_RENDERED_AT = new ConcurrentHashMap<>();
    private static final ConcurrentMap<UUID, Long> FIRST_MISSING_AT = new ConcurrentHashMap<>();
    private static final ConcurrentMap<UUID, List<Component>> CACHED_LINES = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Optional<Method>> METHODS = new ConcurrentHashMap<>();
    private static final AtomicBoolean GSON_FAILURE_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean PLAIN_FAILURE_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean LUNAR_CACHE_FAILURE_LOGGED = new AtomicBoolean();

    private LunarNameTagTracker() {
    }

    public static void observe(Object event, List<?> lines) {
        if (event == null || lines == null || lines.isEmpty() || cancelled(event)) {
            return;
        }

        UUID uuid = uuid(event);
        if (uuid == null) {
            return;
        }

        List<Component> converted = convert(lines);
        if (converted.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        LAST_RENDERED_AT.put(uuid, now);
        FIRST_MISSING_AT.remove(uuid);
        CACHED_LINES.put(uuid, converted);
        IssuesFixDebug.logLunar(uuid, "observed", converted.size(), sample(converted));
    }

    public static boolean isPresent(Entity entity) {
        return isPresent(entity.getUUID(), System.currentTimeMillis());
    }

    public static boolean shouldFallback(Entity entity) {
        UUID uuid = entity.getUUID();
        long now = System.currentTimeMillis();
        if (isPresent(uuid, now)) {
            FIRST_MISSING_AT.remove(uuid);
            return false;
        }

        long firstMissingAt = FIRST_MISSING_AT.computeIfAbsent(uuid, ignored -> now);
        return now - firstMissingAt >= ConfigManager.config().lunarFallbackDelayMs;
    }

    public static List<Component> fallbackLines(Entity entity, Component fallback) {
        UUID uuid = entity.getUUID();
        List<Component> cached = CACHED_LINES.get(uuid);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        cached = lunarCache(uuid);
        if (cached != null && !cached.isEmpty()) {
            CACHED_LINES.put(uuid, cached);
            return cached;
        }

        if (fallback == null || fallback.getString().isBlank()) {
            return List.of();
        }

        return List.of(fallback);
    }

    public static long age(Entity entity) {
        Long lastRenderedAt = LAST_RENDERED_AT.get(entity.getUUID());
        return lastRenderedAt == null ? -1L : System.currentTimeMillis() - lastRenderedAt;
    }

    private static boolean isPresent(UUID uuid, long now) {
        Long lastRenderedAt = LAST_RENDERED_AT.get(uuid);
        return lastRenderedAt != null && now - lastRenderedAt <= ConfigManager.config().lunarNametagPresenceMs;
    }

    private static UUID uuid(Object event) {
        try {
            Method entityMethod = method(event.getClass(), EVENT_ENTITY_METHOD);
            if (entityMethod == null) {
                return null;
            }

            Object bridgeEntity = entityMethod.invoke(event);
            if (bridgeEntity == null) {
                return null;
            }

            Method uuidMethod = method(bridgeEntity.getClass(), BRIDGE_UUID_METHOD);
            if (uuidMethod == null) {
                return null;
            }

            Object uuid = uuidMethod.invoke(bridgeEntity);
            return uuid instanceof UUID value ? value : null;
        } catch (Exception exception) {
            return null;
        }
    }

    private static boolean cancelled(Object event) {
        try {
            Method method = method(event.getClass(), "isCancelled");
            return method != null && Boolean.TRUE.equals(method.invoke(event));
        } catch (Exception exception) {
            return false;
        }
    }

    private static List<Component> convert(List<?> lines) {
        List<Component> components = new ArrayList<>(lines.size());
        for (Object line : lines) {
            Component component = convert(line);
            if (component != null && !component.getString().isBlank()) {
                components.add(component);
            }
        }
        return List.copyOf(components);
    }

    private static Component convert(Object line) {
        if (line instanceof Component component) {
            return component;
        }

        Component converted = convertJson(line);
        if (converted != null) {
            return converted;
        }

        String plain = plain(line);
        return plain == null || plain.isBlank() ? null : Component.literal(plain);
    }

    private static List<Component> lunarCache(UUID uuid) {
        try {
            Class<?> utilClass = Class.forName(LUNAR_UTIL);
            Object root = utilClass.getMethod(LUNAR_ROOT_METHOD).invoke(null);
            if (root == null) {
                return List.of();
            }

            Method storeMethod = method(root.getClass(), LUNAR_NAMETAG_STORE_METHOD);
            if (storeMethod == null) {
                return List.of();
            }

            Object store = storeMethod.invoke(root);
            if (store == null) {
                return List.of();
            }

            Method mapMethod = method(store.getClass(), LUNAR_NAMETAG_MAP_METHOD);
            if (mapMethod == null) {
                return List.of();
            }

            Object map = mapMethod.invoke(store);
            if (!(map instanceof Map<?, ?> values)) {
                return List.of();
            }

            Object lines = values.get(uuid);
            if (!(lines instanceof List<?> list)) {
                return List.of();
            }

            List<Component> converted = convert(list);
            if (!converted.isEmpty()) {
                IssuesFixDebug.logLunar(uuid, "cache-hit", converted.size(), sample(converted));
            }
            return converted;
        } catch (Throwable throwable) {
            if (LUNAR_CACHE_FAILURE_LOGGED.compareAndSet(false, true)) {
                IssuesFixDebug.logLunar(uuid, "cache-read-failed", 0, throwable.getClass().getSimpleName());
            }
            return List.of();
        }
    }

    private static Component convertJson(Object line) {
        try {
            Class<?> componentClass = Class.forName(KYORI_COMPONENT);
            if (!componentClass.isInstance(line)) {
                return null;
            }

            Class<?> serializerClass = Class.forName(GSON_SERIALIZER);
            Object serializer = serializerClass.getMethod("gson").invoke(null);
            Object tree = serializerClass.getMethod("serializeToTree", componentClass).invoke(serializer, line);
            if (!(tree instanceof JsonElement jsonElement)) {
                return null;
            }

            return ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, jsonElement).result().orElse(null);
        } catch (Throwable throwable) {
            if (GSON_FAILURE_LOGGED.compareAndSet(false, true)) {
                IssuesFixDebug.logLunar(null, "gson-conversion-failed", 0, throwable.getClass().getSimpleName());
            }
            return null;
        }
    }

    private static String plain(Object line) {
        try {
            Class<?> componentClass = Class.forName(KYORI_COMPONENT);
            if (!componentClass.isInstance(line)) {
                return String.valueOf(line);
            }

            Class<?> serializerClass = Class.forName(PLAIN_SERIALIZER);
            Object serializer = serializerClass.getMethod("plainText").invoke(null);
            Object plain = serializerClass.getMethod("serialize", componentClass).invoke(serializer, line);
            return plain instanceof String value ? value : null;
        } catch (Throwable throwable) {
            if (PLAIN_FAILURE_LOGGED.compareAndSet(false, true)) {
                IssuesFixDebug.logLunar(null, "plain-conversion-failed", 0, throwable.getClass().getSimpleName());
            }
            return null;
        }
    }

    private static Method method(Class<?> type, String name) {
        return METHODS.computeIfAbsent(type.getName() + "#" + name, ignored -> findMethod(type, name)).orElse(null);
    }

    private static Optional<Method> findMethod(Class<?> type, String name) {
        try {
            Method method = type.getMethod(name);
            method.setAccessible(true);
            return Optional.of(method);
        } catch (Exception ignored) {
        }

        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(name);
                method.setAccessible(true);
                return Optional.of(method);
            } catch (Exception ignored) {
                current = current.getSuperclass();
            }
        }

        return Optional.empty();
    }

    private static String sample(List<Component> lines) {
        StringBuilder builder = new StringBuilder();
        int max = Math.min(lines.size(), 3);
        for (int index = 0; index < max; index++) {
            if (!builder.isEmpty()) {
                builder.append(" | ");
            }
            builder.append(lines.get(index).getString().replace('\n', ' ').replace('\r', ' '));
        }
        return builder.toString();
    }
}
