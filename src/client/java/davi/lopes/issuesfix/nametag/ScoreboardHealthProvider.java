package davi.lopes.issuesfix.nametag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ScoreboardHealthProvider {
    private static final int MAX_REASONABLE_HEALTH = 2048;
    private static final long SCOREBOARD_CACHE_MS = 1000L;
    private static final Map<UUID, CachedHealth> SCOREBOARD_CACHE = new ConcurrentHashMap<>();

    private ScoreboardHealthProvider() {
    }

    public static Health resolve(Entity entity, String visibleName) {
        if (entity instanceof Player player) {
            Integer serverNameTagHealth = ServerNameTagHealthCache.get(player);
            if (serverNameTagHealth != null) {
                return new Health(serverNameTagHealth, "server_nametag");
            }

            CachedHealth cached = SCOREBOARD_CACHE.get(player.getUUID());
            long now = System.currentTimeMillis();
            if (cached != null && now - cached.updatedAt() < SCOREBOARD_CACHE_MS) {
                return cached.health();
            }

            Health resolved = resolveScoreboard(entity, visibleName);
            SCOREBOARD_CACHE.put(player.getUUID(), new CachedHealth(resolved, now));
            return resolved;
        }

        return resolveScoreboard(entity, visibleName);
    }

    private static Health resolveScoreboard(Entity entity, String visibleName) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return null;
        }

        Scoreboard scoreboard = connection.scoreboard();
        Set<String> names = names(entity, visibleName);
        Health belowName = resolve(scoreboard, names, scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME), "below_name", true);
        if (belowName != null) {
            return belowName;
        }

        for (Objective objective : scoreboard.getObjectives()) {
            Health health = resolve(scoreboard, names, objective, "hearts", false);
            if (health != null) {
                return health;
            }
        }

        return resolve(scoreboard, names, scoreboard.getDisplayObjective(DisplaySlot.LIST), "list", false);
    }

    private static Set<String> names(Entity entity, String visibleName) {
        Set<String> names = new LinkedHashSet<>();
        add(names, entity.getScoreboardName());
        add(names, visibleName);
        Component name = entity.getName();
        if (name != null) {
            add(names, name.getString());
        }
        return names;
    }

    private static void add(Set<String> names, String value) {
        if (value != null && !value.isBlank()) {
            names.add(value.trim());
        }
    }

    private static Health resolve(Scoreboard scoreboard, Set<String> names, Objective objective, String source, boolean acceptAnyBelowName) {
        if (objective == null || (!acceptAnyBelowName && !healthObjective(objective))) {
            return null;
        }

        for (String name : names) {
            ReadOnlyScoreInfo score = scoreboard.getPlayerScoreInfo(ScoreHolder.forNameOnly(name), objective);
            if (score == null) {
                continue;
            }

            int value = score.value();
            if (value >= 0 && value <= MAX_REASONABLE_HEALTH) {
                return new Health(value, source + ":" + objective.getName());
            }
        }

        return null;
    }

    private static boolean healthObjective(Objective objective) {
        if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            return true;
        }

        return healthName(objective.getName()) || healthName(string(objective.getDisplayName()));
    }

    private static boolean healthName(String value) {
        if (value == null) {
            return false;
        }

        String clean = value.toLowerCase();
        return clean.contains("health") || clean.contains("vida") || clean.contains("hp");
    }

    private static String string(Component component) {
        return component == null ? "" : component.getString();
    }

    public record Health(int value, String source) {
    }

    private record CachedHealth(Health health, long updatedAt) {
    }
}
