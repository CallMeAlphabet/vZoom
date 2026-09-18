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

package com.vzoom.core;

/**
 * Plain configuration holder. (De)serialisation is performed by the
 * version-specific modules (they have Gson available at runtime), so this class
 * only carries state and sensible defaults.
 *
 * <p>All fields are public so a simple JSON mapper (reflection based) can read
 * and write them without boilerplate.</p>
 */
public final class ZoomConfig {

    // ------------------------------------------------------------------
    // Core scroll / smoothing
    // ------------------------------------------------------------------

    /** Geometric multiplier base applied per scroll notch (e.g. 1.15 = +15% per notch). */
    public double scrollFactor = 1.15;

    /** Extra scroll sensitivity multiplier. */
    public double sensitivity = 1.0;

    /** Cinematic lerp speed (higher = snappier transition). */
    public double smoothing = 14.0;

    /** Easing curve applied to the per-frame smoothing. */
    public EasingMode easingMode = EasingMode.EXPONENTIAL;

    /** How the scroll wheel is translated into zoom motion. */
    public ScrollMode scrollMode = ScrollMode.GEOMETRIC;

    /** Linear step size (only used by {@link ScrollMode#LINEAR}). */
    public double linearStep = 1.0;

    /** Number of evenly-spaced detent levels (only used by {@link ScrollMode#DISCRETE}). */
    public int discreteLevels = 16;

    /** Reverse the scroll direction (scroll up = zoom out). */
    public boolean invertScroll = false;

    // ------------------------------------------------------------------
    // Activation
    // ------------------------------------------------------------------

    /** How the zoom keybind engages the zoom. */
    public ActivationMode activationMode = ActivationMode.HOLD;

    /** Max milliseconds between two presses for a double-tap toggle. */
    public int doubleTapIntervalMs = 300;

    /** Whether the mouse should be smoothed (cinematic camera) while zooming. */
    public boolean cinematicCamera = true;
    /** Extra mouse-damping multiplier while zoomed with the cinematic camera on. */
    public double cinematicSensitivity = 1.0;

    /** When false the zoom factor is reset to 1.0 the moment the key is released. */
    public boolean retainZoomOnRelease = false;

    // ------------------------------------------------------------------
    // Look sensitivity (mouse-turn slowdown while zoomed)
    // ------------------------------------------------------------------

    /** Enable reducing the player's mouse-look speed while zoomed. */
    public boolean reduceSensitivity = true;

    /** Fixed sensitivity multiplier (used when {@link #scaleSensitivityWithZoom} is false). */
    public double sensitivityScale = 0.5;

    /** Scale the look sensitivity proportionally to the zoom (deeper = slower). */
    public boolean scaleSensitivityWithZoom = true;

    /** Floor for the proportional sensitivity multiplier (prevents it reaching ~0). */
    public double sensitivityScaleFloor = 0.02;

    // ------------------------------------------------------------------
    // Bounds / default
    // ------------------------------------------------------------------

    /** Default zoom multiplier when activating zoom. */
    public double defaultZoom = 5.0;

    /** Lower bound on the zoom factor. Values below 1.0 enable wide-angle zoom out. */
    public double minZoom = 1.0;

    // ------------------------------------------------------------------
    // HUD
    // ------------------------------------------------------------------

    /** Show the on-screen zoom indicator while zooming. */
    public boolean hudEnabled = true;

    /** Visual style of the zoom indicator. */
    public HudStyle hudStyle = HudStyle.TEXT;

    /** Where on the screen to draw the HUD. */
    public HudAlign hudAlign = HudAlign.BOTTOM_RIGHT;

    /** Scale of the HUD text (1.0 = default). */
    public double hudScale = 1.0;

    /** ARGB colour of the HUD text (default: warm orange, fully opaque). */
    public int hudColor = 0xFFFFB14E;

    /** ARGB colour of the HUD background panel. */
    public int hudBackgroundColor = 0x66000000;

    /** Horizontal pixel offset applied to the HUD anchor. */
    public int hudOffsetX = 0;

    /** Vertical pixel offset applied to the HUD anchor. */
    public int hudOffsetY = 0;

    /** Draw a faint backdrop behind the HUD text for readability. */
    public boolean hudBackground = true;

    // ------------------------------------------------------------------
    // HUD info lines
    // ------------------------------------------------------------------

    public boolean showTargetInfo = false;
    public boolean showCoordinates = false;  // looked-at block coordinates
    public boolean showFps = false;          // fps counter
    public boolean showDirection = false;    // facing yaw heading

    // ------------------------------------------------------------------
    // Auto-disable conditions
    // ------------------------------------------------------------------

    /** Disable zoom while sprinting. */
    public boolean disableWhileSprinting = false;

    /** Disable zoom while flying / elytra. */
    public boolean disableWhileFlying = false;

    /** Disable zoom while using an item (eating, aiming a bow, etc.). */
    public boolean disableWhileUsingItem = false;

    /** Disable zoom while submerged in water. */
    public boolean disableWhileUnderwater = false;

    // ==================================================================

    /** Default GLFW scancode-independent key (C). Modules translate this to a KeyBinding. */
    public int defaultKey = 67;

    // ==================================================================

    public void applyDefaultsIfInvalid() {
        if (scrollFactor <= 1.0) scrollFactor = 1.15;
        if (sensitivity <= 0.0) sensitivity = 1.0;
        if (smoothing <= 0.0) smoothing = 14.0;
        if (linearStep <= 0.0) linearStep = 1.0;
        if (discreteLevels < 2) discreteLevels = 16;
        if (doubleTapIntervalMs < 50) doubleTapIntervalMs = 300;
        if (hudScale <= 0.0) hudScale = 1.0;
        if (sensitivityScale <= 0.0) sensitivityScale = 0.5;
        if (sensitivityScaleFloor <= 0.0) sensitivityScaleFloor = 0.02;
        if (defaultZoom < 0.0) defaultZoom = 5.0;
        if (minZoom < 0.05) minZoom = 1.0;
        if (easingMode == null) easingMode = EasingMode.EXPONENTIAL;
        if (scrollMode == null) scrollMode = ScrollMode.GEOMETRIC;
        if (hudAlign == null) hudAlign = HudAlign.BOTTOM_RIGHT;
        if (hudStyle == null) hudStyle = HudStyle.TEXT;
        if (activationMode == null) activationMode = ActivationMode.HOLD;
    }

    /** Copy every field from {@code other} into this instance (deep copy of lists). */
    public void copyFrom(ZoomConfig other) {
        this.scrollFactor = other.scrollFactor;
        this.sensitivity = other.sensitivity;
        this.smoothing = other.smoothing;
        this.easingMode = other.easingMode;
        this.scrollMode = other.scrollMode;
        this.linearStep = other.linearStep;
        this.discreteLevels = other.discreteLevels;
        this.invertScroll = other.invertScroll;

        this.activationMode = other.activationMode;
        this.doubleTapIntervalMs = other.doubleTapIntervalMs;
        this.cinematicCamera = other.cinematicCamera;
        this.retainZoomOnRelease = other.retainZoomOnRelease;

        this.reduceSensitivity = other.reduceSensitivity;
        this.sensitivityScale = other.sensitivityScale;
        this.scaleSensitivityWithZoom = other.scaleSensitivityWithZoom;
        this.sensitivityScaleFloor = other.sensitivityScaleFloor;

        this.defaultZoom = other.defaultZoom;
        this.minZoom = other.minZoom;

        this.hudEnabled = other.hudEnabled;
        this.hudStyle = other.hudStyle;
        this.hudAlign = other.hudAlign;
        this.hudScale = other.hudScale;
        this.hudColor = other.hudColor;
        this.hudBackgroundColor = other.hudBackgroundColor;
        this.hudOffsetX = other.hudOffsetX;
        this.hudOffsetY = other.hudOffsetY;
        this.hudBackground = other.hudBackground;

        this.showTargetInfo = other.showTargetInfo;
        this.showCoordinates = other.showCoordinates;
        this.showFps = other.showFps;
        this.showDirection = other.showDirection;

        this.disableWhileSprinting = other.disableWhileSprinting;
        this.disableWhileFlying = other.disableWhileFlying;
        this.disableWhileUsingItem = other.disableWhileUsingItem;
        this.disableWhileUnderwater = other.disableWhileUnderwater;

        this.defaultKey = other.defaultKey;
    }
}
