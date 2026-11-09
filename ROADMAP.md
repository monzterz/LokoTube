# LokoTube — Roadmap & Rencana Pengembangan

Fork dari LePlay (github.com/cnyel9/leplay) oleh Levi Setiadi (@cnyel9), GPL-3.0.
Dikembangkan lanjutan oleh Lokotre (Rizal). v0.1.5 (versionCode 6).

## RENCANA BESOK (urut prioritas)

1. Blokir channel — tekan-tahan video → "Jangan rekomendasikan channel ini" → filter dari beranda/terkait. (effort kecil-menengah)
2. DeArrow (judul dulu) — query sponsor.ajay.app per video → timpa judul clickbait. Thumbnail menyusul.
3. Skip silence (musik) — lewati hening otomatis saat mode musik. (menengah)
4. Bagikan dengan timestamp — link video mulai dari menit tertentu (t=…).
5. Pantau F-Droid RFP (gitlab.com/fdroid/rfp) + issue IzzyOnDroid #848 — balas kalau ada pertanyaan.

## SARAN PENGEMBANGAN (berkaca ke aplikasi lain)

### Audio / Musik
- Skip silence — lewati bagian senyap (ada di RiMusic/Tubular). Menengah.
- Normalisasi audio (ReplayGain) — samakan keras antar lagu di mode musik (RiMusic/InnerTune). Menengah.
- Subtitle .srt — unduh takarir sebagai file (Seal/NewPipe). Kecil-menengah.

### Konten & antarmuka
- DeArrow — anti-clickbait judul/thumbnail (LibreTube punya bawaan). Menengah.
- Blokir channel ("jangan rekomendasikan") — (NewPipe/LibreTube). Kecil-menengah.
- A-B repeat — ulang bagian tertentu untuk tutorial/musisi. Kecil.
- Offset subtitle — geser timing takarir kalau tak sinkron (standar pemutar video). Kecil.
- Daftar hitam kata kunci judul — sembunyikan video dengan kata tertentu. Kecil.

### Privasi & backend
- Backend alternatif (Piped/Invidious) — sumber di pengaturan, tahan rate-limit + privasi (LibreTube). Besar.
- Pengaturan region/konten per negara — sudah sebagian (effectiveCountryCode), bisa dipermak.

### Distribusi & data
- Notifikasi video baru dari channel subscribe (cek latar/WorkManager). Menengah-besar (rewel di HyperOS).
- Backup/ekspor pengaturan penuh (lanjutan ekspor langganan/playlist). Kecil-menengah.

### Besar (tunda)
- Versi desktop (Windows), Android Auto, Chromecast — effort besar, evaluasi belakangan.

## SELESAI

- Branding teal + ikon LOKO + About "Levi Setiadi mod by Lokotre" + pembersihan nama/namespace (com.lokotube.app, nol jejak LePlay).
- Donasi Rizal (DANA/Superbank), keystore release, rilis v0.1.0–v0.1.5 + CI otomatis.
- Auto-sync upstream LePlay (workflow harian + manual).
- Tema Flat Light + Liquid Glass, terjemahan Indonesia, import/ekspor langganan+playlist.
- Fix player LePlay (tap/fullscreen/instant/0.0.9), prefetch + pre-buffer audio.
- Chapter, unduh massal, saklar radio/autoplay, cari di playlist.
- Equalizer preset + volume boost (LoudnessEnhancer).
- SponsorBlock, Return YouTube Dislike, lirik, sleep timer, PiP/background, TV support.
- Widget mini player (prev/play/next), onboarding layar pertama, tombol Bagikan LokoTube, buka tautan YouTube (deep-link).
- Flavor build `github` (updater) + `fdroid` (tanpa updater untuk F-Droid).
- Pengajuan F-Droid (RFP) + issue IzzyOnDroid #848 (decline karena kebijakan LLM, scan APK bersih).