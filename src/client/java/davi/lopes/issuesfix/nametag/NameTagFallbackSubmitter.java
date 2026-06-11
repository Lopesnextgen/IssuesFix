package davi.lopes.issuesfix.nametag;

import com.mojang.blaze3d.vertex.PoseStack;
import davi.lopes.issuesfix.config.ConfigManager;
import davi.lopes.issuesfix.debug.IssuesFixDebug;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class NameTagFallbackSubmitter {
    private static final float LINE_STEP = 9.0F * 1.15F * 0.025F;

    private NameTagFallbackSubmitter() {
    }

    public static boolean submit(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState, String source) {
        if (ConfigManager.config().clearAllNametags) {
            return false;
        }

        NameTagSubmitState submitState = (NameTagSubmitState) state;
        if (!submitState.issuesfix$shouldSubmitFallbackNameTag()) {
            IssuesFixDebug.logSubmit(state, submitState, source + "-no-fallback");
            return false;
        }

        poseStack.pushPose();
        if (state.scoreText != null) {
            poseStack.translate(0.0F, LINE_STEP, 0.0F);
        }

        List<Component> lines = submitState.issuesfix$getFallbackNameTagLines();
        for (int index = lines.size() - 1; index >= 0; index--) {
            Component line = lines.get(index);
            if (line == null || line.getString().isBlank()) {
                continue;
            }

            collector.submitNameTag(
                poseStack,
                submitState.issuesfix$getFallbackNameTagAttachment(),
                state.showExtraEars ? -10 : 0,
                line,
                !state.isDiscrete,
                state.lightCoords,
                state.distanceToCameraSq,
                cameraRenderState
            );

            if (index > 0) {
                poseStack.translate(0.0F, LINE_STEP, 0.0F);
            }
        }

        poseStack.popPose();
        IssuesFixDebug.logSubmit(state, submitState, source + "-submitted");
        submitState.issuesfix$clearFallbackNameTag();
        return true;
    }
}
