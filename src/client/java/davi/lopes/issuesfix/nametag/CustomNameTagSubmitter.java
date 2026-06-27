package davi.lopes.issuesfix.nametag;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;

public final class CustomNameTagSubmitter {
    private CustomNameTagSubmitter() {
    }

    public static boolean submit(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState, String source) {
        NameTagSubmitState submitState = (NameTagSubmitState) state;
        if (!submitState.issuesfix$shouldSubmitCustomNameTag()) {
            return false;
        }

        Component component = submitState.issuesfix$getCustomNameTag();
        if (component == null || component.getString().isBlank()) {
            submitState.issuesfix$clearCustomNameTag();
            return false;
        }

        NameTagRenderGate.markCustom(component);
        NameTagRenderGate.runAllowed(() -> collector.submitNameTag(
            poseStack,
            submitState.issuesfix$getCustomNameTagAttachment(),
            state.showExtraEars ? -10 : 0,
            component,
            false,
            state.lightCoords,
            state.distanceToCameraSq,
            cameraRenderState
        ));

        submitState.issuesfix$clearCustomNameTag();
        return true;
    }
}
