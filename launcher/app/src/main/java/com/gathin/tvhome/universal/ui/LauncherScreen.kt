package com.gathin.tvhome.universal.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import com.gathin.tvhome.universal.NotificationState
import com.gathin.tvhome.R
import com.gathin.tvhome.ScreenBridge
import com.gathin.tvhome.universal.data.AppsCache
import com.gathin.tvhome.universal.data.LauncherState
import com.gathin.tvhome.universal.data.Sound
import com.gathin.tvhome.universal.data.SoundFx
import com.gathin.tvhome.universal.data.WALLPAPER_AUTO
import com.gathin.tvhome.universal.data.findVpnPackages
import com.gathin.tvhome.universal.data.isVideoMix
import com.gathin.tvhome.universal.data.launchApp
import com.gathin.tvhome.universal.data.loadApps
import com.gathin.tvhome.universal.data.loadDefaultVpn
import com.gathin.tvhome.universal.data.loadFavorites
import com.gathin.tvhome.universal.data.loadSettings
import com.gathin.tvhome.universal.data.rememberWeather
import com.gathin.tvhome.universal.data.saveDefaultVpn
import com.gathin.tvhome.universal.data.saveFavorites
import com.gathin.tvhome.universal.data.saveSettings
import com.gathin.tvhome.universal.data.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun LauncherScreen() {
    // Лаунчер не на экране (запущено другое приложение): интерфейс выгружен, память и процессор свободны
    if (!LauncherState.visible) {
        Box(Modifier.fillMaxSize().background(Color.Black))
        return
    }
    // Интерфейс рассчитан на экран шириной 960 dp. На HD, Full HD и 4K телевизорах плотность разная,
    // поэтому масштабируем её так, чтобы вид был одинаковым на любом экране.
    val base = LocalDensity.current
    val scale = (LocalConfiguration.current.screenWidthDp / 960f).coerceIn(0.5f, 4f)
    CompositionLocalProvider(LocalDensity provides Density(base.density * scale, base.fontScale)) {
        LauncherContent()
    }
}

@Composable
private fun LauncherContent() {
    val context = LocalContext.current
    var apps by remember { mutableStateOf(AppsCache.apps) }
    val savedFavs = remember { loadFavorites(context) }
    var favs by remember { mutableStateOf(savedFavs ?: emptyList()) }
    var settings by remember { mutableStateOf(loadSettings(context)) }
    var menuPkg by remember { mutableStateOf<String?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showNotifs by remember { mutableStateOf(false) }
    var showVpnPicker by remember { mutableStateOf(false) }
    var showScreens by remember { mutableStateOf(false) }
    var showAssistant by remember { mutableStateOf(false) }
    var showKey by remember { mutableStateOf(false) }
    var launchAfterPick by remember { mutableStateOf(false) }
    // CleanYourTV's configured VPN app is the universal screen's default one, until changed here.
    var defaultVpn by remember { mutableStateOf(loadDefaultVpn(context) ?: ScreenBridge.defaultVpn(context)) }
    val vpnPackages = remember { findVpnPackages(context) }
    var autoIdx by remember { mutableIntStateOf(0) }

    val weather by rememberWeather()
    val notifs by NotificationState.items.collectAsState()
    val dialogOpen = rememberUpdatedState(menuPkg != null || showSettings || showNotifs || showVpnPicker || showScreens || showAssistant || showKey)

    // «Назад» не закрывает лаунчер (диалоги закрываются сами)
    BackHandler(true) {}

    LaunchedEffect(Unit) {
        SoundFx.configure(settings.soundTheme, settings.volume)
        val loaded = withContext(Dispatchers.IO) { loadApps(context) }
        apps = loaded
        // Первый запуск: «Видео MIX» сразу в избранном, если установлено
        if (savedFavs == null) {
            val preset = ScreenBridge.defaultFavorites(context).toList()
            favs = if (preset.isNotEmpty()) preset else loaded.filter { isVideoMix(it) }.map { it.packageName }
        }
        SoundFx.playStartup()
    }

    // автосмена обоев
    LaunchedEffect(settings.wallpaper) {
        if (settings.wallpaper == WALLPAPER_AUTO) {
            while (true) { delay(10 * 60_000L); autoIdx = (autoIdx + 1) % WALLPAPERS.size }
        }
    }
    val wallpaperIndex = if (settings.wallpaper == WALLPAPER_AUTO) autoIdx else settings.wallpaper

    // заставка после периода бездействия
    LaunchedEffect(settings.screensaverMinutes) {
        while (true) {
            delay(5_000)
            val m = settings.screensaverMinutes
            if (m > 0 && !LauncherState.screensaver && !dialogOpen.value &&
                System.currentTimeMillis() - LauncherState.lastInput > m * 60_000L
            ) LauncherState.screensaver = true
        }
    }

    val installedFavs = remember(apps, favs) { favs.filter { p -> apps.any { it.packageName == p } } }
    val favItems = remember(apps, installedFavs) { installedFavs.mapNotNull { p -> apps.find { it.packageName == p } } }
    fun updateFavs(list: List<String>) { favs = list; saveFavorites(context, list) }

    MaterialTheme {
        Box(Modifier.fillMaxSize()) {
            Wallpaper(wallpaperIndex)
            // затемнение снизу для читаемости; без alpha-слоя, чтобы не рисовать лишний буфер
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x8C000000))))
            )
            TopBar(
                weather = weather,
                notifCount = notifs.size,
                onNotifications = { showNotifs = true },
                onLauncherSettings = { showSettings = true },
                onScreens = { showScreens = true },
                onAssistant = { showAssistant = true },
                onVpn = {
                    val p = defaultVpn
                    if (p != null && apps.any { it.packageName == p }) launchApp(context, p)
                    else { launchAfterPick = true; showVpnPicker = true }
                },
                onVpnLong = { launchAfterPick = false; showVpnPicker = true },
                modifier = Modifier.align(Alignment.TopCenter).padding(horizontal = 32.dp, vertical = 28.dp),
            )
            Dock(
                apps = apps,
                favorites = favItems,
                onMenu = { menuPkg = it },
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 48.dp, bottom = 32.dp),
            )
            if (LauncherState.screensaver) Screensaver(wallpaperIndex, weather)
        }

        menuPkg?.let { pkg ->
            AppMenuDialog(pkg, apps, installedFavs, onFavorites = { updateFavs(it) }, onDismiss = { menuPkg = null })
        }
        if (showSettings) {
            LauncherSettingsDialog(
                settings = settings,
                onChange = {
                    settings = it
                    saveSettings(context, it)
                    SoundFx.configure(it.soundTheme, it.volume)
                    SoundFx.play(Sound.SELECT)
                },
                onAssistantKey = { showSettings = false; showKey = true },
                onDismiss = { showSettings = false },
            )
        }
        if (showNotifs) NotificationsDialog(notifs) { showNotifs = false }
        if (showScreens) ScreenPickerDialog(current = ScreenBridge.current(context)) { showScreens = false }
        if (showAssistant) AssistantDialog(apps) { showAssistant = false }
        if (showKey) DeepSeekKeyDialog { showKey = false }
        if (showVpnPicker) {
            VpnPickerDialog(
                apps = apps,
                vpnPackages = vpnPackages,
                current = defaultVpn,
                onPick = { pkg ->
                    defaultVpn = pkg
                    saveDefaultVpn(context, pkg)
                    showVpnPicker = false
                    SoundFx.play(Sound.FAVORITE)
                    val label = apps.find { it.packageName == pkg }?.label ?: pkg
                    if (launchAfterPick) launchApp(context, pkg)
                    else toast(context, context.getString(R.string.uni_vpn_set_toast, label))
                },
                onDismiss = { showVpnPicker = false },
            )
        }
    }
}
