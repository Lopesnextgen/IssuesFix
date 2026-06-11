package davi.lopes.issuesfix.nametag;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface NameTagSubmitState {
    void issuesfix$setFallbackNameTag(Component component, Vec3 attachment, boolean enabled);

    void issuesfix$setFallbackNameTagLines(List<Component> components, Vec3 attachment, boolean enabled);

    Component issuesfix$getFallbackNameTag();

    List<Component> issuesfix$getFallbackNameTagLines();

    Vec3 issuesfix$getFallbackNameTagAttachment();

    boolean issuesfix$shouldSubmitFallbackNameTag();

    void issuesfix$clearFallbackNameTag();

    void issuesfix$setCustomNameTag(Component component, Vec3 attachment, boolean enabled);

    Component issuesfix$getCustomNameTag();

    Vec3 issuesfix$getCustomNameTagAttachment();

    boolean issuesfix$shouldSubmitCustomNameTag();

    void issuesfix$clearCustomNameTag();
}
