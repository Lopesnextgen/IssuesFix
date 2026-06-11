package davi.lopes.issuesfix;

import davi.lopes.issuesfix.config.ConfigManager;
import davi.lopes.issuesfix.debug.IssuesFixDebug;
import davi.lopes.issuesfix.update.IssuesFixUpdateChecker;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IssuesFix implements ClientModInitializer {
    public static final String MOD_ID = "issuesfix";
    public static final String MOD_NAME = "IssuesFix";
    public static final String MOD_VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        IssuesFixDebug.initialize();
        IssuesFixUpdateChecker.refresh(false);
        System.out.println("[" + MOD_NAME + "] " + MOD_VERSION + " loaded");
        System.out.println("[" + MOD_NAME + "] Debug targets: " + IssuesFixDebug.paths());
        LOGGER.info("{} {} initialized", MOD_NAME, MOD_VERSION);
        LOGGER.info("Debug logs: {}", IssuesFixDebug.paths());
        IssuesFixDebug.logLifecycle(MOD_NAME + " " + MOD_VERSION + " initialized");
    }
}
