<div align="center">

# TV Cinema Launcher

**Three Android TV home screens: the reference carousel, the classic one, and a universal one with a DeepSeek voice assistant.**

[![License: MIT](https://img.shields.io/badge/license-MIT-2ea44f?style=flat-square)](LICENSE)
[![Project site](https://img.shields.io/badge/site-online-6ed6ff?style=flat-square)](https://mosloboev444-hue.github.io/tv-cinema-launcher/)
[![APK](https://img.shields.io/badge/APK-7.2%20MB-0aa1dd?style=flat-square)](https://github.com/mosloboev444-hue/tv-cinema-launcher/releases/latest)
[![Android TV](https://img.shields.io/badge/Android%20TV-8.0%2B-3ddc84?style=flat-square)](https://developer.android.com/tv)
[![DeepSeek](https://img.shields.io/badge/DeepSeek-voice-4d6bfe?style=flat-square)](https://platform.deepseek.com/)
[![No ads](https://img.shields.io/badge/ads-none-critical?style=flat-square)](#)

</div>

**Project site:** <https://mosloboev444-hue.github.io/tv-cinema-launcher/>
([Russian](https://mosloboev444-hue.github.io/tv-cinema-launcher/)) ·
**Download APK:** [releases](https://github.com/mosloboev444-hue/tv-cinema-launcher/releases/latest)

![Cinema screen](screenshots/home-cinema.png)

A fork of the [CleanYourTV](https://github.com/Gathin23/cleanyourtv) 3.4 launcher with **three home screens**
and a switch between them right on the TV:

- **“Cinema”** — the reference look: clock and date in the corner, a weather chip, round header buttons
  (Inputs, Wi-Fi, VPN, Settings), a frosted dock of app icons over a photo wallpaper and an **endless
  carousel of every installed app**; the focused app is enlarged with a white ring and its name above.
- **“Classic”** — the previous CleanYourTV 3.2 home screen, pixel-identical: greeting, dock cards, app rows.
- **“Universal”** — a Google TV style screen: photo wallpaper (mountains and a lake), a minimal top bar,
  “Favorites” and “All apps”, a VPN button. Instead of the Google assistant — **DeepSeek**: a voice assistant
  that opens apps, switches screens, shows the weather, changes the volume and answers questions.

Built for a Philips 50PUS8057/60 (Android TV 14) and tested on it. Works on Android TV 8.0+.

## Features

| | |
|---|---|
| **Three screens** | “Cinema”, “Classic” and “Universal”; hold OK on Settings → “Screen: …”, or use the home button on the universal bar |
| **DeepSeek assistant** | mic on the universal screen: speech recognition, DeepSeek answers, TTS playback; launcher commands work offline |
| **Endless carousel** | all installed apps in a ring, 7 tiles on screen, the last one eases into the first |
| **Weather** | glass chip in the header: icon, temperature, short description; city by public IP, 30-minute cache |
| **VPN button** | round button in the header: green = VPN on, red = off; opens the configured VPN app |
| **App name badge** | two neutral styles above the focused tile: “Glass” (dark glass chip) and “Text only” |
| **Sounds** | soft carousel tick on focus change and a deeper confirm thud on OK; synthesised in code |
| **Smoothness** | layout-free carousel: 5.46 % janky frames, 11 ms median, 28 ms p90, 0 missed vsync |
| **Own VPN** | the VPN pill raises an AmneziaWG (WARP) tunnel inside the launcher; press again to stop |
| **Wallpaper** | sharp 4K photo wallpaper included; your own can be pushed over ADB |
| **No ads, no accounts** | no telemetry; one weather request pair per 30 min, DeepSeek only when asked |

## Screenshots

| | |
|---|---|
| ![Cinema](screenshots/home-cinema.png) | ![Classic](screenshots/home-classic.png) |
| **Cinema** — weather, glass chip, dock | **Classic** — the original 3.2 look |
| ![Glass badge](screenshots/chip-glass.png) | ![Text badge](screenshots/chip-none.png) |
| **“Glass” badge** (default) | **“Text only” badge** |

**Carousel in motion (GIF, recorded on the TV):**

![App carousel](screenshots/carousel.gif)

| ![Universal](screenshots/universal-home.png) | ![DeepSeek](screenshots/universal-deepseek.png) |
| **“Universal”** — wallpaper, Favorites and All apps | **DeepSeek assistant** — an answer |

More: [weather/focus](screenshots/home-cinema-weather-focus.png),
[carousel frames](screenshots/carousel-animation-frames.jpg), [RuTube](screenshots/home-cinema-rutube.png),
[torrents](screenshots/home-cinema-torrserve.png), [IPTVPORTAL](screenshots/home-cinema-iptvportal.png),
[karaoke](screenshots/home-cinema-karaoke.png), [YouTube](screenshots/home-cinema-youtube.png).

## Install

Download the APK from [Releases](https://github.com/mosloboev444-hue/tv-cinema-launcher/releases/latest)
(`CleanYourTV-ThreeScreens-4.1.2.apk`, 7.2 MB, versionCode 21 / 4.1.2) and install it:

```bash
adb connect 192.168.0.39:5555
adb install -r CleanYourTV-ThreeScreens-4.1.2.apk
```

It is signed with the same debug key as previous CleanYourTV 3.2/3.4 builds, so it installs as an update:
the Home role and app data are kept. Android TV 8.0+ (minSdk 26, targetSdk 34).

## Switching screens

1. Press and **hold OK** on the **Settings** button (gear in the header).
2. Choose the screen in the **“Screen: …”** row: Cinema → Classic → Universal. On the Cinema screen the row
   above it switches the name badge style. On the universal screen, the home button opens the same picker.
3. The choice is remembered across reboots.

## The “Universal” screen

A Google TV style screen built with Jetpack Compose; it runs on any Android TV 8.0+ and TV box:

- **Photo wallpaper** — mountains and a lake by default; sunset, night or a 10-minute rotation in settings.
- **Top bar** — mic (DeepSeek assistant), home-screen button, VPN (green/red), network, notifications with a
  counter, search, palette (launcher settings), TV settings, weather and the clock.
- **“Favorites” and “All apps”** — two rows of banner cards; OK opens an app, long OK opens its menu
  (add to favorites, reorder, app info).
- The layout is designed at 960 dp and scales for HD, Full HD and 4K; remote, mouse and touch.

## DeepSeek assistant

The mic pill opens the assistant. Speech is recognised on the TV, the answer is shown and **spoken by TTS**.
Launcher commands run locally and instantly, without the network: open an app, switch screens, show the
weather, change the volume, tell the time. Everything else goes to **DeepSeek** (`deepseek-chat`).

The key lives **only on the TV** — never in the APK or the repository. Set it on the TV (palette →
“DeepSeek: key”) or push a file:

```bash
echo -n "sk-..." > deepseek_key.txt
adb push deepseek_key.txt /sdcard/Android/data/com.gathin.tvhome/files/deepseek_key.txt
adb shell am force-stop com.gathin.tvhome && adb shell input keyevent KEYCODE_HOME
```

Microphone permission is requested on first use (or `adb shell pm grant com.gathin.tvhome
android.permission.RECORD_AUDIO`). Without a key the assistant explains where to get one; without a mic you
can type the question with the remote.

## Own VPN (AmneziaWG)

The VPN pill on the universal screen no longer opens another app — it **raises the launcher's own tunnel**
built on AmneziaWG (WireGuard with DPI obfuscation), the engine from
[SlimVPN](https://github.com/mosloboev444-hue/slimvpn).

- **Press once** to connect, once more to disconnect; the pill is green while the tunnel is up.
**The pill shows which VPN is selected**: its capsule carries the name (“My VPN” or the app)
and turns green while the tunnel is up, red while it is off.

**How the pill works.** It acts on what “VPN source” says: “My VPN (AmneziaWG)” raises the
built-in tunnel; any other app is opened instead. Switching away from the built-in tunnel stops it,
so two VPNs never run at once.

- **Hold OK** for the “My VPN” menu: `.conf` profiles, the active one, “Refresh profiles”,
  “Other VPN apps” and the system VPN settings.

Profiles live only on the TV, in the app folder:

```bash
adb push STR.WARP59009.conf /sdcard/Android/data/com.gathin.tvhome/files/vpn/
# then: hold OK on the VPN pill → “Refresh profiles”
```

No profiles (and no keys) are shipped in the APK or the repository. The engine is built for `armeabi-v7a`
(this Philips); on a 64-bit-only TV the button reports that the engine is unavailable and points to an
external VPN app.

## Weather and privacy

The launcher gained the **INTERNET** permission for the weather chip only: the city comes from the public
IP (`ipwho.is`), the reading from `wttr.in` (fallback `api.met.no`), cached in SharedPreferences for
30 minutes. No other network access, no telemetry, no background services.

## Build

Requires JDK 17+ and Android SDK 34:

```bash
cd launcher
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
./gradlew assembleRelease
# APK: launcher/app/build/outputs/apk/release/app-release.apk
```

The build is signed with the debug key from `~/.android/debug.keystore`, so it updates over earlier builds.
The full feature lives in `launcher/` (Java screens + Kotlin/Compose); only the karaoke tile patch is kept
in `build/karaoke-3.4.patch`.

## Rollback

- Back to the classic look: hold OK on Settings → “Screen: Classic”.
- From the universal screen: the home button in the top bar → “Cinema” or “Classic”.
- Back to a clean CleanYourTV 3.2 without the new screens: `adb install -r -d CleanYourTV-Karaoke-3.2.apk`
  from the [previous project](https://github.com/mosloboev444-hue/android-tv-youtube-vpn-karaoke).

## License and credits

- **MIT** — see [LICENSE](LICENSE).
- Based on [CleanYourTV](https://github.com/Gathin23/cleanyourtv) © 2026 Gathin, MIT. Thanks to the author.
- The fire “KARAOKE!!!” tile and the VPN gate come from
  [android-tv-youtube-vpn-karaoke](https://github.com/mosloboev444-hue/android-tv-youtube-vpn-karaoke).
- Wallpapers: [Wallhaven](https://wallhaven.cc/w/d6yz6j) (BMW M4) and the mountain lake of the universal
  screen; rights belong to their owners, used as personal wallpapers.
- The assistant uses the [DeepSeek API](https://platform.deepseek.com/); the idea grew out of
  [deepseek-alisa](https://github.com/mosloboev444-hue/deepseek-alisa).
- The universal screen is Jetpack Compose for TV (androidx.tv).
- The built-in VPN uses the [AmneziaWG](https://github.com/amnezia-vpn/amneziawg-android) engine
  (Apache-2.0) with the Android wrapper from [SlimVPN](https://github.com/mosloboev444-hue/slimvpn);
  see [LICENSE-AMNEZIAWG.txt](LICENSE-AMNEZIAWG.txt).

## Related projects

- [SlimHome](https://github.com/mosloboev444-hue/slimhome) — Android TV launcher: grid, weather, glass UI.
- [SlimTube](https://github.com/mosloboev444-hue/slimtube), [SlimIPTV](https://github.com/mosloboev444-hue/slimiptv),
  [SlimVPN](https://github.com/mosloboev444-hue/slimvpn), [SlimTV Suite](https://github.com/mosloboev444-hue/slimtv-suite).

---

Made for personal use and shared in case it helps someone. Issues and PRs welcome.
