/*
 * Copyright 2026 Vextoly, ItzAlphabet
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
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
            if (s.config.cinematicCamera) {
                mult *= s.config.cinematicSensitivity;
            }
            this.accumulatedDX *= mult;
            this.accumulatedDY *= mult;
        }
    }
}
