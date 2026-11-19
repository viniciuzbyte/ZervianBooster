# Zervian — Game Performance Suite

Kotlin + Jetpack Compose. `minSdk 29` (Android 10) — `compileSdk`/`targetSdk 36` (Android 16, wajib Play Store mulai 31 Agt 2026).

## Upload ke GitHub
Penyebab paling umum project Android gagal/berat saat di-push: folder `build/`, `.gradle/`, dan `local.properties` ikut ter-commit (isinya cache besar & path lokal mesin kamu). Sudah saya tambahkan `.gitignore` untuk itu - pastikan kamu commit project ini **sebelum** pernah di-build sekali secara lokal, atau jalankan `git rm -r --cached build .gradle` dulu kalau sudah telanjur ke-commit.

Soal CI: saya tambahkan `.github/workflows/blank.yml` (nama file ini memang default GitHub untuk workflow baru) yang build APK otomatis tiap push, pakai `gradle/actions/setup-gradle` - action ini resmi dari Gradle dan menyediakan Gradle sendiri di runner, jadi **tidak butuh `gradlew`/`gradle-wrapper.jar` sudah ter-commit**. Saya sengaja TIDAK sertakan `gradlew`/`gradle-wrapper.jar` di zip ini karena jar itu file biner yang tidak bisa saya buat valid tanpa akses internet di sandbox saya - kalau saya paksa buat, hasilnya rusak dan malah bikin bingung.

Untuk build **lokal** di Android Studio, buka project ini lalu klik "Sync Project with Gradle Files" - Android Studio otomatis menawarkan generate wrapper asli untuk kamu (atau Tools > "Create Gradle Wrapper" kalau tidak muncul).

## Cara buka
1. Buka folder ini di Android Studio (minimal **Meerkat 2024.3.1+** supaya kompatibel API 36).
2. Sync Gradle (butuh koneksi internet untuk download dependency pertama kali — belum sempat saya compile di sini karena sandbox ini tidak ada akses jaringan/Android SDK).
3. Tambahkan ikon app nyata di `res/mipmap-*` (saat ini pakai `@mipmap/ic_launcher` default, ganti dengan aset final kamu / generate lewat Image Asset Studio).
4. Run ke device/emulator Android 10+.

## Yang SUDAH real di versi ini
- **RAM** — `ActivityManager.getMemoryInfo()`, data asli sistem.
- **Suhu** — `PowerManager` thermal status API (Android 10+), data asli, bukan simulasi.
- **CPU** — baca `/proc/stat`; jujur ditampilkan **"N/A"** kalau OS blokir aksesnya (umum terjadi di Android 8+ non-root) — tidak dikarang.
- **GPU** — sengaja TIDAK ditampilkan sebagai angka. Tidak ada API publik untuk itu tanpa root; menampilkan gauge palsu persis yang kamu minta untuk dihindari.
- **Tambah game** — diambil dari `queryIntentActivities` (app yang muncul di launcher), tanpa permission sensitif `QUERY_ALL_PACKAGES`.
- **Boost "Performance/Battery/Standard"** — memanggil `android.app.GameManager` (API publik resmi Android 12+/API 31).

## Keterbatasan yang WAJIB kamu tahu (baca `GameModeManager.kt`)
`GameManager.setGameMode()` untuk app lain dilindungi permission `MANAGE_GAME_MODE` — permission **signature|privileged**, tidak bisa didapat app biasa dari Play Store/APK sideload. Di hampir semua HP non-root, panggilan ini akan gagal dengan `SecurityException` — itu **perilaku normal**, bukan bug. Saat itu terjadi, app otomatis jatuh ke `RealisticBoostActions` (trim proses background via `KILL_BACKGROUND_PROCESSES`, dan Do Not Disturb manual) yang memang bisa dijalankan app biasa.

Konsekuensinya: tombol "PERFORMANCE/BATTERY" akan menampilkan pesan jujur soal ini ke user di hampir semua device, bukan klaim sukses palsu.

Kalau kamu memang mau boost CPU/GPU level kernel beneran seperti XArena, itu hanya mungkin dengan:
- jadi app sistem pre-install OEM (butuh kerja sama vendor), atau
- target device rooted + Magisk module yang grant privileged permission.

## Belum dikerjakan (belum masuk prioritas chat ini, tapi kamu sudah minta sebelumnya)
- **Anti-tamper / anti-clone / enkripsi APK** — proteksi signature check, deteksi root/debugger/Frida, deteksi repack dari MT Manager. Ini legal & standar (bukan malware), siap saya bangun di iterasi berikutnya, tinggal bilang.
- Ikon & splash asset final, dark/light toggle (saat ini always-dark by design).
