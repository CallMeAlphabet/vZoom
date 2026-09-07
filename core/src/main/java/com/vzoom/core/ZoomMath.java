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
 * Pure-math helpers for the zoom engine. No Minecraft types here so the class
 * can live in the shared core module.
 */
public final class ZoomMath {
    private ZoomMath() {}

    public static double clamp(double v, double min, double max) {
        return v < min ? min : (v > max ? max : v);
    }

    public static int clamp(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }

    public static double clamp01(double v) {
        return v < 0.0 ? 0.0 : (v > 1.0 ? 1.0 : v);
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /** Cubic smoothstep in [0,1] -> [0,1]. */
    public static double smoothstep(double t) {
        t = clamp01(t);
        return t * t * (3.0 - 2.0 * t);
    }

    /**
     * Frame-rate independent cinematic approach of {@code current} towards
     * {@code target} using exponential smoothing.
     *
     * <p>{@code k} is a speed constant (higher = snappier). {@code dt} is the
     * elapsed seconds since the previous frame (clamped internally so a paused
     * tab cannot produce a teleport).</p>
     */
    public static double approach(double current, double target, double k, double dt) {
        if (dt <= 0.0) return current;
        if (dt > 0.25) dt = 0.25; // guard against huge gaps (alt-tab / lag spikes)
        double t = 1.0 - Math.exp(-k * dt);
        return current + (target - current) * t;
    }

    /**
     * Easing-aware approach. Maps {@link EasingMode} to a concrete per-frame
     * blend factor so the same {@code smoothing} constant yields noticeably
     * different "feels".
     */
    public static double easedApproach(double current, double target, double k, double dt, EasingMode mode) {
        if (mode == null || mode == EasingMode.INSTANT) {
            return target;
        }
        if (dt <= 0.0) return current;
        if (dt > 0.25) dt = 0.25;
        double diff = target - current;
        double absDiff = Math.abs(diff);
        if (absDiff < 1.0E-7) return target;

        double raw = 1.0 - Math.exp(-k * dt); // exponential decay factor in [0,1)
        double t;
        switch (mode) {
            case LINEAR: {
                // Constant velocity in zoom-space (uniform motion).
                double velocity = Math.max(1.0, k) * 0.5;
                double step = velocity * dt;
                t = Math.min(1.0, step / absDiff);
                break;
            }
            case SMOOTH: {
                // smoothstep the exponential factor -> gentle ease-in-out.
                t = raw * raw * (3.0 - 2.0 * raw);
                break;
            }
            case EASE_OUT: {
                // Decelerate hard near the target; full speed far away.
                double proximity = clamp01(1.0 - absDiff);     // 1 far, 0 close
                double eased = 1.0 - Math.pow(1.0 - raw, 2.0); // ease-out quad
                t = raw + (eased - raw) * (1.0 - proximity);
                break;
            }
            case EXPONENTIAL:
            default:
                t = raw;
                break;
        }
        return current + diff * t;
    }
}
