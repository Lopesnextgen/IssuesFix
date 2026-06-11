package davi.lopes.issuesfix.mixin;

import davi.lopes.issuesfix.nametag.NameTagSubmitState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(AvatarRenderState.class)
public abstract class AvatarRenderStateMixin implements NameTagSubmitState {
    @Unique
    private List<Component> issuesfix$fallbackNameTagLines = List.of();

    @Unique
    private Vec3 issuesfix$fallbackNameTagAttachment;

    @Unique
    private boolean issuesfix$submitFallbackNameTag;

    @Unique
    private Component issuesfix$customNameTag;

    @Unique
    private Vec3 issuesfix$customNameTagAttachment;

    @Unique
    private boolean issuesfix$submitCustomNameTag;

    @Override
    public void issuesfix$setFallbackNameTag(Component component, Vec3 attachment, boolean enabled) {
        issuesfix$setFallbackNameTagLines(component == null ? List.of() : List.of(component), attachment, enabled);
    }

    @Override
    public void issuesfix$setFallbackNameTagLines(List<Component> components, Vec3 attachment, boolean enabled) {
        issuesfix$fallbackNameTagLines = components == null ? List.of() : List.copyOf(components);
        issuesfix$fallbackNameTagAttachment = attachment;
        issuesfix$submitFallbackNameTag = enabled;
    }

    @Override
    public Component issuesfix$getFallbackNameTag() {
        return issuesfix$fallbackNameTagLines.isEmpty() ? null : issuesfix$fallbackNameTagLines.get(0);
    }

    @Override
    public List<Component> issuesfix$getFallbackNameTagLines() {
        return issuesfix$fallbackNameTagLines;
    }

    @Override
    public Vec3 issuesfix$getFallbackNameTagAttachment() {
        return issuesfix$fallbackNameTagAttachment;
    }

    @Override
    public boolean issuesfix$shouldSubmitFallbackNameTag() {
        return issuesfix$submitFallbackNameTag && !issuesfix$fallbackNameTagLines.isEmpty() && issuesfix$fallbackNameTagAttachment != null;
    }

    @Override
    public void issuesfix$clearFallbackNameTag() {
        issuesfix$setFallbackNameTag(null, null, false);
    }

    @Override
    public void issuesfix$setCustomNameTag(Component component, Vec3 attachment, boolean enabled) {
        issuesfix$customNameTag = component;
        issuesfix$customNameTagAttachment = attachment;
        issuesfix$submitCustomNameTag = enabled;
    }

    @Override
    public Component issuesfix$getCustomNameTag() {
        return issuesfix$customNameTag;
    }

    @Override
    public Vec3 issuesfix$getCustomNameTagAttachment() {
        return issuesfix$customNameTagAttachment;
    }

    @Override
    public boolean issuesfix$shouldSubmitCustomNameTag() {
        return issuesfix$submitCustomNameTag && issuesfix$customNameTag != null && issuesfix$customNameTagAttachment != null;
    }

    @Override
    public void issuesfix$clearCustomNameTag() {
        issuesfix$setCustomNameTag(null, null, false);
    }
}
