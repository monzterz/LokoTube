# Pengajuan LokoTube ke IzzyOnDroid

Tujuan: bikin LokoTube terindeks F-Droid/IzzyOnDroid supaya Play Protect cepat "kenal"
tanda tangannya (hilangkan prompt "pindai dulu") dan tersedia lewat klien F-Droid.

## Cara mengajukan

1. Buka: https://codeberg.org/IzzyOnDroid/repodata/issues/new
   (pilih template "App Inclusion Request").
2. Isi form mengikuti contekan di bawah.
3. Submit. Izzy akan scan APK (`needs/apk-scan`); kalau hijau, masuk repo.

## Cek kepatuhan (sudah kucek)

- Lisensi FOSS: GPL-3.0 (LICENSE + COPYRIGHT ada) — LULUS
- Repo publik: github.com/monzterz/LokoTube — LULUS
- Firebase/GMS/Play Services: TIDAK ADA — LULUS
- APK rilis resmi di GitHub Releases — ADA
- Fastlane metadata di repo (fastlane/metadata/en-US/) — ADA
- CATATAN: ada self-updater (Updates.kt) + izin REQUEST_INSTALL_PACKAGES. Tulis di
  "Further Notices" bahwa updater bisa dimatikan bila diminta.

## CONTekan pengisian form (salin per kolom)

Judul:
  [AppRequest] LokoTube

Guidelines (centang):
  [x] I am the developer of the app.
  [x] The app complies with the App Inclusion Policy.
  [x] The app is not already listed in the repo or issue tracker.
  [x] The Fastlane folder is available in the app's repo.

Link to the source code:
  https://github.com/monzterz/LokoTube

Link to app in another app store:
  (kosongkan, atau) https://github.com/monzterz/LokoTube/releases

License used:
  GPL-3.0-only

Categories (max 5):
  Multimedia, Internet

Summary (1-2 kalimat):
  LokoTube streams YouTube video and music without ads or a Google account. It is a
  free, open-source fork of LePlay by Levi Setiadi, rebranded and extended.

Description:
  (salin isi fastlane/metadata/en-US/full_description.txt)

Build instructions:
  Prasyarat: JDK 21 dan Android SDK (platform android-37, build-tools 36.1.0).

    git clone https://github.com/monzterz/LokoTube.git
    cd LokoTube
    ./gradlew assembleRelease

  APK rilis yang ditandatangani ada di app/build/outputs/apk/release/, dan rilis resmi
  ter-attach di GitHub Releases: https://github.com/monzterz/LokoTube/releases

AI Tools Usage (jujur, sesuaikan bila perlu):
  Assistance Level: Substantial – Used throughout development
  AI Tool(s): Claude (Hermes Agent)
  What did the tools help with: feature implementation, debugging, Gradle/CI setup,
    code review. Aplikasi dasar adalah LePlay karya Levi Setiadi (manusia); modifikasi
    LokoTube dikerjakan dengan bantuan AI.
  AI Accountability:
    [x] The human developer(s) reviewed and edited all AI-generated outputs
    [x] The human developer(s) ran manual tests and manually verified all changes

Further Notices:
  LokoTube adalah fork dari LePlay (https://github.com/cnyel9/leplay) oleh Levi Setiadi,
  berlisensi GPL-3.0, dilanjutkan sebagai LokoTube oleh Lokotre. Aplikasi memiliki
  pengecek versi bawaan yang mengambil rilis dari GitHub; bila tim IzzyOnDroid meminta,
  updater tersebut bisa dinonaktifkan lewat build tanpa fitur itu.