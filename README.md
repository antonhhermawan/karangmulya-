# MARKAS RAKYAT DESA KARANGMULYA
**"Pelayanan Mudah, Informasi Terbuka, Desa Bersama Rakyat."**

Aplikasi pelayanan publik digital dan tata kelola pemerintahan Desa Karangmulya berbasis Android modern (Kotlin, Jetpack Compose Material 3, Firebase Authentication & Cloud Firestore Enterprise).

---

## 1. Fitur Utama Sistem

### A. Interface Warga
1. **Autentikasi & Kartu Warga Digital**: Login aman menggunakan Google Sign-In (`CredentialManager`), dilengkapi Kartu Warga Digital ber-QR Code, status verifikasi kependudukan, serta fitur pelindung privasi NIK & Nomor KK (masking `3205******0001`).
2. **Pengajuan Surat Administrasi Online**: Pengajuan SKD, SKU, SKTM, Pengantar SKCK, Surat Kelahiran/Kematian, dan Pengantar Nikah lengkap dengan unggah lampiran dokumen (`PickVisualMedia`), nomor registrasi unik (`SRT-2026-XXXX`), serta **Cetak / Unduh Surat Resmi dalam format PDF**.
3. **Pengaduan Masyarakat (Foto + Lokasi GPS)**: Pelaporan masalah infrastruktur, drainase, PJU, dan pelayanan publik disertai foto bukti dan koordinat GPS Desa Karangmulya.
4. **Aspirasi, Kritik & Saran**: Penyaluran usulan pembangunan untuk Musrenbangdes lengkap dengan fitur dukungan warga (upvote) dan tanggapan resmi BPD/Pemdes.
5. **Keterbukaan Informasi, Agenda & Pembangunan Desa**: Publikasi berita desa, pengumuman resmi, jadwal kegiatan (Posyandu, Musrenbangdes), serta transparansi pagu anggaran APBDes dan persentase realisasi fisik proyek.
6. **Pelacakan Status QR Code, Notifikasi & Pusat Bantuan**: Cek status permohonan menggunakan Nomor Registrasi / QR Code, riwayat notifikasi otomatis, dan kontak darurat perangkat desa.

### B. Interface Staff Desa
1. **Dashboard Statistik Pelayanan**: Ringkasan eksekutif real-time dari database (jumlah warga terverifikasi, surat diproses/selesai, pengaduan, aspirasi, dan progres fisik pembangunan).
2. **Verifikasi & Pemrosesan Surat Online**: Pemeriksaan berkas permohonan warga, pemberian catatan perangkat desa, pembaruan status (`DIAJUKAN`, `DIVERIFIKASI`, `DIPROSES`, `SELESAI`, `DITOLAK`), dan penerbitan surat PDF.
3. **Manajemen Pengaduan & Aspirasi**: Tindak lanjut laporan warga beserta tanggapan resmi tim respon desa.
4. **Manajemen Data Warga, Verifikasi & Hak Akses Staff (RBAC)**: Pengelolaan direktori kependudukan, verifikasi warga baru, serta pengaturan role perangkat desa (`KEPALA_DESA`, `SEKRETARIS_DESA`, `KAUR_UMUM`, `STAFF_PELAYANAN`).
5. **Laporan PDF / Excel (CSV) & Audit Log**: Ekspor laporan pelayanan resmi dalam format PDF dan Excel (`.csv`), manajemen publikasi desa, serta pencatatan Audit Log aktivitas sistem yang bersifat *immutable*.

---

## 2. Arsitektur, Keamanan & Database
- **Frontend & UI**: Kotlin, Jetpack Compose (Material 3), Adaptive Layout (`NavigationBar` untuk HP & `NavigationRail` untuk Tablet/Layar Lebar), Typography lokal *Plus Jakarta Sans* & *Space Grotesk*.
- **Backend & Database**: Firebase Authentication (Google Sign-In via Jetpack Credential Manager) & Cloud Firestore Enterprise.
- **Zero-Trust Security Rules (`firestore.rules`)**:
  - Setiap koleksi (`/citizen_records`, `/letter_requests`, `/complaints`, `/aspirations`, `/village_posts`, `/notifications`, `/audit_logs`) dilindungi validasi skema ketat (`isValid[Entity]`) dan isolasi multi-pengguna (`userId == request.auth.uid || isAdmin()`).
  - Perlindungan PII (NIK & Nomor KK 16 digit) dari akses lintas pengguna.
  - Audit Log bersifat *append-only* (`allow update, delete: if false`).

---

## 3. Panduan Instalasi & Deployment
1. **Menjalankan di AI Studio / Android Studio**:
   - Pastikan file konfigurasi `app/google-services.json` dan `app/src/main/res/values/firebase_applet_config.xml` tersedia.
   - Jalankan build debug:
     ```bash
     gradle assembleDebug
     ```
2. **Pengujian Keamanan Firestore Rules (Lokal)**:
   - Jalankan pengujian JavaScript Rules (Tier 1):
     ```bash
     FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
     ```
   - Jalankan pengujian integrasi Robolectric Kotlin (Tier 2):
     ```bash
     gradle :app:testDebugUnitTest --tests '*Rule*'
     ```
3. **Deployment APK / AAB**:
   - Gunakan menu **Export / Build APK / AAB** pada Google AI Studio atau jalankan `gradle bundleRelease` untuk distribusi produksi.
