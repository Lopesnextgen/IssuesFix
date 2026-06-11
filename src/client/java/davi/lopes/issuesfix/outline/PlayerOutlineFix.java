package davi.lopes.issuesfix.outline;

import davi.lopes.issuesfix.config.ConfigManager;
import davi.lopes.issuesfix.config.IssuesFixConfig;
import davi.lopes.issuesfix.world.WorldMatcher;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public final class PlayerOutlineFix {
    private PlayerOutlineFix() {
    }

    public static void apply(AbstractClientPlayer player, AvatarRenderState state) {
        IssuesFixConfig config = ConfigManager.config();
        if (!config.playerOutlineFix || state.outlineColor == EntityRenderState.NO_OUTLINE) {
            return;
        }

        if (WorldMatcher.matches(config.playerOutlineWorlds) || shouldRemoveAutomatically(config, state.outlineColor)) {
            state.outlineColor = EntityRenderState.NO_OUTLINE;
        }
    }

    private static boolean shouldRemoveAutomatically(IssuesFixConfig config, int outlineColor) {
        if (!config.removeWhitePlayerOutlinesAutomatically) {
            return false;
        }

        int red = outlineColor >> 16 & 255;
        int green = outlineColor >> 8 & 255;
        int blue = outlineColor & 255;
        return red >= 220 && green >= 220 && blue >= 220;
    }
}
