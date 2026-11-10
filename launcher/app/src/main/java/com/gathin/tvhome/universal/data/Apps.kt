package com.gathin.tvhome.universal.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap

/** Только название и пакет: список строится быстро и почти не занимает памяти. */
data class AppItem(val label: String, val packageName: String)

/** Последний загруженный список: при возврате на главный экран показывается мгновенно. */
object AppsCache {
    @Volatile var apps: List<AppItem> = emptyList()
}

fun loadApps(context: Context): List<AppItem> {
    val pm = context.packageManager
    val leanback = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
    val regular = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return (pm.queryIntentActivities(leanback, 0) + pm.queryIntentActivities(regular, 0))
        .filter { it.activityInfo.packageName != context.packageName }
        .distinctBy { it.activityInfo.packageName }
        .map { AppItem(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
        .sortedBy { it.label.lowercase() }
        .also { AppsCache.apps = it }
}

fun launchApp(context: Context, packageName: String) {
    val pm = context.packageManager
    val intent = pm.getLeanbackLaunchIntentForPackage(packageName)
        ?: pm.getLaunchIntentForPackage(packageName)
        ?: return
    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/**
 * Иконки грузятся только для видимых карточек и кэшируются (до ~6 МБ).
 * Размер подбирается под плотность экрана: от 224x126 (около 113 КБ) до 320x180.
 */
object IconCache {
    private val cache = object : LruCache<String, ImageBitmap>(6 * 1024) { // размер в КБ
        override fun sizeOf(key: String, value: ImageBitmap): Int =
            (value.asAndroidBitmap().byteCount / 1024).coerceAtLeast(1)
    }

    fun get(pkg: String): ImageBitmap? = cache.get(pkg)

    fun load(context: Context, pkg: String, W: Int, H: Int): ImageBitmap? {
        cache.get(pkg)?.let { return it }
        val pm = context.packageManager
        return try {
            val component = (pm.getLeanbackLaunchIntentForPackage(pkg) ?: pm.getLaunchIntentForPackage(pkg))?.component
            val banner: Drawable? =
                component?.let { runCatching { pm.getActivityBanner(it) }.getOrNull() }
                    ?: runCatching { pm.getApplicationBanner(pkg) }.getOrNull()
            val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            if (banner != null) {
                banner.setBounds(0, 0, W, H)
                banner.draw(canvas)
            } else {
                // у приложения нет баннера: квадратная иконка по центру тёмной плашки
                val icon = pm.getApplicationIcon(pkg)
                canvas.drawColor(0xFF2A2F3A.toInt())
                val s = 96
                icon.setBounds((W - s) / 2, (H - s) / 2, (W + s) / 2, (H + s) / 2)
                icon.draw(canvas)
            }
            bmp.asImageBitmap().also { cache.put(pkg, it) }
        } catch (_: Exception) {
            null
        }
    }
}
