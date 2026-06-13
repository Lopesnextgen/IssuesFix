package davi.lopes.issuesfix.nametag;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

public final class PlayerNameLabelMatcher {
    private static final double MAX_HORIZONTAL_DISTANCE_SQUARED = 9.0;
    private static final double MAX_VERTICAL_DISTANCE = 5.0;

    private PlayerNameLabelMatcher() {
    }

    public static boolean matches(Component component) {
        ClientLevel level = Minecraft.getInstance().level;
        String label = normalize(component);
        if (level == null || label.isBlank()) {
            return false;
        }

        for (Player player : level.players()) {
            if (containsPlayerName(label, player)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesNear(Component component, double x, double y, double z) {
        return matchingPlayerNear(component, x, y, z) != null;
    }

    public static Player matchingPlayerNear(Component component, double x, double y, double z) {
        ClientLevel level = Minecraft.getInstance().level;
        String label = normalize(component);
        if (level == null || label.isBlank()) {
            return null;
        }

        for (Player player : level.players()) {
            double deltaX = player.getX() - x;
            double deltaZ = player.getZ() - z;
            double deltaY = Math.abs(player.getY() - y);
            if (deltaX * deltaX + deltaZ * deltaZ <= MAX_HORIZONTAL_DISTANCE_SQUARED
                && deltaY <= MAX_VERTICAL_DISTANCE
                && containsPlayerName(label, player)) {
                return player;
            }
        }
        return null;
    }

    private static boolean containsPlayerName(String label, Player player) {
        return contains(label, player.getScoreboardName())
            || contains(label, player.getName().getString());
    }

    private static boolean contains(String label, String playerName) {
        String name = normalize(playerName);
        return name.length() >= 3 && label.contains(name);
    }

    private static String normalize(Component component) {
        return component == null ? "" : normalize(component.getString());
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }

        String stripped = ChatFormatting.stripFormatting(value);
        return stripped == null ? "" : stripped.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}
