# Pengajuan ke IzzyOnDroid

Tujuan: bikin LokoTube terindeks F-Droid/IzzyOnDroid supaya Play Protect cepat "kenal"
tanda tangannya (hilangkan prompt "pindai dulu"), dan tersedia lewat klien F-Droid.

## Hasil cek kepatuhan (App Inclusion Policy)

- Lisensi FOSS: GPL-3.0 (LICENSE + COPYRIGHT ada) — LULUS
- Repo sumber publik: github.com/monzterz/LokoTube — LULUS
- Dependensi non-FOSS (Firebase/GMS/Play Services): TIDAK ADA — LULUS
- APK rilis resmi ter-attach di GitHub Releases: ADA (LokoTube-0.1.3.apk) — LULUS

SATU CATATAN (mungkin ditegur maintainer):
- Aplikasi punya self-updater (Updates.kt) + izin REQUEST_INSTALL_PACKAGES di manifest.
  IzzyOnDroid sengaja menandai izin ini untuk app yang bukan package manager — menandakan
  ada self-updater di dalam. Standar ekosistem F-Droid biasanya minta self-updater dihapus
  (repo yang handle update).
  OPSI: tambahkan build flavor "fdroid" yang mematikan updater + buang izinnya (rekomendasi),
  atau sekalian ajukan apa adanya sambil tulis di issue bahwa updater bisa dimatikan.

## Cara mengajukan (file issue)

1. Buka: https://codeberg.org/IzzyOnDroid/repodata/issues/new
2. Pakai template "App request" (jangan kosongi template).
3. Isi field penting:
   - Name: LokoTube
   - Package ID: com.lokotube.app
   - Source code: https://github.com/monzterz/LokoTube
   - Upstream/basis: https://github.com/cnyel9/leplay (LePlay, Levi Setiadi)
   - License: GPL-3.0-only
   - APK/rilis: GitHub Releases https://github.com/monzterz/LokoTube/releases
   - Deskripsi: pakai isi fastlane/metadata/en-US/full_description.txt (sudah disiapkan).
4. Submit. Izzy akan cek APK + build; kalau hijau, masuk repo dalam beberapa hari.

## File yang sudah disiapkan (di repo ini)

- fastlane/metadata/en-US/title.txt          -> "LokoTube"
- fastlane/metadata/en-US/short_description.txt
- fastlane/metadata/en-US/full_description.txt

Kalau mau bikin flavor fdroid (opsi terbaik, minor): matikan Updates.kt lewat gradle buildConfig
field (mis. UPDATER_ENABLED=false) + hapus REQUEST_INSTALL_PACKAGES untuk flavor itu.