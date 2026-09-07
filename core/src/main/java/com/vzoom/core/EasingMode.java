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
