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
package com.vzoom.client.gui;

import com.vzoom.core.HudStyle;
import com.vzoom.core.ZoomState;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class ZoomHud {
    private static final int MARGIN = 6;
    private static final int PAD = 3;
    private static final int INFO_COLOR = 0xFFCCCCCC;

    private static int frames;
    private static long fpsWindowStart;
    private static int fps;

    private static int currentFps() {
        frames++;
        long now = System.nanoTime();
        if (fpsWindowStart == 0L) fpsWindowStart = now;
        long elapsed = now - fpsWindowStart;
        if (elapsed >= 500_000_000L) {
            fps = (int) (frames * 1_000_000_000L / elapsed);
            frames = 0;
            fpsWindowStart = now;
        }
        return fps;
    }

    public static void render(float tickDelta) {
        ZoomState s = ZoomState.INSTANCE;

        net.minecraft.client.Minecraft mc = Minecraft.getInstance();

        if (!s.shouldShowHud()) return;
        renderIndicator(mc, s);
    }

    private static void renderIndicator(net.minecraft.client.Minecraft mc, ZoomState s) {
        net.minecraft.client.gui.Font font = mc.font;
        int scale = Math.max(1, (int) Math.round(s.config.hudScale));

        List<String> lines = new ArrayList<String>();
        String main;
        if (s.config.hudStyle == HudStyle.PERCENT) {
            main = s.getZoomPercent() + "%";
        } else {
            main = String.format("x%.2f", s.getCurrentZoom());
        }
        lines.add(main);

        if (s.config.showFps) lines.add("FPS: " + currentFps());

        HitResult hit = null;
        if (s.config.showCoordinates || s.config.showTargetInfo) hit = computeHit(mc, s);
        if (s.config.showCoordinates) {
            String c = coordLine(mc, hit);
            if (c != null) lines.add(c);
        }
        if (s.config.showDirection && mc.player != null) {
            lines.add("Facing: " + facing(mc.player.yRot));
        }
        if (s.config.showTargetInfo) {
            String t = targetLine(mc, hit);
            lines.add(t == null ? "\u2014" : t);
        }

        boolean wantsBar = s.config.hudStyle == HudStyle.BAR;
        int barH = wantsBar ? (4 + PAD) : 0;

        int textW = 0;
        for (String l : lines) textW = Math.max(textW, font.width(l));
        int rawW = textW + PAD * 2 + 4;
        int rawH = lines.size() * font.lineHeight + PAD * 2 + barH;

        int sw = rawW * scale;
        int sh = rawH * scale;
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int x = MARGIN;
        int y = MARGIN;
        switch (s.config.hudAlign) {
            default:
            case TOP_LEFT:      x = MARGIN; y = MARGIN; break;
            case TOP_CENTER:    x = (screenW - sw) / 2; y = MARGIN; break;
            case TOP_RIGHT:     x = screenW - sw - MARGIN; y = MARGIN; break;
            case BOTTOM_LEFT:   x = MARGIN; y = screenH - sh - MARGIN; break;
            case BOTTOM_CENTER: x = (screenW - sw) / 2; y = screenH - sh - MARGIN; break;
            case BOTTOM_RIGHT:  x = screenW - sw - MARGIN; y = screenH - sh - MARGIN; break;
            case CENTER:        x = (screenW - sw) / 2; y = (screenH - sh) / 2; break;
        }
        x += s.config.hudOffsetX;
        y += s.config.hudOffsetY;

        GlStateManager._pushMatrix();
        GlStateManager._translatef(x, y, 0);
        GlStateManager._scalef(scale, scale, scale);

        if (s.config.hudBackground) net.minecraft.client.gui.GuiComponent.fill(x - 1, y - 1, x + rawW + 1, y + rawH + 1, s.config.hudBackgroundColor);
        int ty = y + 2;
        for (int i = 0; i < lines.size(); i++) {
            int color = i == 0 ? s.config.hudColor : INFO_COLOR;
            font.drawShadow(lines.get(i), PAD + 2, y + 2 + i * font.lineHeight, color);
        }

        if (wantsBar) {
            int barY = y + 2 + lines.size() * font.lineHeight + PAD;
            int barFullW = rawW - PAD * 2;
            net.minecraft.client.gui.GuiComponent.fill(x + PAD, barY, x + PAD + barFullW, barY + 3, 0x55000000);
            int fillW = (int) Math.round(barFullW * s.getZoomBarProgress());
            net.minecraft.client.gui.GuiComponent.fill(x + PAD, barY, x + PAD + fillW, barY + 3, s.config.hudColor);
        }

        GlStateManager._popMatrix();
    }

    private static HitResult computeHit(net.minecraft.client.Minecraft mc, ZoomState s) {
        Entity cam = mc.getCameraEntity();
        if (cam == null || mc.level == null) return null;
        double dist = Math.min(256.0, 11.0 + s.getCurrentZoom() * 5.0);
        return cam.pick(dist, 0.0F, false);
    }

    private static String coordLine(net.minecraft.client.Minecraft mc, HitResult hit) {
        if (mc.level == null) return null;
        if (hit == null || hit.getType() == HitResult.Type.MISS) return null;
        BlockPos bp;
        if (hit.getType() == HitResult.Type.ENTITY && hit instanceof EntityHitResult) {
            bp = ((EntityHitResult) hit).getEntity().getCommandSenderBlockPosition();
        } else if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult) {
            bp = ((BlockHitResult) hit).getBlockPos();
        } else {
            return null;
        }
        return String.format("XYZ: %d / %d / %d", bp.getX(), bp.getY(), bp.getZ());
    }

    private static String facing(float yaw) {
        String[] dirs = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        int idx = Math.floorMod(Math.round(yaw / 45f), 8);
        return dirs[idx];
    }

    private static String targetLine(net.minecraft.client.Minecraft mc, HitResult hit) {
        if (hit == null || hit.getType() == HitResult.Type.MISS) return null;
        if (hit instanceof EntityHitResult) {
            Entity e = ((EntityHitResult) hit).getEntity();
            String name = e.getName().getString();
            if (e instanceof net.minecraft.world.entity.LivingEntity) {
                net.minecraft.world.entity.LivingEntity living = (net.minecraft.world.entity.LivingEntity) e;
                return name + String.format(" · %.1f/%.0f HP", living.getHealth(), living.getMaxHealth());
            }
            return name;
        }
        if (hit instanceof BlockHitResult) {
            BlockPos bp = ((BlockHitResult) hit).getBlockPos();
            BlockState st = mc.level.getBlockState(bp);
            return st.getBlock().getName().getString() + String.format(" (%d, %d, %d)", bp.getX(), bp.getY(), bp.getZ());
        }
        return null;
    }
}
