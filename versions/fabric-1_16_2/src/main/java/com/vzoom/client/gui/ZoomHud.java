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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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
        if (fpsWindowStart == 0L) {
            fpsWindowStart = now;
        }
        long elapsed = now - fpsWindowStart;
        if (elapsed >= 500_000_000L) {
            fps = (int) (frames * 1_000_000_000L / elapsed);
            frames = 0;
            fpsWindowStart = now;
        }
        return fps;
    }

    public static void render(PoseStack poseStack, float tickDelta) {
        currentFps();
        ZoomState s = ZoomState.INSTANCE;
        // State ticking lives in GameRendererMixin (runs even with the HUD
        // hidden via F1); this method only draws.

        Minecraft mc = Minecraft.getInstance();

        if (!s.shouldShowHud()) {
            return;
        }
        renderIndicator(poseStack, mc, s);
    }

    private static void renderIndicator(PoseStack pose, Minecraft mc, ZoomState s) {
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
            lines.add("FPS: " + currentFps());
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
            lines.add("Facing: " + facing(mc.player.yRot));
        }
        if (s.config.showTargetInfo) {
            String t = targetLine(mc, hit);
            lines.add(t == null ? "\u2014" : t);
        }

        int textW = 0;
        for (String l : lines) textW = Math.max(textW, font.width(l));
        int rawW = textW + PAD * 2;
        int rawH = lines.size() * font.lineHeight + PAD * 2;

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

        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, scale);

        if (s.config.hudBackground) {
            GuiComponent.fill(pose, -1, -1, rawW + 1, rawH + 1, s.config.hudBackgroundColor);
        }
        for (int i = 0; i < lines.size(); i++) {
            int color = i == 0 ? s.config.hudColor : INFO_COLOR;
            font.draw(pose, lines.get(i), PAD, PAD + i * font.lineHeight, color);
        }


        pose.popPose();
    }

    private static HitResult computeHit(Minecraft mc, ZoomState s) {
        Entity cam = mc.getCameraEntity();
        if (cam == null || mc.level == null) return null;
        double dist = Math.min(256.0, 11.0 + s.getCurrentZoom() * 5.0);
        Vec3 start = cam.getEyePosition(1.0F);
        Vec3 direction = cam.getViewVector(1.0F).scale(dist);
        Vec3 end = start.add(direction);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                cam, start, end, cam.getBoundingBox().expandTowards(direction).inflate(1.0D),
                entity -> !entity.isSpectator() && entity.isPickable(), dist);
        HitResult blockHit = cam.pick(dist, 0.0F, false);
        if (entityHit == null) return blockHit;
        if (blockHit.getType() != HitResult.Type.MISS
                && start.distanceToSqr(blockHit.getLocation()) < start.distanceToSqr(entityHit.getLocation())) return blockHit;
        return entityHit;
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
        if (!(hit instanceof EntityHitResult)) return null;
        Entity cam = mc.getCameraEntity();
        if (cam == null) return null;
        double d = cam.getEyePosition(1.0F).distanceTo(hit.getLocation());
        Entity e = ((EntityHitResult) hit).getEntity();
        String name = e.getDisplayName().getString();
        if (e instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) e;
            return name + String.format(" \u00b7 %.1f/%.0f HP", living.getHealth(), living.getMaxHealth()) + " (" + fmtDist(d) + ")";
        }
        return name + " (" + fmtDist(d) + ")";
    }

    private static String fmtDist(double d) {
        if (d >= 10.0) return String.format("%.0fm", d);
        return String.format("%.1fm", d);
    }
}
