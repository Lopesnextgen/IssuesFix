package davi.lopes.issuesfix.nametag;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public final class NameTagFormatter {
    private NameTagFormatter() {
    }

    public static void remember(Entity entity, Component component) {
        NameTagCache.update(entity, component);
    }

    public static Component fallback(Entity entity) {
        Component displayName = entity.getDisplayName();
        NameTagCache.update(entity, displayName);

        if (displayName == null || displayName.getString().trim().isEmpty()) {
            displayName = entity.getName();
        }

        return NameTagCache.get(entity, displayName);
    }
}
