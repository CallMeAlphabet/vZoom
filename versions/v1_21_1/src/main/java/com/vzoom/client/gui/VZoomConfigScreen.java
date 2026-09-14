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

package com.vzoom.client.gui;

import com.vzoom.client.config.VZoomConfigIo;
import com.vzoom.core.ActivationMode;
import com.vzoom.core.EasingMode;
import com.vzoom.core.HudAlign;
import com.vzoom.core.HudStyle;
import com.vzoom.core.ScrollMode;
import com.vzoom.core.ZoomConfig;
import com.vzoom.core.ZoomState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class VZoomConfigScreen extends Screen {

    private static final int BTN_W = 204;
    private static final int BTN_H = 20;
    private static final int ROW = 24;
    private static final int VALUE_W = 120;
    private static final int SIDE_W = 22;

    private static final int[] PALETTE = {
            0xFFFFB14E, 0xFFFFFFFF, 0xFFFF5555, 0xFF55FF55, 0xFF5555FF,
            0xFFFFFF55, 0xFF55FFFF, 0xFFFF55FF, 0xFFAAAAAA, 0xFF000000
    };

    private final Screen parent;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int visTop;
    private int visBottom;
    private int logicalY;
    private final java.util.List<Header> headers = new java.util.ArrayList<>();

    public VZoomConfigScreen(Screen parent) {
        super(Component.translatable("vzoom.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        ZoomConfig cfg = ZoomState.INSTANCE.config;
        int cx = this.width / 2;
        visTop = 38;
        visBottom = this.height - 34;
        logicalY = 0;
        headers.clear();

        header("vzoom.section.basic");
        toggleRow("vzoom.config.hud", () -> cfg.hudEnabled, v2 -> cfg.hudEnabled = v2);
        toggleRow("vzoom.config.cinematic", () -> cfg.cinematicCamera, v2 -> { cfg.cinematicCamera = v2; applyCinematic(cfg); });
        toggleRow("vzoom.config.retain", () -> cfg.retainZoomOnRelease, v2 -> cfg.retainZoomOnRelease = v2);
        cycleRow("vzoom.config.activation", ActivationMode.values(), () -> cfg.activationMode, v2 -> cfg.activationMode = v2);
        intStepperRow("vzoom.config.doubleTap", () -> cfg.doubleTapIntervalMs, v2 -> cfg.doubleTapIntervalMs = v2, 10, 80, 1000);
        toggleRow("vzoom.config.reduceSens", () -> cfg.reduceSensitivity, v2 -> cfg.reduceSensitivity = v2);
        toggleRow("vzoom.config.scaleSensWithZoom", () -> cfg.scaleSensitivityWithZoom, v2 -> cfg.scaleSensitivityWithZoom = v2);
        toggleRow("vzoom.config.invert", () -> cfg.invertScroll, v2 -> cfg.invertScroll = v2);

        header("vzoom.section.scroll");
        cycleRow("vzoom.config.scrollMode", ScrollMode.values(), () -> cfg.scrollMode, v2 -> cfg.scrollMode = v2);
        cycleRow("vzoom.config.easing", EasingMode.values(), () -> cfg.easingMode, v2 -> cfg.easingMode = v2);
        stepperRow("vzoom.config.scrollFactor", () -> cfg.scrollFactor, v2 -> cfg.scrollFactor = v2, 0.01, 1.01, 8.0);
        stepperRow("vzoom.config.sensitivity", () -> cfg.sensitivity, v2 -> cfg.sensitivity = v2, 0.1, 0.1, 5.0);
        stepperRow("vzoom.config.smoothing", () -> cfg.smoothing, v2 -> cfg.smoothing = v2, 1.0, 1.0, 60.0);
        stepperRow("vzoom.config.linearStep", () -> cfg.linearStep, v2 -> cfg.linearStep = v2, 0.25, 0.25, 20.0);
        intStepperRow("vzoom.config.discreteLevels", () -> cfg.discreteLevels, v2 -> cfg.discreteLevels = v2, 1, 2, 64);

        header("vzoom.section.range");
        stepperRow("vzoom.config.defaultZoom", () -> cfg.defaultZoom, v2 -> cfg.defaultZoom = v2, 0.5, 1.0, 50.0);
        stepperRow("vzoom.config.minZoom", () -> cfg.minZoom, v2 -> cfg.minZoom = v2, 0.05, 0.05, 1.0);

        header("vzoom.section.look");
        stepperRow("vzoom.config.sensScale", () -> cfg.sensitivityScale, v2 -> cfg.sensitivityScale = v2, 0.05, 0.05, 1.0);
        stepperRow("vzoom.config.sensFloor", () -> cfg.sensitivityScaleFloor, v2 -> cfg.sensitivityScaleFloor = v2, 0.005, 0.005, 1.0);

        header("vzoom.section.hud");
        cycleRow("vzoom.config.hudStyle", HudStyle.values(), () -> cfg.hudStyle, v2 -> cfg.hudStyle = v2);
        cycleRow("vzoom.config.hudPosition", HudAlign.values(), () -> cfg.hudAlign, v2 -> cfg.hudAlign = v2);
        stepperRow("vzoom.config.hudScale", () -> cfg.hudScale, v2 -> cfg.hudScale = v2, 0.25, 0.25, 4.0);
        colorRow("vzoom.config.hudColor", () -> cfg.hudColor, v2 -> cfg.hudColor = v2);
        toggleRow("vzoom.config.hudBackground", () -> cfg.hudBackground, v2 -> cfg.hudBackground = v2);
        toggleRow("vzoom.config.showFps", () -> cfg.showFps, v2 -> cfg.showFps = v2);
        toggleRow("vzoom.config.showCoords", () -> cfg.showCoordinates, v2 -> cfg.showCoordinates = v2);
        toggleRow("vzoom.config.showDirection", () -> cfg.showDirection, v2 -> cfg.showDirection = v2);
        toggleRow("vzoom.config.showTarget", () -> cfg.showTargetInfo, v2 -> cfg.showTargetInfo = v2);
        intStepperRow("vzoom.config.hudOffsetX", () -> cfg.hudOffsetX, v2 -> cfg.hudOffsetX = v2, 1, -500, 500);
        intStepperRow("vzoom.config.hudOffsetY", () -> cfg.hudOffsetY, v2 -> cfg.hudOffsetY = v2, 1, -500, 500);

        header("vzoom.section.autoDisable");
        toggleRow("vzoom.config.disableSprint", () -> cfg.disableWhileSprinting, v2 -> cfg.disableWhileSprinting = v2);
        toggleRow("vzoom.config.disableFly", () -> cfg.disableWhileFlying, v2 -> cfg.disableWhileFlying = v2);
        toggleRow("vzoom.config.disableUse", () -> cfg.disableWhileUsingItem, v2 -> cfg.disableWhileUsingItem = v2);
        toggleRow("vzoom.config.disableWater", () -> cfg.disableWhileUnderwater, v2 -> cfg.disableWhileUnderwater = v2);

        maxScroll = Math.max(0, logicalY - (visBottom - visTop));
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        int bottomY = this.height - 26;
        addRenderableWidget(Button.builder(Component.translatable("vzoom.config.reset"), b -> {
            cfg.copyFrom(new ZoomConfig());
            applyCinematic(cfg);
            VZoomConfigIo.save();
            scrollOffset = 0;
            rebuildWidgets();
        }).bounds(cx - BTN_W - 4, bottomY, BTN_W, BTN_H).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(cx + 4, bottomY, BTN_W, BTN_H).build());
    }

    private int screenY() {
        return visTop + logicalY - scrollOffset;
    }

    private boolean inBand(int sy) {
        return sy + BTN_H > visTop && sy + BTN_H <= visBottom && sy < visBottom;
    }

    private void header(String key) {
        headers.add(new Header(translate(key), logicalY));
        logicalY += ROW;
    }

    private void toggleRow(String key, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        int sy = screenY();
        if (inBand(sy)) {
            Button btn = Button.builder(toggleLabel(key, getter.get()), b -> {
                setter.accept(!getter.get());
                b.setMessage(toggleLabel(key, getter.get()));
                VZoomConfigIo.save();
            }).bounds(this.width / 2 - BTN_W / 2, sy, BTN_W, BTN_H).build();
            addRenderableWidget(btn);
        }
        logicalY += ROW;
    }

    private <T extends Enum<T>> void cycleRow(String key, T[] values, Supplier<T> getter, Consumer<T> setter) {
        int sy = screenY();
        if (inBand(sy)) {
            Button btn = Button.builder(cycleLabel(key, getter.get()), b -> {
                T cur = getter.get();
                T next = values[(cur.ordinal() + 1) % values.length];
                setter.accept(next);
                b.setMessage(cycleLabel(key, next));
                VZoomConfigIo.save();
            }).bounds(this.width / 2 - BTN_W / 2, sy, BTN_W, BTN_H).build();
            addRenderableWidget(btn);
        }
        logicalY += ROW;
    }

    private void stepperRow(String key, Supplier<Double> getter, Consumer<Double> setter,
                            double step, double min, double max) {
        int cx = this.width / 2;
        int sy = screenY();
        if (inBand(sy)) {
            Button valueBtn = Button.builder(valueLabel(key, getter.get()), b -> {}).bounds(cx - VALUE_W / 2, sy, VALUE_W, BTN_H).build();
            addRenderableWidget(valueBtn);
            addRenderableWidget(Button.builder(Component.literal("-"), b -> {
                double val = clamp(getter.get() - step, min, max);
                setter.accept(val);
                valueBtn.setMessage(valueLabel(key, val));
                VZoomConfigIo.save();
            }).bounds(cx - VALUE_W / 2 - SIDE_W - 4, sy, SIDE_W, BTN_H).build());
            addRenderableWidget(Button.builder(Component.literal("+"), b -> {
                double val = clamp(getter.get() + step, min, max);
                setter.accept(val);
                valueBtn.setMessage(valueLabel(key, val));
                VZoomConfigIo.save();
            }).bounds(cx + VALUE_W / 2 + 4, sy, SIDE_W, BTN_H).build());
        }
        logicalY += ROW;
    }

    private void intStepperRow(String key, IntSupplier getter, IntConsumer setter,
                               int step, int min, int max) {
        int cx = this.width / 2;
        int sy = screenY();
        if (inBand(sy)) {
            Button valueBtn = Button.builder(intLabel(key, getter.getAsInt()), b -> {}).bounds(cx - VALUE_W / 2, sy, VALUE_W, BTN_H).build();
            addRenderableWidget(valueBtn);
            addRenderableWidget(Button.builder(Component.literal("-"), b -> {
                int val = clamp(getter.getAsInt() - step, min, max);
                setter.accept(val);
                valueBtn.setMessage(intLabel(key, val));
                VZoomConfigIo.save();
            }).bounds(cx - VALUE_W / 2 - SIDE_W - 4, sy, SIDE_W, BTN_H).build());
            addRenderableWidget(Button.builder(Component.literal("+"), b -> {
                int val = clamp(getter.getAsInt() + step, min, max);
                setter.accept(val);
                valueBtn.setMessage(intLabel(key, val));
                VZoomConfigIo.save();
            }).bounds(cx + VALUE_W / 2 + 4, sy, SIDE_W, BTN_H).build());
        }
        logicalY += ROW;
    }

    private void colorRow(String key, IntSupplier getter, IntConsumer setter) {
        int sy = screenY();
        if (inBand(sy)) {
            int current = getter.getAsInt();
            Button btn = Button.builder(colorLabel(key, current), b -> {
                int idx = 0;
                for (int i = 0; i < PALETTE.length; i++) if (PALETTE[i] == getter.getAsInt()) idx = i;
                int next = PALETTE[(idx + 1) % PALETTE.length];
                setter.accept(next);
                b.setMessage(colorLabel(key, next));
                VZoomConfigIo.save();
            }).bounds(this.width / 2 - BTN_W / 2, sy, BTN_W, BTN_H).build();
            addRenderableWidget(btn);
        }
        logicalY += ROW;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            int delta = (int) Math.round(-scrollY * ROW);
            int old = scrollOffset;
            scrollOffset = clamp(scrollOffset + delta, 0, maxScroll);
            if (scrollOffset != old) rebuildWidgets();
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xC0101010);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawString(this.font, this.title, (this.width - this.font.width(this.title)) / 2, 14, 0xFFFFFFFF, true);

        for (Header h : headers) {
            int y = visTop + h.logicalY - scrollOffset;
            if (y + this.font.lineHeight > visTop && y < visBottom) {
                int tx = (this.width - this.font.width(h.text)) / 2;
                int ty = y + (BTN_H - this.font.lineHeight) / 2;
                graphics.drawString(this.font, h.text, tx, ty, 0xFFFFD27A, false);
            }
        }

        if (maxScroll > 0) {
            int trackH = visBottom - visTop;
            int thumbH = Math.max(20, trackH * trackH / (trackH + maxScroll));
            int thumbY = visTop + (trackH - thumbH) * scrollOffset / maxScroll;
            int sx = this.width - 6;
            graphics.fill(sx - 1, visTop, sx + 1, visBottom, 0x44FFFFFF);
            graphics.fill(sx - 2, thumbY, sx + 2, thumbY + thumbH, 0xAAFFFFFF);
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private static Component toggleLabel(String key, boolean on) {
        return Component.literal(translate(key) + ": " + (on ? "ON" : "OFF"));
    }

    private static <T extends Enum<T>> Component cycleLabel(String key, T value) {
        return Component.literal(translate(key) + ": " + prettify(value.name()));
    }

    private static Component valueLabel(String key, double value) {
        String shown;
        if (value >= 1000.0) shown = String.format("%.0f", value);
        else if (value >= 100.0) shown = String.format("%.1f", value);
        else if (value >= 10.0) shown = String.format("%.2f", value);
        else shown = String.format("%.3f", value);
        return Component.literal(translate(key) + ": " + shown);
    }

    private static Component intLabel(String key, int value) {
        return Component.literal(translate(key) + ": " + value);
    }

    private static Component colorLabel(String key, int color) {
        return Component.literal(translate(key))
                .withStyle(s2 -> s2.withColor(TextColor.fromRgb(color & 0xFFFFFF)));
    }

    private static String translate(String key) {
        return Component.translatable(key).getString();
    }

    private static String prettify(String name) {
        if (name.isEmpty()) return name;
        String[] parts = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    private static double clamp(double val, double min, double max) {
        return val < min ? min : (val > max ? max : val);
    }

    private static int clamp(int val, int min, int max) {
        return val < min ? min : (val > max ? max : val);
    }

    private static void applyCinematic(ZoomConfig c) {
        net.minecraft.client.Options opt = Minecraft.getInstance().options;
        if (opt != null) {
            opt.smoothCamera = c.cinematicCamera;
        }
    }

    private static final class Header {
        final String text;
        final int logicalY;
        Header(String text, int logicalY) {
            this.text = text;
            this.logicalY = logicalY;
        }
    }
}
