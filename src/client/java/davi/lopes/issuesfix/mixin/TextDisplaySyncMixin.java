package davi.lopes.issuesfix.mixin;

import davi.lopes.issuesfix.nametag.PlayerNameLabelMatcher;
import davi.lopes.issuesfix.nametag.ServerNameTagHealthCache;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(Display.TextDisplay.class)
public abstract class TextDisplaySyncMixin {
    @Unique
    private static volatile EntityDataAccessor<Component> issuesfix$textAccessor;

    @Inject(method = "onSyncedDataUpdated(Lnet/minecraft/network/syncher/EntityDataAccessor;)V", at = @At("HEAD"))
    private void issuesfix$captureTextUpdate(EntityDataAccessor<?> accessor, CallbackInfo callbackInfo) {
        EntityDataAccessor<Component> textAccessor = resolveTextAccessor();
        if (textAccessor == null || !textAccessor.equals(accessor)) {
            return;
        }

        Display.TextDisplay self = (Display.TextDisplay) (Object) this;
        SynchedEntityData data = self.getEntityData();
        if (data == null) {
            return;
        }

        Component text;
        try {
            text = data.get(textAccessor);
        } catch (Throwable ignored) {
            return;
        }
        if (text == null) {
            return;
        }

        Player player = PlayerNameLabelMatcher.matchingByName(text);
        if (player == null) {
            player = PlayerNameLabelMatcher.matchingPlayerNear(text, self.getX(), self.getY(), self.getZ());
        }
        if (player == null) {
            return;
        }

        ServerNameTagHealthCache.capture(player, text);
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static EntityDataAccessor<Component> resolveTextAccessor() {
        EntityDataAccessor<Component> cached = issuesfix$textAccessor;
        if (cached != null) {
            return cached;
        }
        try {
            Field field = Display.TextDisplay.class.getDeclaredField("DATA_TEXT_ID");
            field.setAccessible(true);
            cached = (EntityDataAccessor<Component>) field.get(null);
            issuesfix$textAccessor = cached;
            return cached;
        } catch (Throwable throwable) {
            return null;
        }
    }
}