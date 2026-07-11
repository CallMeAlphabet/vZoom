<img alt="codeberg" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/codeberg_vector.svg"> <img alt="fabric-api" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-api_vector.svg">

 <a href="https://deinserverhost.de/store/aff.php?aff=5710"><img src="https://deinserverhost.de/tca/affiliate-groß.jpg" width="1024" height="207" border="0"></a>

### ⚠️ Project Status: Maintenance-Only

This project is now in **maintenance-only mode**.

* **Feature Complete:** No new features will be added.
* **Updates:** Development is limited to keeping the project compatible with newer versions.
* **Support:** Issue response times and PR reviews will be slower than usual.
 
# vZoom

A feature-rich, smooth cinematic zoom mod for Minecraft (Fabric).

Hold the zoom key (default `C`) and scroll the mouse wheel to dial the zoom in
and out with effectively unlimited range, with look-sensitivity reduction,
saveable zoom profiles, and a configurable info HUD — all from one
version-independent engine that ships for two Minecraft targets.

## Targets

vZoom is built around a shared, version-independent engine in `:core`. The same
engine drives two targets:

- `v1_21_11` — Minecraft 1.21.11 (Mojang mappings, remap Loom, Java 21)
- `v26_2` — Minecraft 26.2 (Mojang names, NoRemap Loom, Java 25)

Both behave identically; only the low-level rendering / keybind glue differs.

## Controls

All keybinds are configurable under **Options → Controls**. The auxiliary
keybinds default to **unbound** so they never clobber your existing bindings —
assign them if you want them.

| Action | Default | What it does |
| --- | --- | --- |
| **Zoom** | `C` | Engages zoom. How it engages depends on **Activation Mode**. While engaged, scrolling the wheel adjusts the zoom level. |
| **Zoom In (step)** | unbound | Zooms in by one notch per press (same as one scroll-up tick). Only does something while zoom is engaged. |
| **Zoom Out (step)** | unbound | Zooms out by one notch per press. |
| **Reset Zoom** | unbound | Snaps the zoom value straight back to 1.0 (normal view) without disengaging. |
| **Previous Preset** | unbound | Jumps to the previous saved preset. |
| **Reload Config** | unbound | Re-reads `config/vzoom.json` live — handy if you hand-edit the file. |

---

## How the zoom math works (short version)

- The applied FOV is `vanillaFov / currentZoom`. A zoom of `2.0` halves the FOV
  (looks 2× closer); `0.5` doubles it (wide-angle "zoom out").
- By default scrolling uses **geometric** scaling: each notch multiplies the
  current zoom by `scrollFactor`, so steps feel perceptually uniform and the
  range is effectively unbounded.
- The displayed value is the *interpolated* `currentZoom`, which glides toward
  your scroll target using the chosen easing.

---

## Config reference

Open the settings via **Mod Menu** (a suggested, optional dependency), or edit
`config/vzoom.json` directly and press **Reload Config** in-game. Every option
below is editable in the settings screen unless marked *(JSON only)*. Booleans
toggle; numbers use `+`/`-` steppers; enums cycle.

### Basic

| Option | Default | Description |
| --- | --- | --- |
| **Zoom HUD** (`hudEnabled`) | `true` | Master switch for the on-screen zoom indicator. Off hides the readout entirely. |
| **Cinematic Camera** (`cinematicCamera`) | `true` | Enables Minecraft's vanilla "smooth camera" while zoomed, softening mouse movement for a cinematic feel. |
| **Retain Zoom on Release** (`retainZoomOnRelease`) | `false` | When on, releasing the zoom key keeps the current zoom level instead of resetting to 1.0. Useful combined with **Toggle** activation. |
| **Activation Mode** (`activationMode`) | `HOLD` | How the Zoom key engages zoom. `HOLD` = zoom only while held. `TOGGLE` = one press flips zoom on/off. `DOUBLE_TAP` = a quick double-press flips it on/off. |
| **Double-Tap Window (ms)** (`doubleTapIntervalMs`) | `300` | Max milliseconds between two presses to count as a double-tap (only used in `DOUBLE_TAP` mode). Lower = need faster taps. |
| **Reduce Look Sensitivity** (`reduceSensitivity`) | `true` | Slows down mouse-look while zoomed so the view doesn't whip around at high zoom. Strongly recommended for aiming. |
| **Scale Sensitivity w/ Zoom** (`scaleSensitivityWithZoom`) | `true` | If on, the look-slowdown grows with depth (deeper = slower). If off, a fixed factor set by **Sensitivity Scale** is used. |
| **Invert Scroll** (`invertScroll`) | `false` | Reverses the scroll direction (scroll up = zoom out). |

### Scroll & Easing

| Option | Default | Description |
| --- | --- | --- |
| **Scroll Mode** (`scrollMode`) | `GEOMETRIC` | How a wheel notch becomes zoom. `GEOMETRIC` multiplies the current zoom (smooth, unlimited range). `LINEAR` adds a fixed amount each notch. `DISCRETE` snaps between a fixed number of evenly-spaced levels (like camera-lens detents). |
| **Easing** (`easingMode`) | `EXPONENTIAL` | The curve of the zoom transition. `INSTANT` snaps with no glide. `EXPONENTIAL` is the classic fast-then-settle glide. `LINEAR` moves at a constant speed toward the target. `SMOOTH` is a gentle ease-in-out. `EASE_OUT` accelerates then settles softly. |
| **Scroll Factor** (`scrollFactor`) | `1.15` | Multiplier per notch in `GEOMETRIC` mode. `1.15` ≈ +15% zoom in per notch. Higher = coarser steps. |
| **Scroll Sensitivity** (`sensitivity`) | `1.0` | Extra multiplier applied to every scroll delta. Higher = faster scrolling overall. |
| **Transition Speed** (`smoothing`) | `14.0` | Speed of the easing glide (higher = snappier, lower = lazier). |
| **Linear Step** (`linearStep`) | `1.0` | Zoom amount added/removed per notch in `LINEAR` mode. |
| **Discrete Levels** (`discreteLevels`) | `16` | Number of evenly spaced detent levels in `DISCRETE` mode. More = finer steps. |

### Zoom Range

| Option | Default | Description |
| --- | --- | --- |
| **Default Zoom** (`defaultZoom`) | `5.0` | Zoom multiplier applied the moment you engage zoom (before scrolling). |
| **Min Zoom** (`minZoom`) | `1.0` | Lower bound on the zoom value. Set below `1.0` (e.g. `0.6`) to allow **wide-angle zoom-out** past the normal FOV. |
| **Max Zoom** (`maxZoom`) | `4096.0` | Upper bound. Purely a safety cap; raise it for extreme telescope zooms. |

### Look Sensitivity

| Option | Default | Description |
| --- | --- | --- |
| **Sensitivity Scale** (`sensitivityScale`) | `0.5` | Fixed look-speed multiplier used when **Scale Sensitivity w/ Zoom** is off. `0.5` = half-speed look while zoomed. |
| **Sensitivity Floor** (`sensitivityScaleFloor`) | `0.02` | Minimum look-speed multiplier when scaling with zoom, so very deep zoom never freezes the camera entirely. |

### HUD

| Option | Default | Description |
| --- | --- | --- |
| **HUD Style** (`hudStyle`) | `TEXT` | Appearance of the indicator. `TEXT` shows `x4.50`. `PERCENT` shows `450%`. `BAR` shows the multiplier plus a horizontal progress bar. `COMPACT` is a compact variant; `MINIMAL` hides the readout. |
| **HUD Position** (`hudAlign`) | `BOTTOM_RIGHT` | Screen anchor: any corner, any edge center, or dead center. |
| **HUD Scale** (`hudScale`) | `1.0` | Text size multiplier for the indicator. |
| **HUD Color** (`hudColor`) | `#FFB14E` | Text color of the main zoom line (cycles a palette in the screen; full ARGB in JSON). |
| **HUD Background** (`hudBackground`) | `true` | Draws a translucent panel behind the indicator text for readability. |
| **Show FPS** (`showFps`) | `false` | Adds an `FPS:` line to the indicator. |
| **Show Target Coordinates** (`showCoordinates`) | `false` | Adds an `XYZ:` line showing the **coordinates of the block (or entity) you are currently looking at** — a long-range raycast, so it works far beyond your normal reach. Omitted if you're looking at the sky. |
| **Show Facing** (`showDirection`) | `false` | Adds an `8-way compass heading (`Facing: NW` etc.) line. |
| **Show Target Info** (`showTargetInfo`) | `false` | Adds a line naming what you're looking at (block or entity display name) plus its distance, e.g. `Diamond Ore (62.4m)`. |
| **HUD Offset X / Y** (`hudOffsetX`, `hudOffsetY`) | `0` | Pixel offsets nudging the indicator from its anchor for fine placement. |
| **HUD Background Color** (`hudBackgroundColor`) *(JSON only)* | `#66000000` | ARGB color of the indicator panel. |

### Auto-Disable

Each of these, when on, forces zoom off while that condition is true (the key
can still be held; it just won't engage). Handy to stop zoom fighting with
combat or movement.

| Option | Default | Description |
| --- | --- | --- |
| **Disable While Sprinting** (`disableWhileSprinting`) | `false` | No zoom while sprinting. |
| **Disable While Flying** (`disableWhileFlying`) | `false` | No zoom while creative flying or gliding with an elytra. |
| **Disable While Using Item** (`disableWhileUsingItem`) | `false` | No zoom while eating, drawing a bow, blocking, etc. |
| **Disable While Underwater** (`disableWhileUnderwater`) | `false` | No zoom while submerged. |

### Presets

A preset is a **complete zoom profile**, not just a zoom level. Switching to one
applies its zoom *and* its easing, scroll mode, scroll factor/sensitivity,
cinematic camera, look-sensitivity behaviour and HUD style all at once — so
"Scope" can feel totally different from "Close" even before you scroll. They are
defined in `config/vzoom.json` (there is no in-game editor for creating/removing
them). Switching a preset (via the button or the keybinds) is saved
automatically so the active profile persists.

| Option | Default | Description |
| --- | --- | --- |
| **Preset** (screen button) | — | Shows the **currently active** preset as `Preset: <name> (<index>/<count>)`. Clicking it cycles to the next preset; the **Next/Previous Preset** keybinds do the same. |
| **Presets** (`presets`) *(JSON only)* | see below | Ordered list of profile entries. Each entry carries `{zoom, smoothing, easingMode, scrollMode, scrollFactor, sensitivity, cinematicCamera, reduceSensitivity, scaleSensitivityWithZoom, sensitivityScale, hudStyle, hudEnabled}` plus a `name`. Edit this list in `config/vzoom.json`, then press **Reload Config**. |
| **Preset Loop** (`presetLoop`) | `true` | If on, cycling past the last preset wraps to the first (and vice-versa). |

The default presets demonstrate how much a profile can change:

| Preset | Zoom | Notable settings |
| --- | --- | --- |
| **Close** | 2.0× | Smoothing 18, `SMOOTH` easing |
| **Standard** | 5.0× | Defaults (geometric, exponential, smoothing 14) |
| **Far** | 12.0× | Smoothing 12, scroll factor 1.2 |
| **Spy** | 30.0× | `SMOOTH` easing, smoothing 9, scroll factor 1.25 |
| **Scope** | 80.0× | `EASE_OUT` easing, `DISCRETE` scroll mode, `PERCENT` HUD, smoothing 6 |

### Other

| Option | Default | Description |
| --- | --- | --- |
| **Default Key** (`defaultKey`) | `67` (`C`) | The GLFW keycode the Zoom keybind binds on first launch. Change it in Controls afterward. |

---

## A note on colors

Colors are stored as ARGB integers (`0xAARRGGBB`). The settings screen cycles a
small palette for **HUD Color**; for full control (including the panel color that
has no screen control) edit `config/vzoom.json` directly and press
**Reload Config**.

## Building

    ./gradlew build

Jars are output under `v1_21_11/build/libs/` and `v26_2/build/libs/`.
