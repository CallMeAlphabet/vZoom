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
import com.vzoom.client.gui.ZoomHud;
import com.vzoom.core.ZoomConfig;
import com.vzoom.core.ZoomState;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod("vzoom")
public class VZoomClient {
    public static final String MOD_ID = "vzoom";

    public static KeyMapping ZOOM_KEY;
    public static KeyMapping ZOOM_IN_KEY;
    public static KeyMapping ZOOM_OUT_KEY;
    public static KeyMapping ZOOM_RESET_KEY;
    public static KeyMapping RELOAD_CONFIG_KEY;

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent e) {
            ZoomConfig cfg = ZoomState.INSTANCE.config;
            ZOOM_KEY = reg(e, "key.vzoom.zoom", cfg.defaultKey);
            ZOOM_IN_KEY = reg(e, "key.vzoom.zoomIn", InputConstants.UNKNOWN.getValue());
            ZOOM_OUT_KEY = reg(e, "key.vzoom.zoomOut", InputConstants.UNKNOWN.getValue());
            ZOOM_RESET_KEY = reg(e, "key.vzoom.reset", InputConstants.UNKNOWN.getValue());
            RELOAD_CONFIG_KEY = reg(e, "key.vzoom.reload", InputConstants.UNKNOWN.getValue());
        }

        @SubscribeEvent
        public static void onRegisterLayers(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent e) {
            e.registerAboveAll(Identifier.fromNamespaceAndPath(MOD_ID, "zoom_hud"), (gg, dt) -> new ZoomHud().extractRenderState(gg, dt));
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class GameBus {
        @SubscribeEvent
        public static void onTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e) {
            handleAuxKeys();
        }
    }

    private static KeyMapping reg(RegisterKeyMappingsEvent e, String name, int defaultKey) {
        KeyMapping km = new KeyMapping(name, InputConstants.Type.KEYSYM, defaultKey, KeyMapping.Category.MISC);
        e.register(km);
        return km;
    }

    private static void handleAuxKeys() {
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
        if (mc.gui.screen() != null) return false;
        int code = ((com.vzoom.client.mixin.KeyMappingAccessor) (Object) ZOOM_KEY).vzoom$getBoundKey().getValue();
        return InputConstants.isKeyDown(mc.getWindow(), code);
    }

    public static boolean isStateZoomable() {
        ZoomConfig c = ZoomState.INSTANCE.config;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) return true;
        if (c.disableWhileSprinting && p.isSprinting()) return false;
        if (c.disableWhileFlying && (p.getAbilities().flying || p.isFallFlying())) return false;
        if (c.disableWhileUsingItem && p.isUsingItem()) return false;
        if (c.disableWhileUnderwater && p.isInWater()) return false;
        return true;
    }

}
