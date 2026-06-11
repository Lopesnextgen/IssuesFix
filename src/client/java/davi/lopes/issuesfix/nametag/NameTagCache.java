package davi.lopes.issuesfix.nametag;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NameTagCache {
    private static final Map<UUID, Component> CACHE = new ConcurrentHashMap<>();

    private NameTagCache() {
    }

    public static void update(Entity entity, Component component) {
        update(entity.getUUID(), component);
    }

    public static void update(UUID uuid, Component component) {
        if (component == null) {
            return;
        }

        String text = component.getString();
        if (text == null || text.isBlank()) {
            return;
        }

        Component existing = CACHE.get(uuid);
        if (existing == null || isBetter(component, existing)) {
            CACHE.put(uuid, component);
        }
    }

    public static Component get(Entity entity, Component fallback) {
        Component cached = CACHE.get(entity.getUUID());
        return cached != null ? cached : fallback;
    }

    public static void clear() {
        CACHE.clear();
    }

    private static boolean isBetter(Component candidate, Component current) {
        return candidate.getString().length() > current.getString().length();
    }
}
