package davi.lopes.issuesfix.config;

import java.util.ArrayList;
import java.util.List;

public final class IssuesFixConfig {
    public boolean clearAllNametags = true;
    public boolean customNametags = true;
    public boolean customNametagBackground = false;
    public boolean customNametagShadow = true;
    public boolean removeFog = true;
    public Boolean removeTntExplosionParticles = true;
    public boolean nametagFix = true;
    public boolean onlyRepairMissingNametags = true;
    public boolean respectScoreboardNametags = true;
    public boolean playerOutlineFix = true;
    public boolean removeWhitePlayerOutlinesAutomatically = true;
    public boolean debug = true;
    public int debugLogEveryMs = 1000;
    public int debugMaxLinesPerSession = 5000;
    public int lunarNametagPresenceMs = 900;
    public int lunarFallbackDelayMs = 1300;
    public List<String> playerOutlineWorlds = new ArrayList<>();

    public void normalize() {
        if (playerOutlineWorlds == null) {
            playerOutlineWorlds = new ArrayList<>();
        }
        if (removeTntExplosionParticles == null) {
            removeTntExplosionParticles = true;
        }
        if (debugLogEveryMs < 100) {
            debugLogEveryMs = 100;
        }
        if (debugMaxLinesPerSession < 50) {
            debugMaxLinesPerSession = 50;
        }
        if (clearAllNametags && debugMaxLinesPerSession < 5000) {
            debugMaxLinesPerSession = 5000;
        }
        if (lunarNametagPresenceMs < 250) {
            lunarNametagPresenceMs = 250;
        }
        if (lunarFallbackDelayMs < 250) {
            lunarFallbackDelayMs = 250;
        }
    }
}
