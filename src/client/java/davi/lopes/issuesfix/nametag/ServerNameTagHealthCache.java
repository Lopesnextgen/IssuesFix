package davi.lopes.issuesfix.nametag;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ServerNameTagHealthCache {
    private static final Pattern HEALTH_PATTERN = Pattern.compile("[\u2764\u2665]\\s*(\\d{1,4})");
    private static final long EXPIRATION_MS = 5000L;
    private static final Map<UUID, Entry> VALUES = new ConcurrentHashMap<>();

    private ServerNameTagHealthCache() {
    }

    public static void capture(Player player, Component component) {
        if (player == null || component == null) {
            return;
        }

        String text = ChatFormatting.stripFormatting(component.getString());
        if (text == null) {
            return;
        }

        Matcher matcher = HEALTH_PATTERN.matcher(text);
        if (!matcher.find()) {
            return;
        }

        try {
            int value = Integer.parseInt(matcher.group(1));
            if (value >= 0 && value <= 2048) {
                VALUES.put(player.getUUID(), new Entry(value, System.currentTimeMillis()));
            }
        } catch (NumberFormatException ignored) {
        }
    }

    public static Integer get(Player player) {
        Entry entry = VALUES.get(player.getUUID());
        if (entry == null) {
            return null;
        }

        if (System.currentTimeMillis() - entry.updatedAt() > EXPIRATION_MS) {
            VALUES.remove(player.getUUID(), entry);
            return null;
        }

        return entry.value();
    }

    private record Entry(int value, long updatedAt) {
    }
}
