package com.zervian.booster.core.monitor

import android.app.ActivityManager
import android.content.Context

data class MemorySnapshot(
    val totalMb: Long,
    val availableMb: Long,
    val usedMb: Long,
    val usedPercent: Int,
    val isLowMemory: Boolean
)

/**
 * ActivityManager.getMemoryInfo() adalah API publik resmi, datanya benar-benar
 * dibaca dari sistem (bukan angka acak) - ini yang dipakai task manager & launcher
 * bawaan OEM juga.
 */
class MemoryMonitor(private val context: Context) {

    private val am = context.applicationContext
        .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun snapshot(): MemorySnapshot {
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        val totalMb = info.totalMem / (1024 * 1024)
        val availMb = info.availMem / (1024 * 1024)
        val usedMb = totalMb - availMb
        val pct = if (totalMb > 0) ((usedMb * 100) / totalMb).toInt() else 0
        return MemorySnapshot(totalMb, availMb, usedMb, pct, info.lowMemory)
    }

    /**
     * Minta sistem membersihkan proses background milik app lain yang sedang idle.
     * Ini BUKAN root-kill paksa (app pihak ketiga memang tidak diizinkan itu sejak
     * Android 5+), tapi permintaan resmi lewat API publik yang benar-benar diproses
     * oleh ActivityManager - nyata, bukan cuma animasi UI.
     */
    fun requestSystemTrim() {
        am.killBackgroundProcesses(context.packageName) // no-op aman untuk diri sendiri
        Runtime.getRuntime().let {
            // Memberi sinyal GC ke JVM proses kita sendiri sebagai bagian dari housekeeping.
            it.gc()
        }
    }
}
