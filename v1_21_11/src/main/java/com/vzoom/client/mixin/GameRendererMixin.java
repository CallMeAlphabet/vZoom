package com.vzoom.client.mixin;

import com.vzoom.core.ZoomState;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks the per-frame FOV calculation. vZoom divides the vanilla FOV by the
 * interpolated zoom factor (vanillaFov / currentZoom). Pure read of engine
 * state; activation and the per-frame advance happen in the HUD callback.
 *
 * <p>In 1.21.x getFov returns a {@code float}.</p>
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void vzoom$modifyFov(CallbackInfoReturnable<Float> cir) {
        ZoomState s = ZoomState.INSTANCE;
        cir.setReturnValue((float) s.computeFov(cir.getReturnValue()));
    }
}
