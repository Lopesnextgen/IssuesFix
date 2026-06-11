package davi.lopes.issuesfix.debug;

import davi.lopes.issuesfix.IssuesFix;
import davi.lopes.issuesfix.config.ConfigManager;
import davi.lopes.issuesfix.config.IssuesFixConfig;
import davi.lopes.issuesfix.nametag.NameTagSubmitState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class IssuesFixDebug {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);
    private static final ConcurrentMap<String, Long> LAST_LOGGED_AT = new ConcurrentHashMap<>();
    private static final AtomicInteger LINES = new AtomicInteger();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("issuesfix-debug.log");
    private static volatile List<Path> targets = List.of(CONFIG_PATH);
    private static volatile boolean initialized;

    private IssuesFixDebug() {
    }

    public static Path path() {
        return targets.isEmpty() ? CONFIG_PATH : targets.get(0);
    }

    public static String paths() {
        StringBuilder builder = new StringBuilder();
        for (Path target : targets) {
            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(target);
        }
        return builder.toString();
    }

    public static void initialize() {
        LAST_LOGGED_AT.clear();
        LINES.set(0);
        targets = discoverTargets();
        initialized = true;
        writeRaw("");
        writeRaw("============================================================");
        writeRaw("IssuesFix " + IssuesFix.MOD_VERSION + " debug session started at " + LocalDateTime.now().format(FORMATTER));
        writeRaw("Config directory: " + FabricLoader.getInstance().getConfigDir());
        writeRaw("Minecraft directory: " + Minecraft.getInstance().gameDirectory);
        writeRaw("Debug targets: " + paths());
        writeRaw("Debug enabled: " + ConfigManager.config().debug);
    }

    public static void logLifecycle(String message) {
        log("lifecycle", message);
    }

    public static void logExtract(Avatar avatar, AvatarRenderState state, String decision, boolean canRepair, boolean fallbackQueued) {
        if (!enabled()) {
            return;
        }

        String key = "extract:" + entityId(avatar) + ":" + decision;
        if (!rateLimited(key)) {
            return;
        }

        log("extract", "decision=" + decision
            + " fallbackQueued=" + fallbackQueued
            + " canRepair=" + canRepair
            + " entity=" + entitySummary(avatar)
            + " playerName=" + component(avatar.getName())
            + " displayName=" + component(avatar.getDisplayName())
            + " stateNameTag=" + component(state.nameTag)
            + " scoreText=" + component(state.scoreText)
            + " distanceSq=" + state.distanceToCameraSq
            + " light=" + state.lightCoords
            + " outline=0x" + Integer.toHexString(state.outlineColor)
            + " hasAttachment=" + (state.nameTagAttachment != null));
    }

    public static void logSubmit(AvatarRenderState state, NameTagSubmitState submitState, String decision) {
        if (!enabled()) {
            return;
        }

        String fallback = component(submitState.issuesfix$getFallbackNameTag());
        String key = "submit:" + decision + ":" + fallback;
        if (!rateLimited(key)) {
            return;
        }

        log("submit", "decision=" + decision
            + " shouldSubmit=" + submitState.issuesfix$shouldSubmitFallbackNameTag()
            + " fallback=" + fallback
            + " fallbackAttachment=" + submitState.issuesfix$getFallbackNameTagAttachment()
            + " stateNameTag=" + component(state.nameTag)
            + " scoreText=" + component(state.scoreText)
            + " distanceSq=" + state.distanceToCameraSq);
    }

    public static void logLunar(UUID uuid, String decision, int lineCount, String sample) {
        if (!enabled()) {
            return;
        }

        String key = "lunar:" + uuid + ":" + decision + ":" + lineCount + ":" + sample;
        if (!rateLimited(key)) {
            return;
        }

        log("lunar", "decision=" + decision
            + " uuid=" + uuid
            + " lineCount=" + lineCount
            + " sample=" + sanitize(sample));
    }

    public static void logNametagBlock(String source, String sample) {
        if (!enabled()) {
            return;
        }

        String key = "nametag-block:" + source + ":" + sample;
        if (!rateLimited(key)) {
            return;
        }

        log("nametag-block", "source=" + source + " sample=" + sanitize(sample));
    }

    public static void log(String category, String message) {
        if (!enabled()) {
            return;
        }
        if (LINES.incrementAndGet() > ConfigManager.config().debugMaxLinesPerSession) {
            return;
        }
        writeRaw("[" + LocalDateTime.now().format(FORMATTER) + "] [" + category + "] " + message);
    }

    private static boolean enabled() {
        IssuesFixConfig config = ConfigManager.config();
        return initialized && config.debug;
    }

    private static boolean rateLimited(String key) {
        long now = System.currentTimeMillis();
        long every = ConfigManager.config().debugLogEveryMs;
        Long previous = LAST_LOGGED_AT.get(key);
        if (previous != null && now - previous < every) {
            return false;
        }
        LAST_LOGGED_AT.put(key, now);
        return true;
    }

    private static String entityId(Entity entity) {
        UUID uuid = entity.getUUID();
        return uuid == null ? String.valueOf(entity.getId()) : uuid.toString();
    }

    private static String entitySummary(Entity entity) {
        return entity.getClass().getSimpleName() + "#" + entity.getId() + "/" + entityId(entity);
    }

    private static String component(Component component) {
        if (component == null) {
            return "null";
        }

        String text = component.getString();
        if (text == null || text.isBlank()) {
            return "<blank>";
        }
        return text.replace('\n', ' ').replace('\r', ' ');
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "null";
        }

        return value.replace('\n', ' ').replace('\r', ' ');
    }

    private static void writeRaw(String line) {
        if (!line.isEmpty()) {
            System.out.println("[IssuesFixDebug] " + line);
        }

        for (Path target : targets) {
            try {
                Path parent = target.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.writeString(target, line + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception exception) {
                IssuesFix.LOGGER.warn("Failed to write debug log at {}", target, exception);
            }
        }
    }

    private static List<Path> discoverTargets() {
        LinkedHashSet<Path> discovered = new LinkedHashSet<>();
        add(discovered, CONFIG_PATH);

        try {
            Minecraft client = Minecraft.getInstance();
            if (client != null && client.gameDirectory != null) {
                Path gameDirectory = client.gameDirectory.toPath();
                add(discovered, gameDirectory.resolve("logs").resolve("issuesfix-debug.log"));
                add(discovered, gameDirectory.resolve("config").resolve("issuesfix-debug.log"));
            }
        } catch (Exception exception) {
            IssuesFix.LOGGER.warn("Failed to resolve Minecraft directory debug target", exception);
        }

        String home = System.getProperty("user.home");
        if (home != null && !home.isBlank()) {
            Path lunarProfile = Path.of(home, ".lunarclient", "profiles", "1.21");
            add(discovered, lunarProfile.resolve("logs").resolve("issuesfix-debug.log"));
            add(discovered, lunarProfile.resolve("config").resolve("issuesfix-debug.log"));
        }

        return List.copyOf(new ArrayList<>(discovered));
    }

    private static void add(LinkedHashSet<Path> paths, Path path) {
        paths.add(path.toAbsolutePath().normalize());
    }
}
