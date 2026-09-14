/*
 * Copyright 2026 CallMeAlphabet (ItzAlphabet)
 * Copyright 2026 Vextoly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
