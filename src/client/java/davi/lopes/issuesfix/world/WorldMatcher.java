package davi.lopes.issuesfix.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.util.Collection;
import java.util.Locale;

public final class WorldMatcher {
    private WorldMatcher() {
    }

    public static boolean matches(Collection<String> entries) {
        if (entries == null || entries.isEmpty()) {
            return false;
        }

        String dimension = currentDimension();
        String server = currentServer();
        String compound = server + "|" + dimension;

        for (String entry : entries) {
            if (entry == null) {
                continue;
            }

            String normalized = entry.trim().toLowerCase(Locale.ROOT);
            if (
                normalized.equals("*") ||
                normalized.equals(dimension) ||
                normalized.equals(server) ||
                normalized.equals(compound)
            ) {
                return true;
            }
        }

        return false;
    }

    public static String currentDimension() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return "";
        }
        return client.level.dimension().identifier().toString().toLowerCase(Locale.ROOT);
    }

    public static String currentServer() {
        Minecraft client = Minecraft.getInstance();
        ServerData data = client.getCurrentServer();
        if (data != null && data.ip != null && !data.ip.isBlank()) {
            return data.ip.toLowerCase(Locale.ROOT);
        }
        return client.isSingleplayer() ? "singleplayer" : "unknown";
    }
}
