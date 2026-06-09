================================================================
PANDUAN SETUP SISTEM INFORMASI EKSPLORASI WISATA
Aplikasi Desktop Native Java dengan JavaFX & PostgreSQL
Kelompok B6
================================================================

DAFTAR ISI:
1. Prerequisites & Instalasi
2. Setup Database PostgreSQL
3. Build & Run Aplikasi
4. Fitur Aplikasi
5. Struktur Kode
6. Konfigurasi Database
7. Testing Aplikasi
8. Troubleshooting
9. Catatan Error Handling

================================================================
1. PREREQUISITES & INSTALASI
================================================================

Sebelum menjalankan aplikasi, pastikan Anda sudah menginstall:

1.1) JAVA DEVELOPMENT KIT (JDK 11+)
   Download: https://www.oracle.com/java/technologies/javase-jdk11-downloads.html
   
   Verifikasi instalasi (buka Command Prompt / PowerShell):
   ```
   java -version
   javac -version
   ```

1.2) MAVEN (Untuk build & dependency management)
   Download: https://maven.apache.org/download.cgi
   Extract ke folder (contoh: C:\apache-maven-3.8.x)
   
   Tambahkan ke Environment Variables:
   - Buka System Properties → Environment Variables
   - New System Variable: MAVEN_HOME = C:\apache-maven-3.8.x
   - Edit PATH: tambahkan %MAVEN_HOME%\bin
   
   Verifikasi instalasi:
   ```
   mvn -v
   ```

1.3) POSTGRESQL (Database Server)
   Download: https://www.postgresql.org/download/windows/
   
   Instalasi:
   - Jalankan installer
   - Set password untuk user 'postgres' (contoh: 'postgres')
   - Default port: 5432
   
   Verifikasi PostgreSQL berjalan:
   - Windows: Services → PostgreSQL
   - Pastikan status "Running"

1.4) GIT (Optional - untuk version control)
   Download: https://git-scm.com/download/win

================================================================
2. SETUP DATABASE PostgreSQL
================================================================

2.1) BUAT DATABASE BARU

   Buka PostgreSQL Command Line (psql):
   - Windows: Cari "SQL Shell (psql)" di Start Menu
   - Atau buka cmd dan ketik: psql -U postgres
   
   Ketik password Anda saat diminta (default: postgres)

2.2) JALANKAN SCRIPT DATABASE

   Di psql, ketik:
   ```
   CREATE DATABASE wisata_db;
   \c wisata_db
   ```
   
   Kemudian jalankan script SQL:
   
   OPSI A: Dari file
   ```
   \i 'C:/Users/PC/Documents/DesktopProject/demo/database/schema.sql'
   ```
   
   OPSI B: Copy-paste seluruh script dari database/schema.sql
   - Buka file schema.sql
   - Copy semua isi
   - Paste di psql
   - Tekan Enter

2.3) VERIFIKASI SETUP

   Di psql, ketik:
   ```
   \dt
   ```
   
   Seharusnya menampilkan tabel:
   - users
   - destinasi
   - itinerary
   - itinerary_detail

   Untuk cek data sample:
   ```
   SELECT * FROM users;
   SELECT COUNT(*) FROM destinasi;
   ```

2.4) SAMPLE DATA LOGIN

   Username: admin
   Password: admin
   Role: Admin
   
   Username: wisatawan01
   Password: admin
   Role: Wisatawan

================================================================
3. BUILD & RUN APLIKASI
================================================================

3.1) BUKA PROJECT DI VS CODE

   ```
   cd C:\Users\PC\Documents\DesktopProject\demo
   code .
   ```

3.2) BUILD DENGAN MAVEN

   Buka Terminal di VS Code (Ctrl+`)
   
   Jalankan:
   ```
   mvn clean compile
   ```
   
   Tunggu hingga selesai. Seharusnya terlihat:
   "BUILD SUCCESS"

3.3) RUN APLIKASI

   Opsi A: Menggunakan Maven
   ```
   mvn clean javafx:run
   ```
   
   Opsi B: Menggunakan VS Code Run Configuration
   - Tekan F5 atau Ctrl+Shift+D
   - Pilih Java
   - Biarkan debugger berjalan

3.4) APLIKASI SIAP DIGUNAKAN

   - Jendela login akan muncul
   - Masukkan username dan password
   - Pilih role (Admin, Pengelola, atau Wisatawan)
   - Klik LOGIN

================================================================
4. FITUR APLIKASI
================================================================

4.1) LOGIN SCREEN
   - Validasi username, password, dan role dari database
   - Error handling untuk koneksi database yang gagal
   - Session management
   
4.2) DASHBOARD - JELAJAHI DESTINASI
   - Tampilkan semua destinasi wisata dalam TableView
   - Pencarian pintar (by nama, kategori, lokasi)
   - Filter berdasarkan kategori
   - Detail destinasi (harga, rating, deskripsi)
   - Tombol "Tambah ke Itinerary"
   
4.3) ITINERARY PLANNER - RENCANA PERJALANAN
   - Buat rencana perjalanan baru
   - Tambahkan destinasi pilihan
   - Edit tanggal mulai & selesai
   - Lihat kalkulasi total biaya otomatis
   - Simpan/update/hapus itinerary
   - Lihat detail itinerary sebelumnya

================================================================
5. STRUKTUR KODE
================================================================

src/main/java/com/example/
│
├── App.java                    ← Entry point aplikasi
├── DatabaseHelper.java         ← JDBC connection & utilities
│
├── Model Classes:
│   ├── User.java              ← Model untuk pengguna
│   ├── Destinasi.java         ← Model untuk destinasi wisata
│   ├── Itinerary.java         ← Model untuk rencana perjalanan
│   └── ItineraryDetail.java   ← Model untuk detail itinerary
│
├── Controllers:
│   ├── LoginController.java    ← Handle login process
│   ├── DashboardController.java← Handle dashboard & destinasi
│   └── ItineraryController.java← Handle itinerary planning
│
└── PrimaryController.java      ← (Deprecated - untuk future use)
    SecondaryController.java    ← (Deprecated - untuk future use)

src/main/resources/com/example/
│
├── FXML Files:
│   ├── login.fxml             ← Layout untuk login screen
│   ├── dashboard.fxml         ← Layout untuk dashboard
│   ├── itinerary.fxml         ← Layout untuk itinerary planner
│   ├── primary.fxml           ← (Deprecated)
│   └── secondary.fxml         ← (Deprecated)
│
├── CSS:
│   └── application.css        ← Dark theme styling
│
└── Images:
    └── (untuk menambahkan gambar destinasi)

database/
│
└── schema.sql                 ← Script SQL database setup

================================================================
6. KONFIGURASI DATABASE
================================================================

Untuk mengubah koneksi database, edit file: DatabaseHelper.java

Cari bagian:
```java
private static final String DB_URL = "jdbc:postgresql://localhost:5432/wisata_db";
private static final String DB_USER = "postgres";
private static final String DB_PASSWORD = "postgres";
```

Sesuaikan dengan konfigurasi PostgreSQL Anda:

- DB_URL: jdbc:postgresql://[host]:[port]/[database_name]
- DB_USER: Username PostgreSQL
- DB_PASSWORD: Password PostgreSQL

Contoh untuk PostgreSQL di server remote:
```java
private static final String DB_URL = "jdbc:postgresql://192.168.1.100:5432/wisata_db";
private static final String DB_USER = "remote_user";
private static final String DB_PASSWORD = "remote_password";
```

================================================================
7. TESTING APLIKASI
================================================================

7.1) TEST LOGIN
   ✓ Login dengan username: admin, password: admin, role: Admin
   ✓ Login dengan username: wisatawan01, password: admin, role: Wisatawan
   ✓ Coba login dengan password salah → harus error
   ✓ Coba login dengan username kosong → harus error

7.2) TEST DASHBOARD
   ✓ Lihat semua destinasi di table
   ✓ Cari destinasi (coba ketik "Bali", "Pantai", dll)
   ✓ Filter berdasarkan kategori
   ✓ Klik destinasi untuk lihat detail
   ✓ Verifikasi harga dan rating ditampilkan dengan benar

7.3) TEST ITINERARY
   ✓ Klik "Tambah ke Itinerary" pada destinasi
   ✓ Buat itinerary baru dengan nama dan tanggal
   ✓ Tambahkan multiple destinasi
   ✓ Verifikasi total biaya dihitung otomatis
   ✓ Hapus destinasi dari itinerary
   ✓ Simpan itinerary
   ✓ Lihat itinerary sebelumnya dari dropdown

7.4) TEST ERROR HANDLING
   ✓ Matikan PostgreSQL → aplikasi harus error dengan pesan jelas
   ✓ Hapus database → aplikasi harus error dengan pesan jelas
   ✓ Invalid input di form → validasi error message

================================================================
8. TROUBLESHOOTING
================================================================

MASALAH 1: "Connection refused" saat login
SOLUSI:
- Pastikan PostgreSQL server sudah running
- Windows: Buka Services → cari "PostgreSQL" → Start
- Test dengan: psql -U postgres

MASALAH 2: "Database 'wisata_db' does not exist"
SOLUSI:
- Buat database: CREATE DATABASE wisata_db;
- Jalankan schema.sql untuk membuat tabel

MASALAH 3: "Authentication failed" saat login
SOLUSI:
- Pastikan PostgreSQL username & password benar
- Edit DatabaseHelper.java → ubah DB_USER dan DB_PASSWORD
- Default: DB_USER = "postgres", DB_PASSWORD = "postgres"

MASALAH 4: "javafx-maven-plugin not found"
SOLUSI:
- Pastikan Maven sudah terinstall dengan benar
- Jalankan: mvn clean
- Jalankan: mvn install

MASALAH 5: Port 5432 sudah terpakai
SOLUSI:
- PostgreSQL sudah berjalan di instance lain
- Ubah port di DB_URL (contoh: jdbc:postgresql://localhost:5433/wisata_db)
- Atau stop PostgreSQL yang lain

MASALAH 6: FXML File "login.fxml not found"
SOLUSI:
- Pastikan file login.fxml ada di: src/main/resources/com/example/
- Compile project dengan: mvn clean compile

================================================================
9. ERROR HANDLING - PENJELASAN TEKNIS
================================================================

9.1) DatabaseHelper.java - Connection Management

ERROR HANDLING untuk koneksi database:
```java
try {
    conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    return conn;
} catch (SQLException e) {
    // Tangkap SQL Exception
    System.out.println("ERROR: " + e.getMessage());
    System.out.println("SQL State: " + e.getSQLState());
    
    // Helpful error messages berdasarkan error code
    if (e.getMessage().contains("Connection refused")) {
        System.out.println("SOLUSI: PostgreSQL server tidak berjalan");
    } else if (e.getMessage().contains("authentication failed")) {
        System.out.println("SOLUSI: Username atau password salah");
    }
}
```

9.2) LoginController.java - Authentication Error Handling

ERROR HANDLING untuk authentication:
```java
try {
    conn = DatabaseHelper.getInstance().getConnection();
    if (conn == null) {
        showError("Database Error", "Gagal terhubung ke database");
        return null;
    }
    
    // Query & validate
    pstmt = conn.prepareStatement(sql);
    rs = pstmt.executeQuery();
    
} catch (SQLException e) {
    System.out.println("ERROR: " + e.getMessage());
    showError("Error", "Terjadi kesalahan saat proses login");
    return null;
    
} finally {
    // Cleanup: Tutup resource dengan aman
    if (rs != null) rs.close();
    if (pstmt != null) pstmt.close();
    if (conn != null) conn.close();
}
```

9.3) DashboardController.java - Data Loading Error Handling

ERROR HANDLING untuk load data:
```java
private void loadDestinationsFromDatabase() {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        conn = DatabaseHelper.getInstance().getConnection();
        if (conn == null) {
            showError("Database Error", "Gagal terhubung ke database");
            return;
        }
        
        // Execute query
        pstmt = conn.prepareStatement(sql);
        rs = pstmt.executeQuery();
        
        // Parse results
        while (rs.next()) {
            // ... populate data
        }
        
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Error loading destinations", e);
        showError("Error", "Gagal memuat destinasi dari database");
        
    } finally {
        // Cleanup semua resource
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
        try { if (conn != null) conn.close(); } catch (Exception e) {}
    }
}
```

9.4) Best Practices - Error Handling

✓ SELALU gunakan try-catch untuk database operations
✓ SELALU close resource di finally block
✓ GUNAKAN PreparedStatement (hindari SQL Injection)
✓ LOG semua error dengan Logger
✓ TAMPILKAN user-friendly error messages di UI
✓ VALIDASI input sebelum database query
✓ GUNAKAN Connection pooling di production

================================================================
10. NOTES IMPLEMENTASI
================================================================

10.1) PASSWORD HASHING

Saat ini menggunakan MD5 untuk password hashing.
⚠️ MD5 TIDAK AMAN UNTUK PRODUCTION!

Untuk production, gunakan:
- bcrypt
- Argon2
- PBKDF2
- scrypt

Contoh dengan bcrypt:
```java
import org.mindrot.jbcrypt.BCrypt;

String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
boolean isPasswordCorrect = BCrypt.checkpw(inputPassword, storedHash);
```

10.2) SESSION MANAGEMENT

User yang login disimpan di memory:
```java
public static User currentUser = null;

public static User getCurrentUser() {
    return currentUser;
}
```

Untuk production, gunakan session framework seperti:
- Spring Security
- Apache Shiro
- Jakarta Servlet Session

10.3) CSS DARK THEME

Warna utama yang digunakan:
- Background gelap: #121212, #1a1a1a
- Aksen (tombol): #DEFF9A (Hijau muda)
- Teks: #DAFFDE, #f5f5f5 (Putih terang)

Untuk customize warna, edit: application.css

10.4) DATABASE CONNECTION POOLING

Saat ini setiap operasi membuat koneksi baru.
Untuk optimasi, gunakan HikariCP atau C3P0:

```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.0.1</version>
</dependency>
```

================================================================
REFERENSI DOKUMENTASI
================================================================

- JavaFX Docs: https://openjfx.io/
- PostgreSQL JDBC: https://jdbc.postgresql.org/
- Maven: https://maven.apache.org/
- Java Logging: https://docs.oracle.com/en/java/javase/11/docs/api/java.logging/java/util/logging/Logger.html

================================================================
CREATED BY: Kelompok B6
DATE: 2024
DATABASE: PostgreSQL 12+
JAVA VERSION: JDK 11+
JAVAFX VERSION: 13+
================================================================
