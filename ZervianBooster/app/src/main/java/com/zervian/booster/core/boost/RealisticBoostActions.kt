package com.zervian.booster.core.boost

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings

/**
 * Ini bagian "boost" yang betul-betul bisa dijalankan app pihak ketiga biasa
 * (tanpa root, tanpa permission privileged), dipakai sebagai pelengkap/fallback
 * saat GameModeManager tidak didukung (lihat GameModeManager.kt untuk kenapa).
 */
class RealisticBoostActions(private val context: Context) {

    private val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Minta sistem membersihkan proses background app lain yang statusnya
     * sudah "cached/idle". Permission KILL_BACKGROUND_PROCESSES bersifat normal
     * (auto-grant), efeknya setara "clear dari recent apps" - nyata, tapi
     * sistem tetap yang memutuskan proses mana yang aman dimatikan (app yang
     * sedang foreground/punya foreground service tidak akan tersentuh).
     */
    fun trimBackgroundOf(targetPackage: String) {
        am.killBackgroundProcesses(targetPackage)
    }

    fun isDndAccessGranted(): Boolean = nm.isNotificationPolicyAccessGranted

    /** Harus user approve manual sekali di Settings - ini standar OS, tidak bisa dilewati. */
    fun requestDndAccessIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)

    /** Aktif/nonaktifkan Do Not Disturb total selama sesi game - beneran memblokir notifikasi masuk. */
    fun setDndDuringGame(enabled: Boolean) {
        if (!isDndAccessGranted()) return
        nm.setInterruptionFilter(
            if (enabled) NotificationManager.INTERRUPTION_FILTER_NONE
            else NotificationManager.INTERRUPTION_FILTER_ALL
        )
    }
}
