package davi.lopes.issuesfix.nametag;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

import java.util.Locale;

public final class CustomNameTagFormatter {
    private CustomNameTagFormatter() {
    }

    public static Component format(Avatar avatar) {
        String name = playerName(avatar);
        if (name.isBlank()) {
            return null;
        }

        MutableComponent component = Component.literal(name).withStyle(isFriendly(avatar) ? ChatFormatting.GREEN : ChatFormatting.RED);
        Component faction = factionTag(avatar, name);
        String factionText = compact(faction);
        if (!factionText.isBlank()) {
            component.append(Component.literal(" ").withStyle(ChatFormatting.GRAY));
            component.append(Component.literal(factionText).withStyle(ChatFormatting.GRAY));
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

        String viewerFaction = factionKey(viewer);
        String avatarFaction = factionKey(avatar);
        return !viewerFaction.isBlank() && viewerFaction.equals(avatarFaction);
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

    private static String factionKey(Entity entity) {
        return key(factionTag(entity, playerName(entity)));
    }

    private static Component factionTag(Entity entity, String name) {
        Component teamSuffix = teamSuffix(entity.getTeam());
        if (teamSuffix != null && !teamSuffix.getString().trim().isEmpty()) {
            return teamSuffix;
        }

        String displaySuffix = displayNameSuffix(entity.getDisplayName(), name);
        if (!displaySuffix.isBlank()) {
            return Component.literal(displaySuffix);
        }

        String teamName = teamName(entity.getTeam());
        if (!teamName.isBlank()) {
            return Component.literal(bracketed(teamName));
        }

        return null;
    }

    private static Component teamSuffix(Team team) {
        if (team instanceof PlayerTeam playerTeam) {
            Component suffix = playerTeam.getPlayerSuffix();
            if (suffix != null && !suffix.getString().trim().isEmpty()) {
                return suffix;
            }

            Component displayName = playerTeam.getDisplayName();
            if (displayName != null && !displayName.getString().trim().isEmpty()) {
                return Component.literal(bracketed(displayName.getString()));
            }
        }
        return null;
    }

    private static String teamName(Team team) {
        if (team == null || team.getName() == null) {
            return "";
        }
        return clean(team.getName());
    }

    private static String displayNameSuffix(Component displayName, String name) {
        if (displayName == null) {
            return "";
        }

        String value = clean(displayName.getString());
        String cleanName = clean(name);
        if (value.isBlank() || cleanName.isBlank()) {
            return "";
        }

        int index = value.toLowerCase(Locale.ROOT).indexOf(cleanName.toLowerCase(Locale.ROOT));
        if (index < 0) {
            return "";
        }

        String tail = value.substring(index + cleanName.length()).trim();
        if (tail.isBlank()) {
            return "";
        }

        int open = tail.indexOf('[');
        int close = tail.indexOf(']', open + 1);
        if (open >= 0 && close > open) {
            return tail.substring(open, close + 1).trim();
        }

        return tail;
    }

    private static String clean(String value) {
        if (value == null) {
            return "";
        }

        String stripped = ChatFormatting.stripFormatting(value);
        return stripped == null ? "" : stripped.trim();
    }

    private static String key(Component component) {
        return compact(component).replace("[", "").replace("]", "").replace("(", "").replace(")", "").replace(" ", "").toLowerCase(Locale.ROOT);
    }

    private static String compact(Component component) {
        if (component == null) {
            return "";
        }
        return clean(component.getString()).replaceAll("\\s+", " ");
    }

    private static String bracketed(String value) {
        String clean = clean(value);
        if (clean.isBlank()) {
            return "";
        }
        if ((clean.startsWith("[") && clean.endsWith("]")) || (clean.startsWith("(") && clean.endsWith(")"))) {
            return clean;
        }
        return "[" + clean + "]";
    }
}
