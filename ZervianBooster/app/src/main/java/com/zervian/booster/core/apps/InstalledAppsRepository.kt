package com.zervian.booster.core.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.zervian.booster.data.model.AppEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Mendata app yang BISA DIBUKA dari launcher (queryIntentActivities dengan
 * CATEGORY_LAUNCHER). Ini sengaja dipilih dibanding minta permission
 * QUERY_ALL_PACKAGES (yang sejak Android 11 butuh justifikasi khusus ke Google
 * Play dan gampang ditolak) - hasil akhirnya identik dengan "app yang muncul
 * di home screen", yang memang itu yang relevan untuk "pilih game untuk diboost".
 */
class InstalledAppsRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    suspend fun listLaunchableApps(): List<AppEntry> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        resolved
            .asSequence()
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .filterNot { it.packageName == context.packageName }
            .map { ai ->
                AppEntry(
                    packageName = ai.packageName,
                    label = pm.getApplicationLabel(ai).toString(),
                    isLikelyGame = isLikelyGame(ai),
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun loadIcon(packageName: String): Drawable? = try {
        pm.getApplicationIcon(packageName)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    /**
     * ApplicationInfo.category (API 26+) diisi sendiri oleh developer app via
     * manifest `android:appCategory="game"` dan dipakai Play Store - jadi ini
     * sinyal resmi dari OS, bukan tebakan nama paket.
     */
    private fun isLikelyGame(ai: ApplicationInfo): Boolean =
        ai.category == ApplicationInfo.CATEGORY_GAME
}
