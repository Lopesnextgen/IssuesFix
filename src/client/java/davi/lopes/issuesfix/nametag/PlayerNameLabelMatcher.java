package davi.lopes.issuesfix.nametag;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlayerNameLabelMatcher {
    private static final double DEFAULT_HORIZONTAL_SQ = 36.0;
    private static final double DEFAULT_VERTICAL = 12.0;
    private static final double PROXIMITY_HORIZONTAL_SQ = 49.0;
    private static final double PROXIMITY_VERTICAL = 14.0;
    private static final Pattern NAME_HEART_PATTERN = Pattern.compile("([A-Za-z0-9_]{3,16})\\s*[\u2764\u2665]\\s*(\\d{1,4})");
    private static final Pattern LABEL_VALUE_PATTERN = Pattern.compile(
        "(?i)(?:\\[\\s*)?(?:hp|vid|vida|health|life)\\s*\\]?\\s*[:\\-]?\\s*(\\d{1,4})"
    );

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

    public static Player matchingByName(Component component) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || component == null) {
            return null;
        }
        String label = normalize(component);
        if (label.isBlank()) {
            return null;
        }
        for (Player player : level.players()) {
            if (containsPlayerName(label, player)) {
                return player;
            }
        }
        return null;
    }

    public static boolean matchesNearAttachment(Vec3 attachment) {
        if (attachment == null) {
            return false;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return false;
        }
        for (Player player : level.players()) {
            if (nearProximity(player, attachment)) {
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
        if (level == null || label.isBlank()) {
            return null;
        }

        Matcher nameHeart = NAME_HEART_PATTERN.matcher(label);
        if (nameHeart.find()) {
            Player player = resolveByName(connection, level, nameHeart.group(1));
            if (player != null && near(player, x, y, z)) {
                return player;
            }
        }

        if (LABEL_VALUE_PATTERN.matcher(label).find()) {
            Player closest = closestPlayer(level, x, y, z);
            if (closest != null) {
                return closest;
            }
        }

        Player closest = closestPlayer(level, x, y, z);
        if (closest != null && isLikelyPlayerLabel(label)) {
            return closest;
        }

        return null;
    }

    private static Player resolveByName(ClientPacketListener connection, ClientLevel level, String name) {
        if (connection == null) {
            return null;
        }
        PlayerInfo info = connection.getPlayerInfoIgnoreCase(name);
        if (info == null) {
            return null;
        }
        return level.getPlayerByUUID(info.getProfile().id());
    }

    private static Player closestPlayer(ClientLevel level, double x, double y, double z) {
        Player best = null;
        double bestSq = DEFAULT_HORIZONTAL_SQ;
        for (Player player : level.players()) {
            double deltaX = player.getX() - x;
            double deltaZ = player.getZ() - z;
            double deltaY = Math.abs(player.getY() - y);
            double sq = deltaX * deltaX + deltaZ * deltaZ;
            if (sq <= bestSq && deltaY <= DEFAULT_VERTICAL) {
                bestSq = sq;
                best = player;
            }
        }
        return best;
    }

    private static boolean isLikelyPlayerLabel(String label) {
        if (label.isBlank()) {
            return false;
        }
        if (label.length() > 48) {
            return false;
        }
        if (label.contains("\n")) {
            return false;
        }
        long letterCount = label.chars().filter(Character::isLetterOrDigit).count();
        return letterCount >= Math.max(3, label.length() / 3);
    }

    private static boolean near(Player player, double x, double y, double z) {
        double deltaX = player.getX() - x;
        double deltaZ = player.getZ() - z;
        double deltaY = Math.abs(player.getY() - y);
        return deltaX * deltaX + deltaZ * deltaZ <= DEFAULT_HORIZONTAL_SQ
            && deltaY <= DEFAULT_VERTICAL;
    }

    private static boolean nearProximity(Player player, Vec3 attachment) {
        double deltaX = player.getX() - attachment.x;
        double deltaZ = player.getZ() - attachment.z;
        double deltaY = Math.abs((player.getY() + player.getBbHeight()) - attachment.y);
        return deltaX * deltaX + deltaZ * deltaZ <= PROXIMITY_HORIZONTAL_SQ
            && deltaY <= PROXIMITY_VERTICAL;
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
