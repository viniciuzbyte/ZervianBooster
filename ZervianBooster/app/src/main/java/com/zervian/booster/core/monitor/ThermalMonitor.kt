package com.zervian.booster.core.monitor

import android.content.Context
import android.os.Build
import android.os.PowerManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Status thermal ASLI dari sistem Android, bukan simulasi.
 * PowerManager.getCurrentThermalStatus() / addThermalStatusListener tersedia
 * resmi sejak API 29 (Android 10) - inilah kenapa minSdk app ini 29.
 *
 * Tidak ada akses ke suhu derajat Celsius mentah tanpa root (itu kewenangan
 * vendor/HAL), yang diberikan OS hanya level keparahan (NONE..SHUTDOWN).
 * Kita tampilkan level ini apa adanya, tidak kita ubah jadi angka derajat palsu.
 */
enum class ThermalLevel(val label: String) {
    NONE("Normal"),
    LIGHT("Hangat"),
    MODERATE("Sedang"),
    SEVERE("Panas"),
    CRITICAL("Kritis"),
    EMERGENCY("Darurat"),
    SHUTDOWN("Mematikan Diri"),
    UNKNOWN("Tidak diketahui")
}

class ThermalMonitor(context: Context) {

    private val powerManager = context.applicationContext
        .getSystemService(Context.POWER_SERVICE) as PowerManager

    fun currentLevel(): ThermalLevel {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return ThermalLevel.UNKNOWN
        return mapStatus(powerManager.currentThermalStatus)
    }

    /** Emits setiap kali status thermal sistem berubah - real-time, bukan polling tebakan. */
    fun observe(): Flow<ThermalLevel> = callbackFlow {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            trySend(ThermalLevel.UNKNOWN)
            awaitClose { }
            return@callbackFlow
        }
        val listener = PowerManager.OnThermalStatusChangedListener { status ->
            trySend(mapStatus(status))
        }
        trySend(mapStatus(powerManager.currentThermalStatus))
        powerManager.addThermalStatusListener(listener)
        awaitClose { powerManager.removeThermalStatusListener(listener) }
    }

    private fun mapStatus(status: Int): ThermalLevel = when (status) {
        PowerManager.THERMAL_STATUS_NONE -> ThermalLevel.NONE
        PowerManager.THERMAL_STATUS_LIGHT -> ThermalLevel.LIGHT
        PowerManager.THERMAL_STATUS_MODERATE -> ThermalLevel.MODERATE
        PowerManager.THERMAL_STATUS_SEVERE -> ThermalLevel.SEVERE
        PowerManager.THERMAL_STATUS_CRITICAL -> ThermalLevel.CRITICAL
        PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalLevel.EMERGENCY
        PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalLevel.SHUTDOWN
        else -> ThermalLevel.UNKNOWN
    }
}
