# vZoom
vZoom — a feature-rich, smooth cinematic zoom mod for Minecraft Fabric & NeoForge

Available for Fabric 1.14–26.3 and NeoForge 1.20.2–26.3.

## Table of Contents
- [Quick Start](#quick-start)
- [Controls](#controls)
- [Zoom Math](#how-the-zoom-math-works)
- [Edit Configuration](#edit-configuration)
- [Note](#note)
- [Manually Building](#manually-building)


## Quick Start

Hold the zoom key (default `C`) and scroll the mouse wheel to zoom in / out with an effectively unlimited range.

## Controls

All keybinds are configurable under `Options → Controls`.

| Action | Default | What it does |
| --- | --- | --- |
| **Zoom** | `C` | Zooms. How it engages depends on the Activation Mode. While zooming, scrolling the wheel adjusts the zoom level. |
| **Zoom In (step)** | unbound | Zooms in by one level per press (same as one scroll-up tick). Only does something while zoom is engaged. |
| **Zoom Out (step)** | unbound | Zooms out by one level per press. |
| **Reset Zoom** | unbound | Snaps the zoom value straight back to 1.0 (normal view) without disengaging. |
| **Reload Config** | unbound | Re-reads `config/vzoom.json` live. |


## How the zoom math works

- The applied FOV is `vanillaFov / currentZoom`. A zoom of `2.0` halves the FOV (looks 2× closer), `0.5` doubles it (wide-angle "zoom out").
- By default scrolling uses geometric scaling: each level multiplies the current zoom by `scrollFactor`, so steps feel uniform and the range is effectively unbounded.
- The displayed value is the interpolated `currentZoom`, which glides toward your scroll target using the chosen easing.


## Edit Configuration

Open the settings via Mod Menu (a suggested, optional dependency), or edit `config/vzoom.json` directly and press "Reload Config" in-game. Every option is editable in the settings screen.


## Note

Colors are stored as ARGB integers (`0xAARRGGBB`). The settings screen cycles a small palette for the HUD color; for full control (including the panel color that has no screen control) edit `config/vzoom.json` directly and press "Reload Config".


## Manually Building

    ./gradlew build

Jars are output under `versions/<module>/build/libs/` (e.g. `versions/neoforge-26_2/build/libs/vzoom-neoforge-26_2-1.2.0.jar`).
