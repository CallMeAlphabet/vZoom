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

import com.mojang.blaze3d.vertex.PoseStack;
import com.vzoom.core.HudStyle;
import com.vzoom.core.ZoomState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

public final class ZoomHud {
    private static final int MARGIN = 6;
    private static final int PAD = 3;
    private static final int INFO_COLOR = 0xFFCCCCCC;

    public static void render(GuiGraphics graphics, DeltaTracker tickDelta) {
        ZoomState s = ZoomState.INSTANCE;
        // State ticking lives in the FOV mixin (runs even with the HUD
        // hidden via F1); this method only draws.
        Minecraft mc = Minecraft.getInstance();

        if (!s.shouldShowHud()) {
            return;
        }
        renderIndicator(graphics, mc, s);
    }

    private static void renderIndicator(GuiGraphics g, Minecraft mc, ZoomState s) {
        Font font = mc.font;
        int scale = Math.max(1, (int) Math.round(s.config.hudScale));

        List<String> lines = new ArrayList<String>();
        String main;
        if (s.config.hudStyle == HudStyle.PERCENT) {
            main = s.getZoomPercent() + "%";
        } else {
            main = String.format("x%.2f", s.getCurrentZoom());
        }
        lines.add(main);

        if (s.config.showFps) {
            lines.add("FPS: " + mc.getFps());
        }
        HitResult hit = null;
        if (s.config.showCoordinates || s.config.showTargetInfo) {
            hit = computeHit(mc, s);
        }
        if (s.config.showCoordinates) {
            String c = coordLine(mc, hit);
            if (c != null) lines.add(c);
        }
        if (s.config.showDirection && mc.player != null) {
            lines.add("Facing: " + facing(mc.player.getYRot()));
        }
        if (s.config.showTargetInfo) {
            String t = targetLine(mc, hit);
            lines.add(t == null ? "\u2014" : t);
        }

        boolean wantsBar = s.config.hudStyle == HudStyle.BAR;
        int barH = wantsBar ? (4 + PAD) : 0;

        int textW = 0;
        for (String l : lines) textW = Math.max(textW, font.width(l));
        int rawW = textW + PAD * 2;
        int rawH = lines.size() * font.lineHeight + PAD * 2 + barH;

        int sw = rawW * scale;
        int sh = rawH * scale;
        int screenW = g.guiWidth();
        int screenH = g.guiHeight();

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

        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, scale);

        if (s.config.hudBackground) {
            g.fill(-1, -1, rawW + 1, rawH + 1, s.config.hudBackgroundColor);
        }
        for (int i = 0; i < lines.size(); i++) {
            int color = i == 0 ? s.config.hudColor : INFO_COLOR;
            g.drawString(font, lines.get(i), PAD, PAD + i * font.lineHeight, color, true);
        }

        if (wantsBar) {
            int barY = PAD + lines.size() * font.lineHeight + PAD;
            int barFullW = rawW - PAD * 2;
            g.fill(PAD, barY, PAD + barFullW, barY + 3, 0x55000000);
            int fillW = (int) Math.round(barFullW * s.getZoomBarProgress());
            g.fill(PAD, barY, PAD + fillW, barY + 3, s.config.hudColor);
        }

        pose.popPose();
    }

    private static HitResult computeHit(Minecraft mc, ZoomState s) {
        Entity cam = mc.getCameraEntity();
        if (cam == null || mc.level == null) return null;
        double dist = Math.min(256.0, 11.0 + s.getCurrentZoom() * 5.0);
        return cam.pick(dist, 0.0F, false);
    }

    private static String coordLine(Minecraft mc, HitResult hit) {
        if (mc.level == null) return null;
        if (hit == null || hit.getType() == HitResult.Type.MISS) return null;
        BlockPos bp;
        if (hit.getType() == HitResult.Type.ENTITY && hit instanceof EntityHitResult) {
            bp = ((EntityHitResult) hit).getEntity().blockPosition();
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

    private static String targetLine(Minecraft mc, HitResult hit) {
        if (hit == null || hit.getType() == HitResult.Type.MISS) return null;
        Entity cam = mc.getCameraEntity();
        if (cam == null) return null;
        double d = cam.getEyePosition(1.0F).distanceTo(hit.getLocation());
        if (hit.getType() == HitResult.Type.ENTITY && hit instanceof EntityHitResult) {
            Entity e = ((EntityHitResult) hit).getEntity();
            String name = e.getDisplayName().getString();
            if (e instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) e;
                return name + String.format(" \u00b7 %.1f/%.0f HP",
                        living.getHealth(), living.getMaxHealth()) + " (" + fmtDist(d) + ")";
            }
            return name + " (" + fmtDist(d) + ")";
        } else if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult) {
            BlockPos bp = ((BlockHitResult) hit).getBlockPos();
            BlockState state = mc.level.getBlockState(bp);
            return state.getBlock().getName().getString() + " (" + fmtDist(d) + ")";
        }
        return null;
    }

    private static String fmtDist(double d) {
        if (d >= 10.0) return String.format("%.0fm", d);
        return String.format("%.1fm", d);
    }
}
