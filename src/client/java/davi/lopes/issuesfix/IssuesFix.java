package davi.lopes.issuesfix;

import davi.lopes.issuesfix.config.ConfigManager;
import davi.lopes.issuesfix.update.IssuesFixUpdateChecker;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IssuesFix implements ClientModInitializer {
    public static final String MOD_ID = "issuesfix";
    public static final String MOD_NAME = "IssuesFix";
    public static final String MOD_VERSION = "1.4.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        IssuesFixUpdateChecker.refresh(false);
        System.out.println("[" + MOD_NAME + "] " + MOD_VERSION + " loaded");
        LOGGER.info("{} {} initialized", MOD_NAME, MOD_VERSION);
    }
}
