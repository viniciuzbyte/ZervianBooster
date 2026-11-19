package com.zervian.booster.core.monitor

import java.io.RandomAccessFile

sealed class CpuReading {
    data class Value(val usedPercent: Int) : CpuReading()
    object Unavailable : CpuReading()
}

/**
 * PENTING - baca ini sebelum pakai:
 * Sejak Android 8 (API 26), kernel Linux Android membatasi akses /proc/stat
 * untuk app pihak ketiga via SELinux (hanya app sistem/root yang boleh baca
 * statistik CPU seluruh device). Di sebagian besar perangkat modern (terutama
 * stock Android, bukan ROM custom/rooted), pembacaan ini akan GAGAL.
 *
 * Kelas ini TIDAK mengarang angka saat gagal - dia mengembalikan
 * CpuReading.Unavailable, dan UI WAJIB menampilkan itu apa adanya
 * ("Butuh akses sistem") alih-alih gauge persen palsu seperti di app
 * booster abal-abal pada umumnya.
 *
 * Kalau sukses baca (beberapa custom ROM/OEM masih mengizinkan), datanya
 * memang real dari /proc/stat.
 */
class CpuEstimator {

    private var prevIdle = 0L
    private var prevTotal = 0L

    fun read(): CpuReading {
        return try {
            RandomAccessFile("/proc/stat", "r").use { reader ->
                val load = reader.readLine()
                val toks = load.split(" ").filter { it.isNotBlank() }
                // cpu  user nice system idle iowait irq softirq steal
                val idle = toks[4].toLong() + toks[5].toLong()
                val total = toks.drop(1).take(8).sumOf { it.toLong() }

                if (prevTotal == 0L) {
                    prevIdle = idle; prevTotal = total
                    return CpuReading.Unavailable // butuh 2 sample, belum ada baseline
                }

                val deltaIdle = idle - prevIdle
                val deltaTotal = total - prevTotal
                prevIdle = idle; prevTotal = total

                if (deltaTotal <= 0) return CpuReading.Unavailable
                val usedPct = (100 * (deltaTotal - deltaIdle) / deltaTotal).toInt()
                CpuReading.Value(usedPct.coerceIn(0, 100))
            }
        } catch (e: Exception) {
            // Akses ditolak SELinux / file tidak ada / format tak terduga -> jujur bilang tak tersedia
            CpuReading.Unavailable
        }
    }
}
