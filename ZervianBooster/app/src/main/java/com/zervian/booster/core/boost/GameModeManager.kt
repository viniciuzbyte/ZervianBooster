package com.zervian.booster.core.boost

import android.app.GameManager
import android.content.Context
import android.os.Build

enum class BoostProfile { BATTERY, STANDARD, PERFORMANCE }

sealed class BoostResult {
    data class Applied(val profile: BoostProfile, val viaSystemApi: Boolean) : BoostResult()
    data class Unsupported(val reason: String) : BoostResult()
}

/**
 * Zervian TIDAK bisa melakukan overclock CPU/GPU sungguhan - itu kewenangan
 * firmware vendor (seperti XArena bawaan Transsion/Infinix yang berjalan
 * sebagai app sistem). Yang Zervian lakukan di sini adalah memanggil
 * `android.app.GameManager`, API PUBLIK & RESMI dari Android (API 31+),
 * yang benar-benar meneruskan permintaan mode performa ke sistem operasi -
 * ini nyata dipakai, bukan hiasan UI saja.
 *
 * - GAME_MODE_PERFORMANCE: minta OS prioritaskan FPS/CPU-GPU utk game ini.
 * - GAME_MODE_BATTERY   : minta OS throttle utk hemat daya.
 * - GAME_MODE_STANDARD  : kembali ke default sistem.
 *
 * Android 10 & 11 TIDAK memiliki GameManager (baru ada di Android 12/API 31).
 * Untuk itu kita jujur memberi tahu pengguna bahwa di versi ini Zervian hanya
 * bisa melakukan housekeeping (trim memori, blokir notifikasi) tanpa klaim
 * "boost performa sistem" yang sebenarnya tidak bisa dijamin di versi tsb.
 *
 * KETERBATASAN PENTING (wajib dibaca, bukan cuma komentar basa-basi):
 * `setGameMode(packageName, mode)` untuk MENGATUR mode game MILIK APP LAIN
 * dilindungi permission `MANAGE_GAME_MODE`, yaitu permission level
 * "signature|privileged" - permission ini TIDAK BISA didapat app biasa yang
 * di-install dari Play Store/APK sideload, walau sudah minta izin run-time.
 * Hanya app sistem resmi OEM (misal Game Dashboard bawaan Samsung/Pixel/
 * Transsion) yang di-sign dengan sertifikat platform yang bisa memegangnya.
 * Jadi di HAMPIR SEMUA perangkat, pemanggilan ini akan selalu berakhir di
 * SecurityException di bawah - ini BUKAN bug/kasus tepi, ini perilaku normal
 * yang diharapkan untuk app pihak ketiga. Saya tetap sertakan kode ini karena
 * (a) ini cara resmi/satu-satunya API publik yang ada untuk niat ini, dan
 * (b) pada perangkat yang di-root + sudah diberi privileged permission lewat
 * Magisk module, ini akan benar-benar jalan. Untuk pengguna awam non-root,
 * Zervian harus jujur bilang "tidak didukung" dan jatuh ke housekeeping biasa.
 */
class GameModeManager(private val context: Context) {

    fun isSystemBoostSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    fun applyProfile(packageName: String, profile: BoostProfile): BoostResult {
        if (!isSystemBoostSupported()) {
            return BoostResult.Unsupported(
                "Android ${Build.VERSION.RELEASE} belum punya GameManager API (butuh Android 12+). " +
                    "Zervian tetap jalankan housekeeping (trim memori & DND) untuk game ini."
            )
        }

        val gm = context.getSystemService(Context.GAME_SERVICE) as? GameManager
            ?: return BoostResult.Unsupported("GameManager tidak tersedia di perangkat ini.")

        return try {
            val targetMode = when (profile) {
                BoostProfile.PERFORMANCE -> GameManager.GAME_MODE_PERFORMANCE
                BoostProfile.BATTERY -> GameManager.GAME_MODE_BATTERY
                BoostProfile.STANDARD -> GameManager.GAME_MODE_STANDARD
            }

            // getGameModeInfo mengembalikan mode yang didukung OEM untuk app target.
            val supportedModes = gm.getGameModeInfo(packageName)?.availableGameModes
            val reallySupported = supportedModes?.contains(targetMode) == true

            if (!reallySupported) {
                return BoostResult.Unsupported(
                    "Perangkat/OEM ini tidak mendaftarkan mode tersebut untuk game ini."
                )
            }

            gm.setGameMode(packageName, targetMode)
            BoostResult.Applied(profile, viaSystemApi = true)
        } catch (e: SecurityException) {
            // Ini hasil yang DIHARAPKAN pada app non-sistem/non-root - lihat dokumentasi
            // di atas kelas ini. Bukan berarti kode salah.
            BoostResult.Unsupported(
                "Sistem menolak: mengatur mode game untuk app lain butuh izin MANAGE_GAME_MODE " +
                    "yang hanya dipegang app sistem OEM. Zervian beralih ke housekeeping manual."
            )
        } catch (e: Exception) {
            BoostResult.Unsupported("Gagal menghubungi GameManager: ${e.message}")
        }
    }
}
