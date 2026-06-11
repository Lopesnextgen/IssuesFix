package davi.lopes.issuesfix.mixin;

import davi.lopes.issuesfix.lunar.LunarNameTagCleaner;
import davi.lopes.issuesfix.lunar.LunarNameTagTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "com.moonsworth.lunar.client.RICRCIRCHHOHOICOHIIHHCHIHRICCI.OOHOHRICOORCHHCRCHHRRCRORHICRH.RICRCIRCHHOHOICOHIIHHCHIHRICCI.OOOOCRCIRHRICRIOHHROIRCHHIHIHC", remap = false)
public abstract class LunarNameTagEventMixin {
    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void issuesfix$clearConstructedEvent(CallbackInfo callbackInfo) {
        LunarNameTagCleaner.clear(this);
    }

    @Inject(method = "getLines()Ljava/util/List;", at = @At("RETURN"), remap = false)
    private void issuesfix$observeLines(CallbackInfoReturnable<List<?>> callbackInfo) {
        LunarNameTagCleaner.clear(this, callbackInfo.getReturnValue());
        if (LunarNameTagCleaner.enabled()) {
            return;
        }

        LunarNameTagTracker.observe(this, callbackInfo.getReturnValue());
    }

    @Inject(method = "CICICOICRHRHORICOCHHROCCOCCCOR()Lnet/kyori/adventure/text/Component;", at = @At("RETURN"), cancellable = true, remap = false)
    private void issuesfix$clearCurrentLine(CallbackInfoReturnable<Object> callbackInfo) {
        if (LunarNameTagCleaner.enabled()) {
            callbackInfo.setReturnValue(LunarNameTagCleaner.clearComponent(callbackInfo.getReturnValue()));
        }
    }
}
