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
