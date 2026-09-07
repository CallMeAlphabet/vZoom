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

package com.vzoom.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
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

/**
 * vZoom client entrypoint for the modern (Mojmap, MC 26.2) target.
 *
 * <p>Besides the main zoom key it registers a handful of auxiliary keybinds
 * (step in / step out / reset / reload config) processed once per
 * client tick.</p>
 */
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
                InputConstants.Type.KEYSYM,
                cfg.defaultKey,
                KeyMapping.Category.MISC
        ));
        ZOOM_IN_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vzoom.zoomIn", InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));
        ZOOM_OUT_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vzoom.zoomOut", InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));
        ZOOM_RESET_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vzoom.reset", InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));
        RELOAD_CONFIG_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vzoom.reload", InputConstants.Type.KEYSYM,
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

    /** Raw "zoom key currently held down" check (player must be in-world and no screen open). */
    public static boolean isZoomHeld() {
        if (ZOOM_KEY == null) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return false;
        if (mc.screen != null) return false;
        Window window = mc.getWindow();
        int code = KeyMappingHelper.getBoundKeyOf(ZOOM_KEY).getValue();
        return InputConstants.isKeyDown(window, code);
    }

    /** Whether the player's current state permits zooming (auto-disable rules). */
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

    /** Legacy alias — treated as the raw held state. Prefer {@link ZoomState#isActive()}. */
    public static boolean isZoomActive() {
        return isZoomHeld();
    }
}
