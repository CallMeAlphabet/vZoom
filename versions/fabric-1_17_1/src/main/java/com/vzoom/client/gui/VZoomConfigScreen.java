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
import com.vzoom.client.config.VZoomConfigIo;
import com.vzoom.core.ActivationMode;
import com.vzoom.core.EasingMode;
import com.vzoom.core.HudAlign;
import com.vzoom.core.HudStyle;
import com.vzoom.core.ScrollMode;
import com.vzoom.core.ZoomConfig;
import com.vzoom.core.ZoomState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.TextComponent;

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
        super(new TranslatableComponent("vzoom.config.title"));
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
        toggleRow("vzoom.config.hud", () -> cfg.hudEnabled, v -> cfg.hudEnabled = v);
        toggleRow("vzoom.config.cinematic", () -> cfg.cinematicCamera, v -> { cfg.cinematicCamera = v; applyCinematic(cfg); clearWidgets(); });
        if (cfg.cinematicCamera) {
            stepperRow("vzoom.config.cinematicSens", () -> cfg.cinematicSensitivity, v -> cfg.cinematicSensitivity = v, 0.05, 0.05, 2.0);
        }
        toggleRow("vzoom.config.retain", () -> cfg.retainZoomOnRelease, v -> cfg.retainZoomOnRelease = v);
        cycleRow("vzoom.config.activation", ActivationMode.values(), () -> cfg.activationMode, v -> cfg.activationMode = v);
        intStepperRow("vzoom.config.doubleTap", () -> cfg.doubleTapIntervalMs, v -> cfg.doubleTapIntervalMs = v, 10, 80, 1000);
        toggleRow("vzoom.config.reduceSens", () -> cfg.reduceSensitivity, v -> cfg.reduceSensitivity = v);
        toggleRow("vzoom.config.scaleSensWithZoom", () -> cfg.scaleSensitivityWithZoom, v -> cfg.scaleSensitivityWithZoom = v);
        toggleRow("vzoom.config.invert", () -> cfg.invertScroll, v -> cfg.invertScroll = v);

        header("vzoom.section.scroll");
        cycleRow("vzoom.config.scrollMode", ScrollMode.values(), () -> cfg.scrollMode, v -> cfg.scrollMode = v);
        cycleRow("vzoom.config.easing", EasingMode.values(), () -> cfg.easingMode, v -> cfg.easingMode = v);
        stepperRow("vzoom.config.scrollFactor", () -> cfg.scrollFactor, v -> cfg.scrollFactor = v, 0.01, 1.01, 8.0);
        stepperRow("vzoom.config.sensitivity", () -> cfg.sensitivity, v -> cfg.sensitivity = v, 0.1, 0.1, 5.0);
        stepperRow("vzoom.config.smoothing", () -> cfg.smoothing, v -> cfg.smoothing = v, 1.0, 1.0, 60.0);
        stepperRow("vzoom.config.linearStep", () -> cfg.linearStep, v -> cfg.linearStep = v, 0.25, 0.25, 20.0);
        intStepperRow("vzoom.config.discreteLevels", () -> cfg.discreteLevels, v -> cfg.discreteLevels = v, 1, 2, 64);

        header("vzoom.section.range");
        stepperRow("vzoom.config.defaultZoom", () -> cfg.defaultZoom, v -> cfg.defaultZoom = v, 0.5, 1.0, 50.0);
        stepperRow("vzoom.config.minZoom", () -> cfg.minZoom, v -> cfg.minZoom = v, 0.05, 0.05, 1.0);

        header("vzoom.section.look");
        stepperRow("vzoom.config.sensScale", () -> cfg.sensitivityScale, v -> cfg.sensitivityScale = v, 0.05, 0.05, 1.0);
        stepperRow("vzoom.config.sensFloor", () -> cfg.sensitivityScaleFloor, v -> cfg.sensitivityScaleFloor = v, 0.005, 0.005, 1.0);

        header("vzoom.section.hud");
        cycleRow("vzoom.config.hudStyle", HudStyle.values(), () -> cfg.hudStyle, v -> cfg.hudStyle = v);
        cycleRow("vzoom.config.hudPosition", HudAlign.values(), () -> cfg.hudAlign, v -> cfg.hudAlign = v);
        stepperRow("vzoom.config.hudScale", () -> cfg.hudScale, v -> cfg.hudScale = v, 0.25, 0.25, 4.0);
        colorRow("vzoom.config.hudColor", () -> cfg.hudColor, v -> cfg.hudColor = v);
        toggleRow("vzoom.config.hudBackground", () -> cfg.hudBackground, v -> cfg.hudBackground = v);
        toggleRow("vzoom.config.showFps", () -> cfg.showFps, v -> cfg.showFps = v);
        toggleRow("vzoom.config.showCoords", () -> cfg.showCoordinates, v -> cfg.showCoordinates = v);
        toggleRow("vzoom.config.showDirection", () -> cfg.showDirection, v -> cfg.showDirection = v);
        toggleRow("vzoom.config.showTarget", () -> cfg.showTargetInfo, v -> cfg.showTargetInfo = v);
        intStepperRow("vzoom.config.hudOffsetX", () -> cfg.hudOffsetX, v -> cfg.hudOffsetX = v, 1, -500, 500);
        intStepperRow("vzoom.config.hudOffsetY", () -> cfg.hudOffsetY, v -> cfg.hudOffsetY = v, 1, -500, 500);

        header("vzoom.section.autoDisable");
        toggleRow("vzoom.config.disableSprint", () -> cfg.disableWhileSprinting, v -> cfg.disableWhileSprinting = v);
        toggleRow("vzoom.config.disableFly", () -> cfg.disableWhileFlying, v -> cfg.disableWhileFlying = v);
        toggleRow("vzoom.config.disableUse", () -> cfg.disableWhileUsingItem, v -> cfg.disableWhileUsingItem = v);
        toggleRow("vzoom.config.disableWater", () -> cfg.disableWhileUnderwater, v -> cfg.disableWhileUnderwater = v);

        maxScroll = Math.max(0, logicalY - (visBottom - visTop));
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        int bottomY = this.height - 26;
        addRenderableWidget(new Button(cx - BTN_W - 4, bottomY, BTN_W, BTN_H, new TranslatableComponent("vzoom.config.reset"),  b -> {
            cfg.copyFrom(new ZoomConfig());
            applyCinematic(cfg);
            VZoomConfigIo.save();
            scrollOffset = 0;
            clearWidgets();
        }));

        addRenderableWidget(new Button(cx + 4, bottomY, BTN_W, BTN_H, new TranslatableComponent("gui.done"),  b -> onClose()));
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
            Button btn = new Button(this.width / 2 - BTN_W / 2, sy, BTN_W, BTN_H, toggleLabel(key, getter.get()),  b -> {
                setter.accept(!getter.get());
                b.setMessage(toggleLabel(key, getter.get()));
                VZoomConfigIo.save();
            });
            addRenderableWidget(btn);
        }
        logicalY += ROW;
    }

    private <T extends Enum<T>> void cycleRow(String key, T[] values, Supplier<T> getter, Consumer<T> setter) {
        int sy = screenY();
        if (inBand(sy)) {
            Button btn = new Button(this.width / 2 - BTN_W / 2, sy, BTN_W, BTN_H, cycleLabel(key, getter.get()),  b -> {
                T cur = getter.get();
                T next = values[(cur.ordinal() + 1) % values.length];
                setter.accept(next);
                b.setMessage(cycleLabel(key, next));
                VZoomConfigIo.save();
            });
            addRenderableWidget(btn);
        }
        logicalY += ROW;
    }

    private void stepperRow(String key, Supplier<Double> getter, Consumer<Double> setter,
                            double step, double min, double max) {
        int cx = this.width / 2;
        int sy = screenY();
        if (inBand(sy)) {
            Button valueBtn = new Button(cx - VALUE_W / 2, sy, VALUE_W, BTN_H, valueLabel(key, getter.get()),  b -> {});
            addRenderableWidget(valueBtn);
            addRenderableWidget(new Button(cx - VALUE_W / 2 - SIDE_W - 4, sy, SIDE_W, BTN_H, new TextComponent("-"),  b -> {
                double v = clamp(getter.get() - step, min, max);
                setter.accept(v);
                valueBtn.setMessage(valueLabel(key, v));
                VZoomConfigIo.save();
            }));
            addRenderableWidget(new Button(cx + VALUE_W / 2 + 4, sy, SIDE_W, BTN_H, new TextComponent("+"),  b -> {
                double v = clamp(getter.get() + step, min, max);
                setter.accept(v);
                valueBtn.setMessage(valueLabel(key, v));
                VZoomConfigIo.save();
            }));
        }
        logicalY += ROW;
    }

    private void intStepperRow(String key, IntSupplier getter, IntConsumer setter,
                               int step, int min, int max) {
        int cx = this.width / 2;
        int sy = screenY();
        if (inBand(sy)) {
            Button valueBtn = new Button(cx - VALUE_W / 2, sy, VALUE_W, BTN_H, intLabel(key, getter.getAsInt()),  b -> {});
            addRenderableWidget(valueBtn);
            addRenderableWidget(new Button(cx - VALUE_W / 2 - SIDE_W - 4, sy, SIDE_W, BTN_H, new TextComponent("-"),  b -> {
                int v = clamp(getter.getAsInt() - step, min, max);
                setter.accept(v);
                valueBtn.setMessage(intLabel(key, v));
                VZoomConfigIo.save();
            }));
            addRenderableWidget(new Button(cx + VALUE_W / 2 + 4, sy, SIDE_W, BTN_H, new TextComponent("+"),  b -> {
                int v = clamp(getter.getAsInt() + step, min, max);
                setter.accept(v);
                valueBtn.setMessage(intLabel(key, v));
                VZoomConfigIo.save();
            }));
        }
        logicalY += ROW;
    }

    private void colorRow(String key, IntSupplier getter, IntConsumer setter) {
        int sy = screenY();
        if (inBand(sy)) {
            int current = getter.getAsInt();
            Button btn = new Button(this.width / 2 - BTN_W / 2, sy, BTN_W, BTN_H, colorLabel(key, current),  b -> {
                int idx = 0;
                for (int i = 0; i < PALETTE.length; i++) if (PALETTE[i] == getter.getAsInt()) idx = i;
                int next = PALETTE[(idx + 1) % PALETTE.length];
                setter.accept(next);
                b.setMessage(colorLabel(key, next));
                VZoomConfigIo.save();
            });
            addRenderableWidget(btn);
        }
        logicalY += ROW;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (maxScroll > 0) {
            int delta = (int) Math.round(-scroll * ROW);
            int old = scrollOffset;
            scrollOffset = clamp(scrollOffset + delta, 0, maxScroll);
            if (scrollOffset != old) clearWidgets();
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        fill(poseStack, 0, 0, this.width, this.height, 0xC0101010);
        super.render(poseStack, mouseX, mouseY, partialTick);

        drawString(poseStack, this.font, this.title, (this.width - this.font.width(this.title)) / 2, 14, 0xFFFFFFFF);

        for (Header h : headers) {
            int y = visTop + h.logicalY - scrollOffset;
            if (y + this.font.lineHeight > visTop && y < visBottom) {
                int tx = (this.width - this.font.width(h.text)) / 2;
                int ty = y + (BTN_H - this.font.lineHeight) / 2;
                drawString(poseStack, this.font, h.text, tx, ty, 0xFFFFD27A);
            }
        }

        if (maxScroll > 0) {
            int trackH = visBottom - visTop;
            int thumbH = Math.max(20, trackH * trackH / (trackH + maxScroll));
            int thumbY = visTop + (trackH - thumbH) * scrollOffset / maxScroll;
            int sx = this.width - 6;
            fill(poseStack, sx - 1, visTop, sx + 1, visBottom, 0x44FFFFFF);
            fill(poseStack, sx - 2, thumbY, sx + 2, thumbY + thumbH, 0xAAFFFFFF);
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private static Component toggleLabel(String key, boolean on) {
        return new TextComponent(translate(key) + ": " + (on ? "ON" : "OFF"));
    }

    private static <T extends Enum<T>> Component cycleLabel(String key, T value) {
        return new TextComponent(translate(key) + ": " + prettify(value.name()));
    }

    private static Component valueLabel(String key, double value) {
        String shown;
        if (value >= 1000.0) shown = String.format("%.0f", value);
        else if (value >= 100.0) shown = String.format("%.1f", value);
        else if (value >= 10.0) shown = String.format("%.2f", value);
        else shown = String.format("%.3f", value);
        return new TextComponent(translate(key) + ": " + shown);
    }

    private static Component intLabel(String key, int value) {
        return new TextComponent(translate(key) + ": " + value);
    }

    private static Component colorLabel(String key, int color) {
        return new TextComponent(translate(key));
    }

    private static String translate(String key) {
        return new TranslatableComponent(key).getString();
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

    private static double clamp(double v, double min, double max) {
        return v < min ? min : (v > max ? max : v);
    }

    private static int clamp(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
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
