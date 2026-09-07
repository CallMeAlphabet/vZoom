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
