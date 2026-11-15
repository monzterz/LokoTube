# Draft komentar balasan — F-Droid RFP #4764

Tempel di: https://gitlab.com/fdroid/rfp/-/work_items/4764 (butuh login GitLab)

---

Hi, updating here with the fixes for the scanner flags from the v0.1.5 scan.
The current release is v0.1.6 (versionCode 7), which already ships all of these:

1. `scanner-error` ("usual suspect `org.gradle.toolchains.foojay-resolver`") —
   removed the foojay-resolver plugin from `settings.gradle.kts`.

2. `insecure-gradlew` ("gradle-wrapper.jar cannot be verified as free software") —
   regenerated `gradle/wrapper/gradle-wrapper.jar` to the official Gradle wrapper.

3. "missing distributionSha256Sum" —
   pinned it in `gradle/wrapper/gradle-wrapper.properties`
   (`acd53f1edaf02f1a8ff99879f8a34b302661a057d9b063ae9e35b552f804d20a` for
   Gradle 9.7.1).

Re the `trackers` flag (`io.opencensus:opencensus-api` / `opencensus-proto`):
these are pulled transitively by the Android Gradle Plugin build tooling, not by
the app itself — `./gradlew dependencyInsight` shows no such dependency and they
are not included in the built APK, so the "no trackers" claim in the description
holds. Happy to re-verify this on a fresh scan.

Source: https://github.com/monzterz/LokoTube
The `fdroid` flavor is unchanged (`./gradlew assembleFdroidRelease` — no in-app
self-updater, no REQUEST_INSTALL_PACKAGES permission).