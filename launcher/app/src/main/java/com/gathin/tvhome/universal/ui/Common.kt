package com.gathin.tvhome.universal.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.gathin.tvhome.universal.data.Sound
import com.gathin.tvhome.universal.data.SoundFx
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun minuteNow() = System.currentTimeMillis() / 60_000L * 60_000L

/** Текущая минута (мс). Состояние меняется раз в минуту, поэтому интерфейс не перерисовывается зря. */
@Composable
fun rememberNow(): State<Long> {
    val now = remember { mutableStateOf(minuteNow()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L - System.currentTimeMillis() % 60_000L + 50)
            now.value = minuteNow()
        }
    }
    return now
}

/** Время в формате системы (12 или 24 часа — как настроено на телевизоре). */
fun formatTime(context: Context, millis: Long): String =
    android.text.format.DateFormat.getTimeFormat(context).format(Date(millis))

/** Дата на языке системы; skeleton, например "EEEdMMM" или "EEEEdMMMM". */
fun formatDate(millis: Long, skeleton: String): String {
    val locale = Locale.getDefault()
    val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, skeleton)
    return SimpleDateFormat(pattern, locale).format(Date(millis))
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PillButton(
    icon: ImageVector,
    color: Color = Color(0x66000000),
    badge: Int = 0,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Box {
        Surface(
            onClick = { SoundFx.play(Sound.SELECT); onClick() },
            onLongClick = onLongClick,
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = color,
                focusedContainerColor = color,
                contentColor = Color.White,
                focusedContentColor = Color.White,
            ),
            // The launcher look: the focused pill is the same colour, but gets a clear white ring.
            border = ClickableSurfaceDefaults.border(
                border = Border.None,
                focusedBorder = Border(
                    border = BorderStroke(3.dp, Color.White),
                    inset = 0.dp,
                    shape = RoundedCornerShape(50),
                ),
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.16f),
            modifier = Modifier
                .size(44.dp)
                .onFocusChanged { if (it.hasFocus) SoundFx.play(Sound.FOCUS) },
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
        if (badge > 0) {
            Box(
                Modifier.align(Alignment.TopEnd).size(18.dp).background(Color(0xFFE53935), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(badge.coerceAtMost(99).toString(), color = Color.White, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun InfoPill(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .background(Color(0x66000000), RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}
