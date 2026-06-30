package com.vzoom.client.mixin;

import com.vzoom.core.ZoomState;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Two responsibilities while the zoom key is engaged:
 * <ul>
 *   <li>Route mouse-scroll deltas to the vZoom engine (geometric/linear/discrete
 *       zoom) and cancel vanilla hotbar switching.</li>
 *   <li>Scale the accumulated mouse-look deltas so deep zoom stays aimable.</li>
 * </ul>
 */
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow
    private double accumulatedDX;

    @Shadow
    private double accumulatedDY;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void vzoom$onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (ZoomState.INSTANCE.shouldCancelScroll()) {
            ZoomState.INSTANCE.onScroll(vertical);
            ci.cancel();
        }
    }

    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void vzoom$scaleSensitivity(CallbackInfo ci) {
        ZoomState s = ZoomState.INSTANCE;
        if (s.isActive() && s.config.reduceSensitivity) {
            double mult = s.getSensitivityScale();
            this.accumulatedDX *= mult;
            this.accumulatedDY *= mult;
        }
    }
}
