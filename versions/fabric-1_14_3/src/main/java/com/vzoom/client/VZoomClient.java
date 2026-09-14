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
package com.vzoom.client;

import com.vzoom.client.config.VZoomConfigIo;
import com.vzoom.core.ZoomConfig;
import com.vzoom.core.ZoomState;
import net.fabricmc.api.ClientModInitializer;
import com.vzoom.client.mixin.KeyMappingAccessor;
import com.vzoom.client.mixin.KeyMappingMapsAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class VZoomClient implements ClientModInitializer {
    public static final String MOD_ID = "vzoom";

    public static KeyMapping ZOOM_KEY;
    public static KeyMapping ZOOM_IN_KEY;
    public static KeyMapping ZOOM_OUT_KEY;
    public static KeyMapping ZOOM_RESET_KEY;
    public static KeyMapping RELOAD_CONFIG_KEY;

    private static KeyMapping key(String name, int defaultKey) {
        KeyMapping km = new KeyMapping(name, InputConstants.Type.KEYSYM, defaultKey, "key.categories.vzoom");
        KeyMappingMapsAccessor.vzoom$byId().put(name, km);
        KeyMappingMapsAccessor.vzoom$byCode().put(InputConstants.Type.KEYSYM.getOrCreate(defaultKey), km);
        return km;
    }

    @Override
    public void onInitializeClient() {
        VZoomConfigIo.load();
        ZoomConfig cfg = ZoomState.INSTANCE.config;

        ZOOM_KEY = key("zoom", cfg.defaultKey);
        ZOOM_IN_KEY = key("zoomIn", com.mojang.blaze3d.platform.InputConstants.UNKNOWN.getValue());
        ZOOM_OUT_KEY = key("zoomOut", com.mojang.blaze3d.platform.InputConstants.UNKNOWN.getValue());
        ZOOM_RESET_KEY = key("reset", com.mojang.blaze3d.platform.InputConstants.UNKNOWN.getValue());
        RELOAD_CONFIG_KEY = key("reload", com.mojang.blaze3d.platform.InputConstants.UNKNOWN.getValue());

    }

    public static void handleAuxKeys() {
        ZoomState s = ZoomState.INSTANCE;
        if (ZOOM_IN_KEY != null) { while (ZOOM_IN_KEY.consumeClick()) s.stepZoom(+1); }
        if (ZOOM_OUT_KEY != null) { while (ZOOM_OUT_KEY.consumeClick()) s.stepZoom(-1); }
        if (ZOOM_RESET_KEY != null && ZOOM_RESET_KEY.consumeClick()) s.resetZoom();
        if (RELOAD_CONFIG_KEY != null && RELOAD_CONFIG_KEY.consumeClick()) VZoomConfigIo.load();
    }

    public static boolean isZoomHeld() {
        if (ZOOM_KEY == null) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return false;
        if (mc.screen != null) return false;
        int code = ((KeyMappingAccessor) (Object) ZOOM_KEY).vzoom$getBoundKey().getValue();
        return com.mojang.blaze3d.platform.InputConstants.isKeyDown(mc.window.getWindow(), code);
    }

    public static boolean isStateZoomable() {
        ZoomConfig c = ZoomState.INSTANCE.config;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) return true;
        if (c.disableWhileSprinting && p.isSprinting()) return false;
        if (c.disableWhileFlying && (p.abilities.flying || p.isFallFlying())) return false;
        if (c.disableWhileUsingItem && p.isUsingItem()) return false;
        if (c.disableWhileUnderwater && p.isInWater()) return false;
        return true;
    }

}
