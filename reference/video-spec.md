# Reference home-screen spec — from video_2026-11-09_23-21-57.mp4

Source: portrait 576×1024, 30 fps, 42.7 s, filmed at an angle (phone). Reference frames used:
`work/frames/f_007.jpg` (t≈7 s, clean home screen), `work/frames/f_033.jpg` (t≈33 s), and 10 fps close-ups
`work/spec/tmp/f10_home_001/042/060/070/100/110/…jpg` (t≈4–16 s, camera close to the screen).

**All coordinates below are in a 1920×1080 screen space.** They were obtained by perspective-rectifying
f_007: the TV bezel edges gave the horizon (camera yaw only, verticals are vertical in the image), and the
unfocused app tiles (squares) were used to fix the aspect. Result: `work/spec/crops/ref-panel-1920x1080.png`
(raw) and `…-grid.png` (60 px grid). Residual error is **±2–3 % (~±30 px)**, mostly horizontal.
The left ~7 % of the screen is never inside the video frame, so the clock's left margin is the least certain.

> Note on the car: it is **not** an F90 M5 CS. It is the current **BMW M5 (G90, 2025, illuminated kidney
> grille / "Iconic Glow", KZ plate "999 JOH 01")** in a purple-blue studio shot with a red backdrop.
> No public source for that exact photo was found; the wallpaper delivered is reconstructed from the
> video itself (see `work/wallpapers/`).

---

## 1. Screen composition

One photo wallpaper, full-bleed. Only three UI clusters on top of it:

| cluster | position |
|---|---|
| clock + divider + date | top-left |
| 3 round buttons | top-right |
| app dock | bottom centre |

No greeting, no weather, no app rows, no logo, no page indicator, nothing under the dock
(the dock sits 52 px above the bottom edge). UI is flat white text + frosted-glass surfaces.

## 2. Clock block (top-left)

| item | 1920×1080 value | notes |
|---|---|---|
| time text `14:32` | bbox ≈ x **150–360**, y **88–160** → digits cap-height **≈ 72 px** | 24-hour, no leading zero; white #FFFFFF |
| time font | very large, **light weight**, geometric/neo-grotesque (looks like Google Sans / Roboto Light / Segoe UI Light), tight tracking | est. font-size ≈ **105–115 px** (cap-height ≈ 0.68 em) |
| divider | 1 vertical line ≈ **2–3 px** wide, x ≈ **395**, y ≈ **88–148** (height ≈ 60 px) | white at ~60–70 % opacity |
| date line 1 | `ВОСКРЕСЕНЬЕ` x ≈ **440–640**, y ≈ **88–110** → cap-height **≈ 21 px** | uppercase, letter-spaced ~1–2 px, white |
| date line 2 | `8 НОЯБРЯ` x ≈ **440–560**, y ≈ **135–157** | same style, line pitch ≈ 47 px |
| left margin | ≈ **150 px** (8 %) — **estimate, panel edge never in frame** | top margin to time = 88 px |
| gap time → divider | ≈ 35 px; divider → date ≈ 45 px | divider is vertically centred on the two text lines |

Time/date were live: 14:32 → 14:33; date = Sunday 8 November (2026-11-08 was indeed a Sunday).

## 3. Top-right buttons

**3** frosted round buttons, identical size, right-aligned.

| item | value |
|---|---|
| count / shape | 3 × circle, diameter ≈ **56 px**, corner = full round |
| centres | y ≈ **136**; x ≈ **1560, 1664, 1768** → spacing **104 px**, gap ≈ 48 px |
| right margin | ≈ **128 px** (1920 − 1768 − 28) |
| top margin | ≈ **108 px** (136 − 28) |
| style | translucent frosted disc (blurred wallpaper behind, ~white 8–14 % fill, 1 px light rim, soft drop shadow), **white monochrome icon** ~26–28 px |
| icon 1 (left) | **microphone** — voice search (rounded mic capsule, thin stem). Not a profile/avatar: no head/shoulder shape |
| icon 2 (middle) | **Wi-Fi** (3 arcs + dot; looked brighter/white in one frame — active/connected state) |
| icon 3 (right) | **gear** — settings |

## 4. Dock (bottom)

A frosted-glass pill band holding a **circular carousel of 5 app tiles**. The focused tile is always the
**3rd of the 5 visible**; pressing left/right *rotates* the row (all 5 apps are always on screen, order is
cyclic). When focus moves, the row slides horizontally with eased motion (~150–250 ms), and the
focused tile grows while the previous one shrinks.

| item | 1920×1080 value | notes |
|---|---|---|
| band bbox | x ≈ **618–1305** (w ≈ **687**), y ≈ **883–1028** (h ≈ **145**), bottom margin **≈ 52 px** | horizontally centred (centre 961 ≈ 960) — confirms the rectification |
| band shape | rounded rect / pill, corner radius ≈ **65–72 px** (ends nearly semicircular) | |
| band material | frosted glass: backdrop blur ≈ 20–30 px, white fill ~**10–16 %**, 1 px light border ~25 % white, very soft outer shadow | wallpaper (car/wheel) is visibly blurred through it |
| tile size (unfocused) | **≈ 103 × 103 px**, corner radius ≈ **28–30 px** (≈ 28 % ) | tiles are exactly square; icons are the real app icons (they fill the tile) |
| gap between tiles | ≈ **18 px** (pitch ≈ 121 px) | |
| focused tile | **≈ 165 × 165 px** = **≈ 1.6 ×** unfocused | overflows the band by ~10 px above and below |
| focus treatment | **white border ring ≈ 3–4 px** around the whole tile + soft white glow/shadow; slight scale-up of icon inside | the ring is clearly visible on the black Кинопоиск tile |
| app-name chip | dark translucent rounded rect above the focused tile: **height ≈ 62–70 px**, width fits the text (≈ 180–200 px for "Кинопоиск", ≈ 130–150 px for "Иви"), corner radius ≈ **14–16 px**, gap to tile top ≈ **45–55 px**, horizontally centred on the tile | fill ≈ rgba(25,25,30,0.70), maybe light 1 px rim; text white, semi-bold, cap-height ≈ **26–30 px**, no leading spaces — e.g. "Иви", "Кинопоиск", "YouTube", "VK Видео" |
| chip visibility | appears when the focus lands on a tile, stays while focus is there and fades out after focus leaves (in the t≈33 s frames the Иви tile is focused **without** a chip) | |
| tiles ring order (cyclic) | **YouTube → Кинопоиск → Иви → VK Видео → "•••" → (YouTube)** | observed windows are all 5 consecutive items of this ring with the focused one in slot 3 |

### Dock apps

1. **YouTube** — white tile, classic red rounded-rect play icon.
2. **Кинопоиск** — black tile, orange/yellow starburst "К" (official Kinopoisk icon).
3. **Иви** — light/white tile with a red rounded-square ivi logo (white "ivi" inside); default focused app in the video.
4. **VK Видео** — pink/red tile, blue vertical bar + white play triangle (VK Video icon).
5. **"•••" tile** — pale/frosted translucent tile, same glass material as the band, with **three white dots** centred.
   It is one of the five carousel items but its semantics is **unknown**: most likely the launcher's
   "more apps / приложения" tile, but it could be a placeholder. It never showed a name chip in the video.

Frames that show a name chip: **Иви** — f_006/f_007 (t 6–7 s) and f10_home_070 (t 10.9 s);
**Кинопоиск** — f10_home_042 (8.1 s), f10_home_092/095 (13.1–13.4 s), f10_home_110 (14.9 s);
**YouTube** — f10_home_100 (13.9 s); **VK Видео** — f_012 (12 s) / f10_home_060 (9.9 s window).

## 5. Wallpaper / background

- Full-bleed photo: dark red studio backdrop (top), red-pink lit floor with reflections (bottom), car in the
  middle band, filling the full 16:9 frame (no letterboxing, no visible tint/dark overlay on top of it —
  the UI just sits on the photo).
- Delivered asset (reconstructed from the video, UI removed): `work/wallpapers/wallpaper-1920x1080.jpg`
  (and a 3840×2160 upscale `wallpaper-4k.jpg`). Sharpness is limited by the source video (effective
  ≈ 570×450 for the whole screen), the two smudged areas are where the dock/clock/buttons were painted
  over the photo. If we iterate: replace with the real photo if it can be found, or re-shoot-crop the car
  from the close-up frames for extra sharpness.

## 6. What to reproduce, in order of visual importance

1. Frosted dock band + 5-icon cyclic carousel with the focused tile at slot 3, white ring + name chip.
2. Big light-weight clock + thin divider + two-line uppercase date, top-left.
3. Three frosted round icon buttons top-right (mic, Wi-Fi, gear).
4. Photo wallpaper (M5 G90, red studio, KZ plate).
5. Everything else empty — no rows, no labels, no greeting.

## 7. Uncertainties (marked as estimates)

- Clock left margin (~150 px) and the exact left/top rounding of the panel: the panel's left edge is never
  in the video frame (video is a 9:16 crop); the value comes from a 16:9 reconstruction of the visible part.
- Absolute sizes carry ±2–3 % from the rectification; the chip height/width and band corner radius are
  ±15 % (small, soft details).
- Fonts are unknown: visual match only (light sans for the clock, medium/rounded sans for date and chip).
- Semantics of the 5th tile ("•••") and the exact icon set of the 3 buttons (mic / Wi-Fi / gear) are inferred.
- The video was filmed at an angle with a rolling phone camera; one frame is not perfectly consistent.

---

## 8. Delivered files

| file | what |
|---|---|
| `work/spec/video-spec.md` | this spec |
| `work/spec/crops/ref-panel-1920x1080.png` | perspective-corrected reference frame (f_007) in 1920×1080 screen space — the measurement basis |
| `work/spec/crops/ref-panel-1920x1080-grid.png` | same with 60 px grid |
| `work/spec/crops/clock-block-2x.png`, `topright-buttons-2x.png`, `topright-buttons-f033-5x.png` | clock/date block, top-right buttons |
| `work/spec/crops/dock-2x.png`, `dock-focus-ivi-chip-3x.png`, `dock-focus-kinopoisk-chip-3x.png`, `dock-focus-youtube-chip-3x.png` | dock band, focused tile + name chips |
| `work/spec/crops/dock-tile-dots-4x.png`, `dock-focused-tile-ring-4x.png` | the 5th "•••" tile, white focus ring |
| `work/spec/crops/wallpaper-car-2x.png` | car/wallpaper detail used to identify the model |
| `work/spec/crops/source-f_007-home.jpg`, `source-f_033-home.jpg` | original video frames (angle view) |
| `work/wallpapers/wallpaper-1920x1080.jpg` | reconstructed wallpaper (delivered, in use on TV) |
| `work/wallpapers/wallpaper-4k.jpg` | 3840×2160 upscale of the same |
| `work/target-mockup.png` | 1920×1080 mockup of the target screen (visual acceptance target) |

## 9. Mockup ("#": `work/target-mockup.png`) — what is real vs approximated

Real measured geometry (clock positions/sizes, dock band bbox, tile sizes/gaps, focus scale, chip position,
button positions). Approximated: fonts (Segoe UI Light/regular/semibold stand-ins), app icons re-drawn with
Pillow primitives (not the official assets), frosted-glass blur, and the wallpaper is the video-derived
reconstruction (soft in the areas the UI covered; the dock band area behind the icons is a smooth patch).
