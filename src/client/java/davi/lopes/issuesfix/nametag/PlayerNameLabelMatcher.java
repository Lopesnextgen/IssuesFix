package davi.lopes.issuesfix.nametag;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlayerNameLabelMatcher {
    private static final double MAX_HORIZONTAL_DISTANCE_SQUARED = 9.0;
    private static final double MAX_VERTICAL_DISTANCE = 5.0;
    private static final Pattern HEALTH_NAME_PATTERN = Pattern.compile("([A-Za-z0-9_]{3,16})\\s*[\u2764\u2665]\\s*\\d{1,4}");

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
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        String label = plain(component);
        if (level == null || connection == null || label.isBlank()) {
            return null;
        }

        Matcher matcher = HEALTH_NAME_PATTERN.matcher(label);
        if (!matcher.find()) {
            return null;
        }

        PlayerInfo info = connection.getPlayerInfoIgnoreCase(matcher.group(1));
        if (info == null) {
            return null;
        }

        Player player = level.getPlayerByUUID(info.getProfile().id());
        return player != null && near(player, x, y, z) ? player : null;
    }

    private static boolean near(Player player, double x, double y, double z) {
        double deltaX = player.getX() - x;
        double deltaZ = player.getZ() - z;
        double deltaY = Math.abs(player.getY() - y);
        return deltaX * deltaX + deltaZ * deltaZ <= MAX_HORIZONTAL_DISTANCE_SQUARED
            && deltaY <= MAX_VERTICAL_DISTANCE;
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

    private static String plain(Component component) {
        if (component == null) {
            return "";
        }

        String stripped = ChatFormatting.stripFormatting(component.getString());
        return stripped == null ? "" : stripped;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }

        String stripped = ChatFormatting.stripFormatting(value);
        return stripped == null ? "" : stripped.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}
