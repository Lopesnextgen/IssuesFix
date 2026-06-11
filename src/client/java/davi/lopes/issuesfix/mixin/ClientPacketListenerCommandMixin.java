package davi.lopes.issuesfix.mixin;

import davi.lopes.issuesfix.command.IssuesFixCommand;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerCommandMixin {
    @Inject(method = "sendCommand(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void issuesfix$showStatus(String command, CallbackInfo callbackInfo) {
        if (command == null) {
            return;
        }

        String normalized = command.trim();
        if (!normalized.equalsIgnoreCase("issuefix") && !normalized.equalsIgnoreCase("issuesfix")) {
            return;
        }

        callbackInfo.cancel();
        IssuesFixCommand.execute();
    }
}
