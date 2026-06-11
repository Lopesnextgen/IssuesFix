package davi.lopes.issuesfix.update;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import davi.lopes.issuesfix.IssuesFix;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class IssuesFixUpdateChecker {
    public static final String OWNER = "Lopesnextgen";
    public static final String REPOSITORY = "IssuesFix";
    public static final String RELEASES_URL = "https://github.com/Lopesnextgen/IssuesFix/releases";

    private static final URI LATEST_RELEASE_API = URI.create("https://api.github.com/repos/" + OWNER + "/" + REPOSITORY + "/releases/latest");
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(6)).build();
    private static volatile Snapshot snapshot = Snapshot.checking(IssuesFix.MOD_VERSION);
    private static volatile CompletableFuture<Snapshot> activeCheck = CompletableFuture.completedFuture(snapshot);

    private IssuesFixUpdateChecker() {
    }

    public static Snapshot snapshot() {
        return snapshot;
    }

    public static synchronized CompletableFuture<Snapshot> refresh(boolean force) {
        if (!force && snapshot.status() == Status.WORKING) {
            return CompletableFuture.completedFuture(snapshot);
        }

        if (!activeCheck.isDone()) {
            return activeCheck;
        }

        snapshot = Snapshot.checking(IssuesFix.MOD_VERSION);
        HttpRequest request = HttpRequest.newBuilder(LATEST_RELEASE_API)
            .timeout(Duration.ofSeconds(8))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", IssuesFix.MOD_NAME + "/" + IssuesFix.MOD_VERSION)
            .GET()
            .build();

        activeCheck = CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString()).handle((response, throwable) -> {
            Snapshot result;
            try {
                if (throwable != null) {
                    result = Snapshot.failed(IssuesFix.MOD_VERSION, throwable.getClass().getSimpleName());
                } else if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    result = Snapshot.failed(IssuesFix.MOD_VERSION, "HTTP " + response.statusCode());
                } else {
                    JsonObject release = JsonParser.parseString(response.body()).getAsJsonObject();
                    String releaseVersion = normalize(jsonString(release, "tag_name", IssuesFix.MOD_VERSION));
                    String releaseUrl = jsonString(release, "html_url", RELEASES_URL);
                    String author = OWNER;
                    if (release.has("author") && release.get("author").isJsonObject()) {
                        author = jsonString(release.getAsJsonObject("author"), "login", OWNER);
                    }

                    boolean updateAvailable = compareVersions(releaseVersion, IssuesFix.MOD_VERSION) > 0;
                    result = new Snapshot(Status.WORKING, IssuesFix.MOD_VERSION, releaseVersion, updateAvailable, releaseUrl, author, "");
                }
            } catch (Exception exception) {
                result = Snapshot.failed(IssuesFix.MOD_VERSION, exception.getClass().getSimpleName());
            }
            snapshot = result;
            return result;
        });
        return activeCheck;
    }

    public static int compareVersions(String left, String right) {
        int[] a = parts(left);
        int[] b = parts(right);
        int length = Math.max(a.length, b.length);
        for (int index = 0; index < length; index++) {
            int leftPart = index < a.length ? a[index] : 0;
            int rightPart = index < b.length ? b[index] : 0;
            if (leftPart != rightPart) {
                return Integer.compare(leftPart, rightPart);
            }
        }
        return 0;
    }

    public static String normalize(String version) {
        if (version == null || version.isBlank()) {
            return "0.0.0";
        }

        String value = version.trim().toLowerCase(Locale.ROOT);
        if (value.startsWith("v")) {
            value = value.substring(1);
        }
        return value;
    }

    private static int[] parts(String version) {
        String normalized = normalize(version);
        String numeric = normalized.split("[^0-9.]", 2)[0];
        if (numeric.isBlank()) {
            return new int[] {0};
        }

        String[] values = numeric.split("\\.");
        int[] parts = new int[values.length];
        for (int index = 0; index < values.length; index++) {
            try {
                parts[index] = Integer.parseInt(values[index]);
            } catch (NumberFormatException exception) {
                parts[index] = 0;
            }
        }
        return parts;
    }

    private static String jsonString(JsonObject object, String key, String fallback) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return fallback;
        }

        String value = object.get(key).getAsString();
        return value == null || value.isBlank() ? fallback : value;
    }

    public enum Status {
        CHECKING,
        WORKING,
        FAILED
    }

    public record Snapshot(Status status, String localVersion, String releaseVersion, boolean updateAvailable, String releaseUrl, String author, String error) {
        public static Snapshot checking(String localVersion) {
            return new Snapshot(Status.CHECKING, localVersion, localVersion, false, RELEASES_URL, OWNER, "");
        }

        public static Snapshot failed(String localVersion, String error) {
            return new Snapshot(Status.FAILED, localVersion, localVersion, false, RELEASES_URL, OWNER, error == null ? "" : error);
        }
    }
}
