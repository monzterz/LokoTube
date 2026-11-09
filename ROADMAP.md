# LokoTube — Roadmap

Fork dari [LePlay](https://github.com/cnyel9/leplay) oleh Levi Setiadi (@cnyel9), GPL-3.0.
Dikembangkan lanjutan oleh Lokotre (Rizal).

## Selesai

### FASE 1 — Branding
- [x] Aksen merah → teal, window colors, logo top bar
- [x] Support Developer → DANA 0856-8560-110 / Superbank 000009973561 (Rizal)
- [x] About "Levi Setiadi mod by Lokotre" (tanpa link), COPYRIGHT/README atribusi
- [x] Icon launcher LOKO (transparan) + docs/icon GitHub + rootProject.name

### FASE 2 — Rilis
- [x] Keystore release, rilis v0.1.0 (signed + asset)

### FASE 3 — Otomasi
- [x] GitHub Actions: debug tiap push, release signed tiap tag
- [x] Auto-sync upstream LePlay (harian + manual dispatch; issue saat konflik)

### Fitur pengalaman (mobile)
- [x] Tema Flat Light + Liquid Glass (toggle)
- [x] Terjemahan Bahasa Indonesia lengkap
- [x] Import/ekspor langganan (CSV Takeout) + playlist lokal (JSON)
- [x] Fix player dari LePlay (tap/fullscreen, restore, instant, 0.0.9) via auto-sync
- [x] Prefetch stream + pre-buffer audio (pindah lagu berurutan instan)
- [x] Chapter/titik waktu video (parse deskripsi + lompat)
- [x] Unduh massal (multi-select + peringatan ukuran)
- [x] Saklar radio/autoplay (putar satu lagu & berhenti)
- [x] Cari di dalam playlist
- [x] Equalizer (preset gain-band, bebas preset bawaan perangkat)
- [x] Form umpan-balik (buka GitHub issues)

## Backlog (belum dijadwalkan)
- [ ] Widget layar utama (pencarian + kontrol mini player)
- [ ] Notifikasi video/rilis baru dari channel yang di-subscribe (cek latar)
- [ ] Versi desktop (Windows)
- [ ] Dukungan Chromecast (effort besar — extractor belum siap)