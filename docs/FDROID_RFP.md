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

## Komentar susulan untuk RFP #4764 (siap tempel)

Hi, a status update on the scanner flags from the v0.1.5 scan. Since then the app has
shipped v0.1.7, v0.1.8 and v0.1.9; the current release is **v0.1.9 (versionCode 10)**.

Both scanner errors are fixed and can be checked against upstream checksums:

- `gradle/wrapper/gradle-wrapper.jar` sha256 `7a9ce74cff467ca1bf60a4fcd9f05185acceda4d0f382434d393e17864262c5d`
  is the official Wrapper JAR for Gradle 9.7.1 (https://gradle.org/release-checksums/).
- `gradle/wrapper/gradle-wrapper.properties` pins
  `distributionSha256Sum=acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a`
  (the official 9.7.1 binary ZIP checksum).
- `settings.gradle.kts` no longer references `org.gradle.toolchains.foojay-resolver`.

On the tracker flag: `opencensus-api` / `opencensus-proto` are pulled in by the Android
Gradle Plugin build tooling only. The built APK contains no `io/opencensus` classes
(checked by scanning `classes*.dex` of the v0.1.9 APK), so the "no trackers" claim holds.

Build recipe for this app: `gradle: - fdroid` (`./gradlew assembleFdroidRelease`). That
flavor ships without the in-app self-updater and without `REQUEST_INSTALL_PACKAGES`
(verified with aapt2 on the built APK).

Would it be possible to re-run the scanner against v0.1.9 so the stale labels are
cleared? Happy to provide anything else that helps.
