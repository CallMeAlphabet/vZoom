package com.vzoom.core;

/**
 * The version-independent zoom engine: a singleton state machine that owns the
 * infinite geometric zoom value, the activation logic (hold / toggle / double-tap),
 * and the per-frame cinematic interpolation.
 *
 * <p>Version-specific modules feed it raw events:
 * <ul>
 *   <li>{@link #updateActivation(boolean, boolean)} once per frame with the raw
 *       key state + environment gating (drives hold/toggle/double-tap),</li>
 *   <li>{@link #update()} once per frame (advances the easing),</li>
 *   <li>{@link #onScroll(double)} from the mouse-scroll Mixin,</li>
 *   <li>{@link #computeFov(double)} from the GameRenderer/Camera FOV Mixin (pure read),</li>
 *   <li>{@link #stepZoom(int)} from the dedicated zoom-in/out keybinds.</li>
 * </ul></p>
 *
 * <p>Design notes:
 * <ul>
 *   <li>Zooming uses <b>geometric multiplication</b> by default which has no
 *       fixed ceiling or floor (the absolute FOV adjustment naturally shrinks as
 *       you zoom in and grows as you zoom out). {@link ScrollMode#LINEAR} and
 *       {@link ScrollMode#DISCRETE} offer alternative feels.</li>
 *   <li>Transitions use frame-rate independent smoothing shaped by
 *       {@link EasingMode}.</li>
 *   <li>The applied FOV is {@code vanillaFov / currentZoom}. A {@code minZoom}
 *       below 1.0 turns the lens into a wide-angle ("zoom out").</li>
 * </ul></p>
 */
public final class ZoomState {

    public static final ZoomState INSTANCE = new ZoomState();

    /** Shared configuration. Modules (re)load this from disk and read it back here. */
    public final ZoomConfig config = new ZoomConfig();

    // ---- live state ----
    private boolean active = false;
    private double targetZoom = 1.0;
    private double currentZoom = 1.0;
    private long lastNanos = 0L;

    // ---- activation edge detection ----
    private boolean prevHeld = false;
    private boolean toggledOn = false;
    private long lastPressNanos = 0L;

    private ZoomState() {}

    // ------------------------------------------------------------------
    // Per-frame activation (call exactly once per frame)
    // ------------------------------------------------------------------

    /**
     * Resolve whether the zoom should be active this frame given the raw key
     * state and whether the player's environment permits zooming. Handles the
     * hold / toggle / double-tap activation modes via edge detection.
     */
    public void updateActivation(boolean keyHeld, boolean environmentAllowsZoom) {
        ActivationMode mode = config.activationMode;
        boolean pressed = keyHeld && !prevHeld;
        boolean desired;

        switch (mode) {
            case TOGGLE: {
                if (pressed) toggledOn = !toggledOn;
                desired = toggledOn;
                break;
            }
            case DOUBLE_TAP: {
                if (pressed) {
                    long now = System.nanoTime();
                    long interval = config.doubleTapIntervalMs * 1_000_000L;
                    if (now - lastPressNanos <= interval) {
                        toggledOn = !toggledOn;
                    }
                    lastPressNanos = now;
                }
                desired = toggledOn;
                break;
            }
            case HOLD:
            default: {
                desired = keyHeld;
                break;
            }
        }

        prevHeld = keyHeld;

        boolean wasActive = active;
        active = desired && environmentAllowsZoom;

        if (active && !wasActive) {
            targetZoom = ZoomMath.clamp(Math.max(targetZoom, getEffectiveDefaultZoom()),
                    config.minZoom, config.maxZoom);
            currentZoom = targetZoom;
        } else if (!active && wasActive && !config.retainZoomOnRelease) {
            targetZoom = 1.0;
        }
    }

    // ------------------------------------------------------------------
    // Per-frame easing advance (call exactly once per frame)
    // ------------------------------------------------------------------
    public void update() {
        double dt = elapsedSeconds();
        double releaseTarget = config.retainZoomOnRelease ? targetZoom : 1.0;
        double goal = active ? targetZoom : releaseTarget;

        if (!active && config.retainZoomOnRelease) {
            currentZoom = ZoomMath.easedApproach(currentZoom, goal, config.smoothing, dt, config.easingMode);
        } else if (!active && Math.abs(currentZoom - 1.0) < 1.0E-4) {
            currentZoom = 1.0;
        } else {
            currentZoom = ZoomMath.easedApproach(currentZoom, goal, config.smoothing, dt, config.easingMode);
        }
    }

    // ------------------------------------------------------------------
    // Input from the Mouse scroll Mixin
    // ------------------------------------------------------------------

    /**
     * Apply a vertical scroll delta while zooming. Dispatches to the configured
     * {@link ScrollMode}.
     */
    public void onScroll(double verticalAmount) {
        if (!active || verticalAmount == 0.0) return;
        if (Math.abs(verticalAmount) < 1.0E-4) return;

        double amount = verticalAmount;
        if (config.invertScroll) amount = -amount;
        applyStep(amount > 0 ? 1 : -1, Math.abs(amount));
    }

    /**
     * Step the zoom by {@code direction} notches (+1 zoom in, -1 zoom out).
     */
    public void stepZoom(int direction) {
        if (!active) return;
        applyStep(direction, 1.0);
    }

    private void applyStep(int direction, double magnitude) {
        double mag = magnitude * config.sensitivity;
        switch (config.scrollMode) {
            case LINEAR: {
                targetZoom += direction * config.linearStep * mag;
                break;
            }
            case DISCRETE: {
                int levels = Math.max(2, config.discreteLevels);
                int idx = nearestDiscreteIndex(targetZoom, levels);
                idx += direction * (int) Math.max(1, Math.round(mag));
                idx = ZoomMath.clamp(idx, 0, levels - 1);
                targetZoom = discreteValue(idx, levels);
                break;
            }
            case GEOMETRIC:
            default: {
                double factor = Math.pow(config.scrollFactor, direction * mag);
                targetZoom *= factor;
                break;
            }
        }
        targetZoom = ZoomMath.clamp(targetZoom, config.minZoom, config.maxZoom);
    }

    private int nearestDiscreteIndex(double zoom, int levels) {
        if (zoom <= config.minZoom) return 0;
        if (zoom >= config.maxZoom) return levels - 1;
        double minLog = Math.log(config.minZoom);
        double maxLog = Math.log(config.maxZoom);
        double zLog = Math.log(zoom);
        double frac = (zLog - minLog) / (maxLog - minLog);
        int idx = (int) Math.round(frac * (levels - 1));
        return ZoomMath.clamp(idx, 0, levels - 1);
    }

    private double discreteValue(int idx, int levels) {
        double minLog = Math.log(config.minZoom);
        double maxLog = Math.log(config.maxZoom);
        double frac = levels <= 1 ? 0.0 : (double) idx / (levels - 1);
        return Math.exp(ZoomMath.lerp(minLog, maxLog, frac));
    }

    // ------------------------------------------------------------------
    // Pure FOV computation (safe to call many times per frame)
    // ------------------------------------------------------------------

    public double computeFov(double vanillaFov) {
        double zoom = Math.max(currentZoom, 1.0E-12);
        double fov = vanillaFov / zoom;
        double floor = 1.0E-4;
        if (fov < floor) fov = floor;
        return fov;
    }

    public boolean shouldCancelScroll() {
        return active;
    }

    // ------------------------------------------------------------------
    // Look-sensitivity scale (read by the MouseHandler Mixin)
    // ------------------------------------------------------------------

    public double getSensitivityScale() {
        if (!config.reduceSensitivity || !active) return 1.0;
        double scale;
        if (config.scaleSensitivityWithZoom) {
            scale = 1.0 / Math.max(1.0E-6, currentZoom);
        } else {
            scale = config.sensitivityScale;
        }
        return ZoomMath.clamp(scale, config.sensitivityScaleFloor, 1.0);
    }

    // ------------------------------------------------------------------
    // HUD helpers
    // ------------------------------------------------------------------

    public double getZoomBarProgress() {
        double minLog = Math.log(Math.max(1.0E-6, config.minZoom));
        double maxLog = Math.log(Math.max(config.minZoom + 1.0E-6, config.maxZoom));
        double zLog = Math.log(Math.max(1.0E-6, currentZoom));
        return ZoomMath.clamp01((zLog - minLog) / (maxLog - minLog));
    }

    // ------------------------------------------------------------------
    // Active state
    // ------------------------------------------------------------------
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        boolean wasActive = this.active;
        this.active = active;
        if (active && !wasActive) {
            targetZoom = ZoomMath.clamp(Math.max(targetZoom, getEffectiveDefaultZoom()),
                    config.minZoom, config.maxZoom);
            currentZoom = targetZoom;
        } else if (!active && !config.retainZoomOnRelease) {
            targetZoom = 1.0;
        }
    }

    // ------------------------------------------------------------------
    // HUD helpers
    // ------------------------------------------------------------------
    public int getZoomPercent() {
        return (int) Math.round(currentZoom * 100.0);
    }

    public double getCurrentZoom() {
        return currentZoom;
    }

    public double getTargetZoom() {
        return targetZoom;
    }

    public double getEffectiveDefaultZoom() {
        return ZoomMath.clamp(config.defaultZoom, config.minZoom, config.maxZoom);
    }

    public boolean shouldShowHud() {
        if (!active || !config.hudEnabled || config.hudStyle == HudStyle.MINIMAL) return false;
        return Math.abs(currentZoom - 1.0) > 1.0E-3;
    }

    public void reset() {
        currentZoom = 1.0;
        targetZoom = 1.0;
        toggledOn = false;
    }

    public void resetZoom() {
        currentZoom = 1.0;
        targetZoom = 1.0;
    }

    // ------------------------------------------------------------------
    private double elapsedSeconds() {
        long now = System.nanoTime();
        long prev = lastNanos;
        lastNanos = now;
        if (prev == 0L) return 0.016;
        double seconds = (now - prev) / 1_000_000_000.0;
        if (seconds < 0.0 || seconds > 1.0) seconds = 0.016;
        return seconds;
    }
}
