# Draft Request For Packaging (RFP) — F-Droid

Kirim ke: https://gitlab.com/fdroid/rfp/issues/new
Setelah issue dibuat, tautkan issue IzzyOnDroid #848 di komentar (biar mereka "pick up").

## Isi RFP

**Package ID:** com.lokotube.app

**App name:** LokoTube

**Summary:**
LokoTube is a free, open-source Android client for streaming YouTube video and
music without ads, tracking, or a Google account.

**Description:**
LokoTube is a free, open-source Android client for streaming YouTube video and
music without ads, tracking, or a Google account. It is a fork of LePlay by Levi
Setiadi (github.com/cnyel9/leplay), rebranded and extended.

Features: ad-free playback with SponsorBlock and Return YouTube Dislike,
background and picture-in-picture playback, music mode with an auto-radio queue,
video/audio/MP3 downloads, local playlists with import/export, an equalizer with
volume boost, a home-screen mini-player widget, video chapters, in-playlist
search, bulk downloads, and Indonesian/English translations. Built on
NewPipeExtractor (no Google Play Services required), GPL-3.0, no ads, no trackers.

Build note: the app has two build flavors — please build the `fdroid` flavor
(./gradlew assembleFdroidRelease), which ships without the in-app self-updater
and without the REQUEST_INSTALL_PACKAGES permission.

**Source code:** https://github.com/monzterz/LokoTube

**License:** GPL-3.0-only

**Build flavor (PENTING):** `fdroid`
Build command: `./gradlew assembleFdroidRelease`
- Flavor `fdroid` dinonaktifkan self-updater + tanpa izin REQUEST_INSTALL_PACKAGES
  (BuildConfig.UPDATES_ENABLED=false).
- Fastlane metadata ada di fastlane/metadata/android/en-US/ (teks + ikon + screenshot).

**Upstream / related:** https://github.com/cnyel9/leplay (LePlay, oleh Levi Setiadi)

## Catatan untuk maintainer
- Aplikasi punya flavor `github` (dengan self-updater) untuk distribusi GitHub, dan
  flavor `fdroid` (bersih) — gunakan flavor `fdroid`.
- IzzyOnDroid menolak karena kebijakan LLM (mereka lihat "Substantial" AI usage);
  F-Droid belum punya kebijakan itu. Kami transparan: basis LePlay tulis tangan
  Levi Setiadi, modifikasi LokoTube dikerjakan dengan bantuan AI (Claude/Hermes),
  direview + dites manual oleh pengembang manusia.
- Referensi: issue IzzyOnDroid #848 (APK scan result: 0 offending libraries).