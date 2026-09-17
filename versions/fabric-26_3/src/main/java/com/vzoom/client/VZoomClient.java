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

import com.mojang.blaze3d.platform.InputConstants;
import com.vzoom.client.config.VZoomConfigIo;
import com.vzoom.client.gui.ZoomHud;
import com.vzoom.core.ZoomConfig;
import com.vzoom.core.ZoomState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;

public class VZoomClient implements ClientModInitializer {
    public static final String MOD_ID = "vzoom";

    public static KeyMapping ZOOM_KEY;
    public static KeyMapping ZOOM_IN_KEY;
    public static KeyMapping ZOOM_OUT_KEY;
    public static KeyMapping ZOOM_RESET_KEY;
    public static KeyMapping RELOAD_CONFIG_KEY;

    @Override
    public void onInitializeClient() {
        VZoomConfigIo.load();
        ZoomConfig cfg = ZoomState.INSTANCE.config;

        ZOOM_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vzoom.zoom",
                InputConstants.Type.KEYBOARD,
                cfg.defaultKey,
                KeyMapping.Category.MISC
        ));
        ZOOM_IN_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vzoom.zoomIn", InputConstants.Type.KEYBOARD,
                InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));
        ZOOM_OUT_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vzoom.zoomOut", InputConstants.Type.KEYBOARD,
                InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));
        ZOOM_RESET_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vzoom.reset", InputConstants.Type.KEYBOARD,
                InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));
        RELOAD_CONFIG_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vzoom.reload", InputConstants.Type.KEYBOARD,
                InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "zoom_hud"), new ZoomHud());

        ClientTickEvents.END_CLIENT_TICK.register(client -> handleAuxKeys());
    }

    private static void handleAuxKeys() {
        ZoomState s = ZoomState.INSTANCE;
        if (ZOOM_IN_KEY != null) {
            while (ZOOM_IN_KEY.consumeClick()) s.stepZoom(+1);
        }
        if (ZOOM_OUT_KEY != null) {
            while (ZOOM_OUT_KEY.consumeClick()) s.stepZoom(-1);
        }
        if (ZOOM_RESET_KEY != null && ZOOM_RESET_KEY.consumeClick()) {
            s.resetZoom();
        }
        if (RELOAD_CONFIG_KEY != null && RELOAD_CONFIG_KEY.consumeClick()) {
            VZoomConfigIo.load();
        }
    }

    public static boolean isZoomHeld() {
        if (ZOOM_KEY == null) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return false;
        if (mc.gui.screen() != null) return false;
        int code = KeyMappingHelper.getBoundKeyOf(ZOOM_KEY).getValue();
        return InputConstants.isKeyDown(code);
    }

    public static boolean isStateZoomable() {
        ZoomConfig c = ZoomState.INSTANCE.config;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) return true;
        if (c.disableWhileSprinting && p.isSprinting()) return false;
        if (c.disableWhileFlying && (p.getAbilities().flying || p.isFallFlying())) return false;
        if (c.disableWhileUsingItem && p.isUsingItem()) return false;
        if (c.disableWhileUnderwater && p.isUnderWater()) return false;
        return true;
    }

}
