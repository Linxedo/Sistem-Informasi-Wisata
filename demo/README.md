# Sistem Informasi Eksplorasi Wisata
## Aplikasi Desktop Native Java dengan JavaFX & PostgreSQL
### Kelompok B6

---

## 📋 DESKRIPSI PROYEK

Aplikasi desktop untuk merencanakan perjalanan wisata di Indonesia dengan fitur:
- **Login & Authentication** - Validasi user dari database PostgreSQL
- **Dashboard Destinasi** - Browse destinasi wisata dengan pencarian pintar
- **Itinerary Planner** - Rencanakan perjalanan dengan kalkulasi biaya otomatis
- **Dark Modern UI** - Tema gelap profesional dengan JavaFX & CSS custom
- **Strong Error Handling** - Try-catch di setiap database operation

---

## ⚙️ REQUIREMENTS

| Komponen | Versi | Download |
|----------|-------|----------|
| Java JDK | 11+ | [oracle.com/java](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) |
| Maven | 3.8+ | [maven.apache.org](https://maven.apache.org/download.cgi) |
| PostgreSQL | 12+ | [postgresql.org](https://www.postgresql.org/download/windows/) |
| JavaFX | 13+ | (otomatis via Maven) |

---

## 🚀 QUICK START

### 1. Setup Database (PostgreSQL)

```bash
# Buka psql
psql -U postgres

# Buat database
CREATE DATABASE wisata_db;
\c wisata_db

# Jalankan script SQL
\i 'C:/Users/PC/Documents/DesktopProject/demo/database/schema.sql'

# Verifikasi
\dt
```

### 2. Build & Run Aplikasi

```bash
cd C:\Users\PC\Documents\DesktopProject\demo

# Compile
mvn clean compile

# Run
mvn clean javafx:run
```

### 3. Login

Gunakan credentials sample:
- **Username:** admin | **Password:** admin | **Role:** Admin
- **Username:** wisatawan01 | **Password:** admin | **Role:** Wisatawan

---

## 📁 PROJECT STRUCTURE

```
demo/
├── src/main/java/com/example/
│   ├── App.java                    ← Entry point
│   ├── DatabaseHelper.java         ← JDBC connection
│   ├── LoginController.java        ← Login logic
│   ├── DashboardController.java    ← Destinasi list & search
│   ├── ItineraryController.java    ← Itinerary management
│   ├── User.java                   ← Model: User
│   ├── Destinasi.java              ← Model: Destinasi
│   ├── Itinerary.java              ← Model: Itinerary
│   └── ItineraryDetail.java        ← Model: Itinerary detail
│
├── src/main/resources/com/example/
│   ├── login.fxml                  ← Login UI
│   ├── dashboard.fxml              ← Dashboard UI
│   ├── itinerary.fxml              ← Itinerary UI
│   └── application.css             ← Dark theme styling
│
├── database/
│   └── schema.sql                  ← Database DDL & sample data
│
├── pom.xml                         ← Maven config
├── SETUP_GUIDE.md                  ← Detailed setup guide
├── ERROR_HANDLING_GUIDE.md         ← Error handling documentation
└── README.md                       ← This file
```

---

## 🎨 FITUR UTAMA

### 1️⃣ LOGIN PAGE
- ✅ Validasi username & password dari database
- ✅ Selection role (Admin, Pengelola, Wisatawan)
- ✅ Error handling koneksi database
- ✅ Session management

### 2️⃣ DASHBOARD - JELAJAHI DESTINASI
- ✅ Tampilkan semua destinasi dalam TableView
- ✅ **Search pintar** (nama, kategori, lokasi)
- ✅ Filter berdasarkan kategori
- ✅ Lihat detail: harga, rating, deskripsi
- ✅ Tombol "Tambah ke Itinerary"

### 3️⃣ ITINERARY PLANNER
- ✅ Buat rencana perjalanan baru
- ✅ Tambahkan multiple destinasi
- ✅ Set tanggal mulai & selesai
- ✅ **Kalkulasi total biaya otomatis**
- ✅ Lihat rencana perjalanan sebelumnya
- ✅ Edit/Update/Delete itinerary

### 4️⃣ UI - DARK MODERN THEME
- 🎨 Background gelap: #121212, #1a1a1a
- 🎨 Aksen hijau: #DEFF9A
- 🎨 Teks terang: #DAFFDE, #f5f5f5
- 🎨 Custom CSS styling
- 🎨 Responsive layout

---

## 🔒 ERROR HANDLING

### Database Connection
```java
try {
    conn = DatabaseHelper.getInstance().getConnection();
    if (conn == null) {
        showError("Database Error", "Gagal terhubung ke database");
        return null;
    }
} catch (SQLException e) {
    // Log error & show user-friendly message
    LOGGER.log(Level.SEVERE, "Error message", e);
}
```

### Input Validation
```java
if (username.isEmpty() || password.isEmpty()) {
    showError("Validasi Gagal", "Username dan password tidak boleh kosong!");
    return;
}
```

### Resource Cleanup
```java
finally {
    try { if (rs != null) rs.close(); } catch (Exception e) {}
    try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
    try { if (conn != null) conn.close(); } catch (Exception e) {}
}
```

**📝 Detail error handling → lihat ERROR_HANDLING_GUIDE.md**

---

## 🗄️ DATABASE SCHEMA

### TABEL: users
```
id (SERIAL PRIMARY KEY)
username (VARCHAR UNIQUE)
password (VARCHAR - hashed)
role (VARCHAR - Admin/Pengelola/Wisatawan)
nama_lengkap (VARCHAR)
email (VARCHAR)
no_telepon (VARCHAR)
```

### TABEL: destinasi
```
id (SERIAL PRIMARY KEY)
nama (VARCHAR)
kategori (VARCHAR)
harga (INT)
deskripsi (TEXT)
koordinat (VARCHAR)
lokasi (VARCHAR)
rating (DECIMAL)
```

### TABEL: itinerary
```
id (SERIAL PRIMARY KEY)
user_id (INT FK → users)
nama_rencana (VARCHAR)
total_biaya (INT)
tanggal_mulai (DATE)
tanggal_selesai (DATE)
status (VARCHAR - Draft/Finalized/Completed)
```

### TABEL: itinerary_detail
```
id (SERIAL PRIMARY KEY)
itinerary_id (INT FK → itinerary)
destinasi_id (INT FK → destinasi)
urutan (INT)
tanggal_kunjungan (DATE)
```

---

## ⚠️ TROUBLESHOOTING

| Problem | Solution |
|---------|----------|
| "Connection refused" | PostgreSQL server tidak berjalan → Start PostgreSQL di Services |
| "Database does not exist" | Jalankan schema.sql untuk create database & tables |
| "Authentication failed" | Update DB_USER & DB_PASSWORD di DatabaseHelper.java |
| "FXML not found" | Pastikan file .fxml ada di src/main/resources/com/example/ |
| "Port 5432 already in use" | Ubah port di DB_URL atau stop PostgreSQL instance lain |

**Selengkapnya → SETUP_GUIDE.md**

---

## 📊 SAMPLE DATA

Login credentials yang tersedia:

| Username | Password | Role | Status |
|----------|----------|------|--------|
| admin | admin | Admin | ✅ Active |
| pengelola01 | admin | Pengelola | ✅ Active |
| wisatawan01 | admin | Wisatawan | ✅ Active |
| wisatawan02 | admin | Wisatawan | ✅ Active |

Sample destinasi: Borobudur, Bali, Bromo, Raja Ampat, Danau Toba, Monnas, Komodo, Kawah Putih

---

## 🔧 KONFIGURASI DATABASE

Edit file: `src/main/java/com/example/DatabaseHelper.java`

```java
private static final String DB_URL = "jdbc:postgresql://localhost:5432/wisata_db";
private static final String DB_USER = "postgres";
private static final String DB_PASSWORD = "postgres";
```

---

## 📚 DOKUMENTASI

- **SETUP_GUIDE.md** - Step-by-step setup instructions
- **ERROR_HANDLING_GUIDE.md** - Detailed error handling documentation
- **Code Comments** - Inline comments di setiap class

---

## ✅ CHECKLIST FITUR

- [x] Database DDL script (users, destinasi, itinerary, itinerary_detail)
- [x] DatabaseHelper.java dengan JDBC connection
- [x] Login page dengan authentication
- [x] Dashboard dengan list destinasi & search pintar
- [x] Detail destinasi (harga, rating, deskripsi)
- [x] Itinerary planner (buat, tambah, hapus destinasi)
- [x] Kalkulasi total biaya otomatis
- [x] Dark modern UI dengan CSS custom
- [x] Error handling di setiap layer
- [x] Try-catch untuk database operations
- [x] Resource cleanup (ResultSet, Statement, Connection)
- [x] Logging untuk debugging
- [x] Session management
- [x] Input validation

---

## 🎓 LEARNING OUTCOMES

Melalui proyek ini, Anda akan belajar:
- ✅ JavaFX untuk UI desktop
- ✅ PostgreSQL JDBC connection
- ✅ MVC architecture pattern
- ✅ Error handling best practices
- ✅ SQL DDL & DML queries
- ✅ Resource management (try-finally)
- ✅ CSS styling di JavaFX
- ✅ Logging & debugging
- ✅ Maven build system
- ✅ FXML XML markup

---

## 👥 TEAM

**Kelompok B6** - Project Akhir Sistem Informasi Eksplorasi Wisata

---

## 📄 LICENSE

Educational Project - Used for learning purposes only

---

## 📞 SUPPORT

Untuk masalah atau pertanyaan:
1. Baca **SETUP_GUIDE.md** untuk setup issues
2. Baca **ERROR_HANDLING_GUIDE.md** untuk error handling
3. Check console output untuk error messages
4. Lihat inline code comments untuk penjelasan

---

**Last Updated:** 2024  
**Java Version:** JDK 11+  
**JavaFX Version:** 13+  
**PostgreSQL Version:** 12+
