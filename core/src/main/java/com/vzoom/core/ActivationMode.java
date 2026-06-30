package com.vzoom.core;

/**
 * How the zoom keybind activates the zoom.
 *
 * <ul>
 *   <li>{@link #HOLD} — zoom while the key is held (classic behaviour).</li>
 *   <li>{@link #TOGGLE} — a single press flips the zoom on/off.</li>
 *   <li>{@link #DOUBLE_TAP} — a quick double-press flips the zoom on/off.</li>
 * </ul>
 */
public enum ActivationMode {
    HOLD,
    TOGGLE,
    DOUBLE_TAP
}
