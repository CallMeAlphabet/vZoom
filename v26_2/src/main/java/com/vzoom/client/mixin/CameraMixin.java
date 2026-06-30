package com.vzoom.client.mixin;

import com.vzoom.core.ZoomState;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks the per-frame FOV calculation (MC 26.x exposes it on {@link Camera}).
 * vZoom divides the vanilla FOV by the interpolated zoom factor. Pure read of
 * engine state; activation and the per-frame advance happen in the HUD callback.
 */
@Mixin(Camera.class)
public class CameraMixin {

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void vzoom$modifyFov(CallbackInfoReturnable<Float> cir) {
        ZoomState s = ZoomState.INSTANCE;
        cir.setReturnValue((float) s.computeFov(cir.getReturnValue()));
    }
}
