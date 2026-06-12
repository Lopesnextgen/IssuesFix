package davi.lopes.issuesfix.mixin;

import davi.lopes.issuesfix.nametag.TextDisplayNameTagState;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TextDisplayEntityRenderState.class)
public abstract class TextDisplayEntityRenderStateMixin implements TextDisplayNameTagState {
    @Unique
    private boolean issuesfix$playerNameTag;

    @Override
    public boolean issuesfix$isPlayerNameTag() {
        return issuesfix$playerNameTag;
    }

    @Override
    public void issuesfix$setPlayerNameTag(boolean playerNameTag) {
        issuesfix$playerNameTag = playerNameTag;
    }
}
