package davi.lopes.issuesfix.nametag;

import davi.lopes.issuesfix.debug.IssuesFixDebug;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CustomNameTagFormatter {
    private static final Pattern FACTION_TAG_PATTERN = Pattern.compile("\\[([^\\]]{1,16})\\]");
    private static final Pattern INTERNAL_TEAM_PATTERN = Pattern.compile("(?i)^[0-9a-f][a-z0-9_]{2,16}a$");
    private static final Map<String, Long> LAST_DEBUG = new ConcurrentHashMap<>();
    private static final long DEBUG_INTERVAL_MS = 2500L;

    private CustomNameTagFormatter() {
    }

    public static Component format(Avatar avatar) {
        String name = playerName(avatar);
        if (name.isBlank()) {
            return null;
        }

        MutableComponent component = Component.literal(name).withStyle(isFriendly(avatar) ? ChatFormatting.GREEN : ChatFormatting.RED);
        String faction = factionTag(avatar, name);
        if (!faction.isBlank()) {
            component.append(Component.literal(" ").withStyle(ChatFormatting.GRAY));
            component.append(Component.literal(faction).withStyle(ChatFormatting.GRAY));
        }
        return component;
    }

    private static String playerName(Entity entity) {
        String scoreboardName = entity.getScoreboardName();
        if (scoreboardName != null && !scoreboardName.isBlank()) {
            return scoreboardName;
        }

        Component name = entity.getName();
        return name == null ? "" : name.getString().trim();
    }

    private static boolean isFriendly(Avatar avatar) {
        LocalPlayer viewer = Minecraft.getInstance().player;
        if (viewer == null) {
            return false;
        }

        if (avatar == viewer) {
            return true;
        }

        if (sameScoreboardTeam(viewer, avatar)) {
            return true;
        }

        String viewerFaction = factionKey(factionTag(viewer, playerName(viewer)));
        String avatarFaction = factionKey(factionTag(avatar, playerName(avatar)));
        return !viewerFaction.isBlank() && viewerFaction.equalsIgnoreCase(avatarFaction);
    }

    private static boolean sameScoreboardTeam(LocalPlayer viewer, Avatar avatar) {
        Team viewerTeam = viewer.getTeam();
        Team avatarTeam = avatar.getTeam();
        if (viewerTeam == null || avatarTeam == null) {
            return false;
        }

        if (viewerTeam == avatarTeam || viewerTeam.getName().equals(avatarTeam.getName())) {
            return true;
        }

        return viewerTeam.isAlliedTo(avatarTeam) || avatarTeam.isAlliedTo(viewerTeam) || avatar.isAlliedTo(viewer);
    }

    private static String factionTag(Entity entity, String name) {
        String faction = firstFactionTag(name,
            teamPrefix(entity.getTeam()),
            teamSuffix(entity.getTeam()),
            teamDisplayName(entity.getTeam()),
            tabListDisplayName(entity),
            entity.getDisplayName()
        );
        debugSources(entity, name, faction);
        return faction;
    }

    private static Component teamPrefix(Team team) {
        return team instanceof PlayerTeam playerTeam ? playerTeam.getPlayerPrefix() : null;
    }

    private static Component teamSuffix(Team team) {
        return team instanceof PlayerTeam playerTeam ? playerTeam.getPlayerSuffix() : null;
    }

    private static Component teamDisplayName(Team team) {
        return team instanceof PlayerTeam playerTeam ? playerTeam.getDisplayName() : null;
    }

    private static Component tabListDisplayName(Entity entity) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return null;
        }

        PlayerInfo info = connection.getPlayerInfo(entity.getUUID());
        return info == null ? null : info.getTabListDisplayName();
    }

    private static String firstFactionTag(String playerName, Component... components) {
        for (Component component : components) {
            String tag = factionTagFrom(component, playerName);
            if (!tag.isBlank()) {
                return tag;
            }
        }
        return "";
    }

    private static String factionTagFrom(Component component, String playerName) {
        String value = clean(component);
        if (value.isBlank()) {
            return "";
        }

        Matcher matcher = FACTION_TAG_PATTERN.matcher(value);
        while (matcher.find()) {
            String tag = normalizeTag(matcher.group(1));
            if (validFactionTag(tag) && !internalTag(tag, playerName)) {
                return "[" + tag + "]";
            }
        }
        return "";
    }

    private static String normalizeTag(String value) {
        String clean = clean(value);
        String role = "";
        for (int index = 0; index < clean.length(); index++) {
            String current = clean.substring(index, index + 1);
            if ("#".equals(current) || "+".equals(current) || "-".equals(current)) {
                role = current;
                break;
            }
            if ("*".equals(current) || "⁎".equals(current)) {
                role = "*";
                break;
            }
            if (Character.isLetterOrDigit(clean.charAt(index))) {
                break;
            }
        }

        String tag = clean.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        return role + tag;
    }

    private static boolean validFactionTag(String tag) {
        String faction = factionKey(tag);
        return faction.length() >= 2 && faction.length() <= 8;
    }

    private static boolean internalTag(String tag, String playerName) {
        String compactTag = tag.replaceAll("\\s+", "");
        if (INTERNAL_TEAM_PATTERN.matcher(compactTag).matches()) {
            return true;
        }

        String normalizedTag = factionKey(compactTag).toLowerCase(Locale.ROOT);
        String normalizedPlayer = clean(playerName).replaceAll("[^A-Za-z0-9_]", "").toLowerCase(Locale.ROOT);
        return !normalizedPlayer.isBlank() && normalizedTag.contains(normalizedPlayer);
    }

    private static String factionKey(String faction) {
        return clean(faction).replaceAll("[\\[\\]#\\+\\*⁎\\-\\s]", "");
    }

    private static String clean(Component component) {
        return component == null ? "" : clean(component.getString());
    }

    private static String clean(String value) {
        if (value == null) {
            return "";
        }

        String stripped = ChatFormatting.stripFormatting(value);
        return stripped == null ? "" : stripped.trim();
    }

    private static void debugSources(Entity entity, String name, String faction) {
        long now = System.currentTimeMillis();
        Long previous = LAST_DEBUG.get(name);
        if (previous != null && now - previous < DEBUG_INTERVAL_MS) {
            return;
        }
        LAST_DEBUG.put(name, now);

        Team team = entity.getTeam();
        IssuesFixDebug.log("nametag-source",
            "player=" + name
                + " faction=" + (faction.isBlank() ? "none" : faction)
                + " teamName=" + (team == null ? "null" : clean(team.getName()))
                + " prefix=" + clean(teamPrefix(team))
                + " suffix=" + clean(teamSuffix(team))
                + " teamDisplay=" + clean(teamDisplayName(team))
                + " tab=" + clean(tabListDisplayName(entity))
                + " display=" + clean(entity.getDisplayName())
                + " name=" + clean(entity.getName()));
    }
}
