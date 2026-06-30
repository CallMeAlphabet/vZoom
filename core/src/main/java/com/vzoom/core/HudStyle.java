package com.vzoom.core;

/**
 * Visual presentation of the on-screen zoom indicator.
 */
public enum HudStyle {
    /** e.g. {@code x4.50} — the original compact text. */
    TEXT,
    /** Percentage text ({@code 450%}). */
    PERCENT,
    /** Both multiplier and a horizontal progress bar beneath it. */
    BAR,
    /** Multiplier plus distance / target info (when enabled). */
    COMPACT,
    /** Nothing — hides the indicator entirely (overlays still render). */
    MINIMAL
}
