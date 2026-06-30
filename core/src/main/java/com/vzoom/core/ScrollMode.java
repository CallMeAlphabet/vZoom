package com.vzoom.core;

/**
 * How a scroll-wheel notch is translated into a zoom delta.
 *
 * <ul>
 *   <li>{@link #GEOMETRIC} — each notch multiplies the current zoom by a factor
 *       (perceptually uniform steps, effectively infinite range). The original
 *       vZoom behaviour.</li>
 *   <li>{@link #LINEAR} — each notch adds a fixed amount to the zoom factor.</li>
 *   <li>{@link #DISCRETE} — the zoom snaps between a fixed number of evenly
 *       spaced levels (in log space), like a camera lens with detents.</li>
 * </ul>
 */
public enum ScrollMode {
    GEOMETRIC,
    LINEAR,
    DISCRETE
}
