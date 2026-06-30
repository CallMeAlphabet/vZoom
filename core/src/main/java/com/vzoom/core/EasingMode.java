package com.vzoom.core;

/**
 * Transition curve applied to the per-frame smoothing of the zoom value.
 *
 * <p>Because the zoom target moves continuously (the user scrolls), the easing
 * shapes the <em>approach rate</em> towards the target rather than the more
 * familiar two-state interpolation. {@link ZoomMath#easedApproach} maps each
 * mode to a concrete per-frame blend factor.</p>
 */
public enum EasingMode {
    /** Snap to the target instantly — no cinematic glide at all. */
    INSTANT,
    /** Frame-rate independent exponential decay (the classic vZoom glide). */
    EXPONENTIAL,
    /** Constant-velocity approach — uniform motion towards the target. */
    LINEAR,
    /** Smoothstep blend of the exponential factor; gentler at both ends. */
    SMOOTH,
    /** Decelerates sharply as it nears the target (heavy ease-out). */
    EASE_OUT
}
