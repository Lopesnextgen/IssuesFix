package davi.lopes.issuesfix.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import davi.lopes.issuesfix.IssuesFix;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("issuesfix.json");
    private static IssuesFixConfig config = new IssuesFixConfig();

    private ConfigManager() {
    }

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                try (var reader = Files.newBufferedReader(PATH)) {
                    IssuesFixConfig loaded = GSON.fromJson(reader, IssuesFixConfig.class);
                    config = loaded == null ? new IssuesFixConfig() : loaded;
                }
            }
        } catch (Exception exception) {
            IssuesFix.LOGGER.error("Failed to load configuration", exception);
            config = new IssuesFixConfig();
        }

        config.normalize();
        save();
    }

    public static void save() {
        config.normalize();

        try {
            Path parent = PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temporaryPath = PATH.resolveSibling(PATH.getFileName() + ".tmp");
            try (var writer = Files.newBufferedWriter(temporaryPath)) {
                GSON.toJson(config, writer);
            }
            try {
                Files.move(temporaryPath, PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception exception) {
                Files.move(temporaryPath, PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception exception) {
            IssuesFix.LOGGER.error("Failed to save configuration", exception);
        }
    }

    public static IssuesFixConfig config() {
        return config;
    }
}
