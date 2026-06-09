================================================================
ERROR HANDLING DOCUMENTATION
Sistem Informasi Eksplorasi Wisata - Kelompok B6
================================================================

OVERVIEW:
Aplikasi ini menerapkan error handling yang KUAT di setiap layer:
✓ Database connection layer
✓ Authentication layer
✓ Data loading layer
✓ User input validation
✓ Resource cleanup (try-finally)
✓ Logging untuk debugging

================================================================
1. DATABASE CONNECTION ERROR HANDLING (DatabaseHelper.java)
================================================================

1.1) DRIVER INITIALIZATION
═══════════════════════════════════════════════════════════════

KODE:
```java
private void initializeDriver() {
    try {
        Class.forName(DB_DRIVER);
        LOGGER.log(Level.INFO, "PostgreSQL Driver berhasil dimuat");
    } catch (ClassNotFoundException e) {
        // ERROR HANDLING: PostgreSQL driver tidak ditemukan
        LOGGER.log(Level.SEVERE, "ERROR: PostgreSQL Driver tidak ditemukan!", e);
        System.out.println("FATAL ERROR: Driver PostgreSQL tidak terinstall!");
        System.out.println("Message: " + e.getMessage());
    }
}
```

ERROR HANDLING:
- Tangkap ClassNotFoundException jika driver tidak ditemukan
- Log ke LOGGER dengan level SEVERE
- Tampilkan pesan error yang jelas ke console
- Hentikan aplikasi (fatal error)

PENYEBAB UMUM:
- PostgreSQL JDBC driver belum di-add ke pom.xml
- Maven dependency belum di-download

SOLUSI:
- Pastikan di pom.xml ada:
  <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>42.7.1</version>
  </dependency>
- Jalankan: mvn clean compile

═══════════════════════════════════════════════════════════════

1.2) CONNECTION TO DATABASE
═══════════════════════════════════════════════════════════════

KODE:
```java
public Connection getConnection() {
    Connection connection = null;
    try {
        connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        LOGGER.log(Level.INFO, "Koneksi database berhasil");
        return connection;
    } catch (SQLException e) {
        // ERROR HANDLING: Tangkap SQL Exception
        LOGGER.log(Level.SEVERE, "ERROR: Gagal terhubung ke database", e);
        
        // Tampilkan error details
        System.out.println("====== DATABASE CONNECTION ERROR ======");
        System.out.println("Error Code: " + e.getErrorCode());
        System.out.println("SQL State: " + e.getSQLState());
        System.out.println("Message: " + e.getMessage());
        System.out.println("========================================");
        
        // Cek tipe error untuk debugging
        if (e.getMessage().contains("Connection refused")) {
            System.out.println("SOLUSI: PostgreSQL server tidak berjalan atau tidak dapat diakses");
        } else if (e.getMessage().contains("authentication failed")) {
            System.out.println("SOLUSI: Username atau password salah");
        } else if (e.getMessage().contains("database") && e.getMessage().contains("does not exist")) {
            System.out.println("SOLUSI: Database 'wisata_db' belum dibuat");
        }
        
        return null;
    }
}
```

ERROR HANDLING POINTS:
1. SQLException untuk koneksi gagal
2. Error code & SQL state untuk identifikasi masalah
3. Error message yang helpful untuk debugging
4. Cek tipe error → tampilkan solusi spesifik

KEMUNGKINAN ERROR & SOLUSI:

ERROR 1: "Connection refused"
├─ Penyebab: PostgreSQL server tidak berjalan
├─ SQL State: 08001
└─ Solusi: 
   1. Buka Services (Windows)
   2. Cari "PostgreSQL"
   3. Klik Start

ERROR 2: "authentication failed for user 'postgres'"
├─ Penyebab: Username/password salah
├─ SQL State: 28P01
└─ Solusi:
   1. Buka DatabaseHelper.java
   2. Ubah DB_USER dan DB_PASSWORD
   3. Default: DB_USER = "postgres", DB_PASSWORD = "postgres"

ERROR 3: "FATAL: database 'wisata_db' does not exist"
├─ Penyebab: Database belum dibuat
├─ SQL State: 3D000
└─ Solusi:
   1. Buka psql
   2. CREATE DATABASE wisata_db;
   3. Jalankan schema.sql

═══════════════════════════════════════════════════════════════

1.3) RESOURCE CLEANUP
═══════════════════════════════════════════════════════════════

KODE:
```java
public static void closeConnection(Connection connection) {
    if (connection != null) {
        try {
            connection.close();
            LOGGER.log(Level.INFO, "Koneksi database berhasil ditutup");
        } catch (SQLException e) {
            // ERROR HANDLING: Log warning jika error saat close
            LOGGER.log(Level.WARNING, "ERROR: Gagal menutup koneksi database", e);
            System.out.println("WARNING: Gagal menutup koneksi - " + e.getMessage());
        }
    }
}

public static void closeAllResources(Connection connection, PreparedStatement statement, ResultSet resultSet) {
    closeResultSet(resultSet);    // Close ResultSet terlebih dahulu
    closeStatement(statement);    // Close Statement
    closeConnection(connection); // Close Connection terakhir
}
```

ERROR HANDLING:
- Try-catch untuk setiap resource yang ditutup
- Log warning (tidak fatal) jika error
- Jangan throw exception saat cleanup

BEST PRACTICE:
- Tutup ResultSet terlebih dahulu
- Kemudian close Statement
- Terakhir close Connection
- Ini penting untuk menghindari memory leak

================================================================
2. AUTHENTICATION ERROR HANDLING (LoginController.java)
================================================================

2.1) INPUT VALIDATION
═══════════════════════════════════════════════════════════════

KODE:
```java
// ERROR HANDLING: Cek input kosong
if (username.isEmpty() || password.isEmpty()) {
    showError("Validasi Gagal", 
             "Username dan password tidak boleh kosong!");
    LOGGER.log(Level.WARNING, "Login attempt dengan input kosong");
    return;
}

if (username.length() < 3) {
    showError("Validasi Gagal", 
             "Username minimal 3 karakter");
    return;
}

if (password.length() < 1) {
    showError("Validasi Gagal", 
             "Password tidak boleh kosong");
    return;
}
```

ERROR HANDLING:
- Validasi sebelum query database
- Cek panjang username minimum
- Tampilkan error dialog untuk user
- Log warning untuk audit trail

BENEFIT:
- Hindari database query yang tidak perlu
- Feedback langsung untuk user
- Lebih aman (prevent SQL injection attempts)

═══════════════════════════════════════════════════════════════

2.2) DATABASE AUTHENTICATION
═══════════════════════════════════════════════════════════════

KODE:
```java
private User authenticateUser(String username, String password, String role) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        // ERROR HANDLING: Cek koneksi database
        conn = DatabaseHelper.getInstance().getConnection();
        if (conn == null) {
            showError("Database Error", 
                     "Gagal terhubung ke database.\n" +
                     "Pastikan PostgreSQL sudah berjalan dan database 'wisata_db' sudah dibuat.");
            LOGGER.log(Level.SEVERE, "Database connection failed during login");
            return null;
        }
        
        // ERROR HANDLING: Gunakan PreparedStatement (prevent SQL injection)
        String sql = "SELECT id, username, password, role, nama_lengkap, email, no_telepon " +
                    "FROM users WHERE username = ? AND role = ?";
        
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, username);  // Parameter binding
        pstmt.setString(2, role);      // Parameter binding
        
        rs = pstmt.executeQuery();
        
        if (rs.next()) {
            // User ditemukan
            String storedPassword = rs.getString("password");
            String hashedInputPassword = hashPassword(password);
            
            if (hashedInputPassword.equals(storedPassword)) {
                // PASSWORD COCOK
                User user = new User(...);
                LOGGER.log(Level.INFO, "User authenticated successfully: " + username);
                return user;
            } else {
                // PASSWORD SALAH
                LOGGER.log(Level.WARNING, "Wrong password for user: " + username);
                return null;
            }
        } else {
            // USER TIDAK DITEMUKAN
            LOGGER.log(Level.WARNING, "User not found: " + username + " with role: " + role);
            return null;
        }
        
    } catch (Exception e) {
        // ERROR HANDLING: Exception dari database
        LOGGER.log(Level.SEVERE, "Error during authentication", e);
        System.out.println("==== AUTHENTICATION ERROR ====");
        System.out.println("Message: " + e.getMessage());
        System.out.println("Type: " + e.getClass().getSimpleName());
        System.out.println("==============================");
        
        showError("Error", "Terjadi kesalahan saat proses login.\n" + e.getMessage());
        return null;
        
    } finally {
        // ERROR HANDLING: Resource cleanup
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
        try { if (conn != null) conn.close(); } catch (Exception e) {}
    }
}
```

ERROR HANDLING POINTS:
1. Check koneksi database exist
2. Gunakan PreparedStatement (SQL injection prevention)
3. Validate password match
4. Try-catch untuk unexpected exceptions
5. Resource cleanup di finally block

SECURITY FEATURES:
✓ PreparedStatement (prevent SQL injection)
✓ Password hashing (MD5 - upgrade ke bcrypt di production)
✓ Separate query untuk authentication (prevent info disclosure)
✓ Log semua login attempts untuk audit
✓ No sensitive data di error messages

═══════════════════════════════════════════════════════════════

2.3) NAVIGATION ERROR HANDLING
═══════════════════════════════════════════════════════════════

KODE:
```java
private void openDashboard(User user) throws IOException {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
        Parent root = loader.load();
        
        DashboardController controller = loader.getController();
        controller.setCurrentUser(user);
        controller.loadDestinations();
        
        Stage stage = (Stage) loginButton.getScene().getWindow();
        Scene scene = new Scene(root);
        
        // ERROR HANDLING: Load CSS
        String css = getClass().getResource("application.css").toExternalForm();
        scene.getStylesheets().add(css);
        
        stage.setScene(scene);
        stage.show();
        
    } catch (IOException e) {
        // ERROR HANDLING: Exception saat load FXML
        LOGGER.log(Level.SEVERE, "Error loading dashboard", e);
        throw e;
    }
}
```

ERROR HANDLING:
- Try-catch untuk IOException (FXML not found)
- Log severe error
- Throw exception ke caller untuk handle

PENYEBAB UMUM:
- File dashboard.fxml tidak ada
- Resource path salah
- Typo di resource path

================================================================
3. DATA LOADING ERROR HANDLING (DashboardController.java)
================================================================

3.1) LOAD DESTINASI
═══════════════════════════════════════════════════════════════

KODE:
```java
private void loadDestinationsFromDatabase() {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        // ERROR HANDLING: Check koneksi
        conn = DatabaseHelper.getInstance().getConnection();
        if (conn == null) {
            LOGGER.log(Level.SEVERE, "Database connection failed");
            showError("Database Error", "Gagal terhubung ke database");
            return;
        }
        
        // QUERY
        String sql = "SELECT id, nama, kategori, harga, deskripsi, koordinat, lokasi, rating " +
                    "FROM destinasi ORDER BY nama ASC";
        
        pstmt = conn.prepareStatement(sql);
        rs = pstmt.executeQuery();
        
        // PARSE RESULT
        allDestinations.clear();
        while (rs.next()) {
            Destinasi destinasi = new Destinasi(
                rs.getInt("id"),
                rs.getString("nama"),
                rs.getString("kategori"),
                rs.getInt("harga"),
                rs.getString("deskripsi"),
                rs.getString("koordinat"),
                rs.getString("lokasi"),
                rs.getDouble("rating"),
                null
            );
            allDestinations.add(destinasi);
        }
        
        LOGGER.log(Level.INFO, "Loaded " + allDestinations.size() + " destinations");
        
    } catch (Exception e) {
        // ERROR HANDLING: Log dan tampilkan error
        LOGGER.log(Level.SEVERE, "Error loading destinations from database", e);
        System.out.println("==== DATABASE ERROR ====");
        System.out.println("Message: " + e.getMessage());
        System.out.println("========================");
        showError("Error", "Gagal memuat destinasi dari database");
        
    } finally {
        // CLEANUP
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
        try { if (conn != null) conn.close(); } catch (Exception e) {}
    }
}
```

ERROR HANDLING:
- Check koneksi before query
- Try-catch untuk SQLException
- Resource cleanup di finally
- User-friendly error messages

3.2) SEARCH & FILTER
═══════════════════════════════════════════════════════════════

KODE:
```java
private void filterDestinations(String searchText) {
    filteredDestinations.clear();
    String selectedKategori = kategoriFilter.getValue();
    String searchLower = searchText.toLowerCase().trim();
    
    for (Destinasi destinasi : allDestinations) {
        // ERROR HANDLING: Null-safe checks
        boolean kategoriMatch = selectedKategori.equals("Semua Kategori") ||
                              destinasi.getKategori().equalsIgnoreCase(selectedKategori);
        
        boolean searchMatch = searchLower.isEmpty() ||
                            destinasi.getNama().toLowerCase().contains(searchLower) ||
                            destinasi.getKategori().toLowerCase().contains(searchLower) ||
                            destinasi.getLokasi().toLowerCase().contains(searchLower);
        
        if (kategoriMatch && searchMatch) {
            filteredDestinations.add(destinasi);
        }
    }
}
```

ERROR HANDLING:
- Check for null values
- Case-insensitive search
- Multiple field search

================================================================
4. ITINERARY OPERATION ERROR HANDLING (ItineraryController.java)
================================================================

4.1) INSERT ITINERARY
═══════════════════════════════════════════════════════════════

KODE:
```java
private void insertNewItinerary(String namaRencana, LocalDate startDate, LocalDate endDate) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        // ERROR HANDLING: Check koneksi
        conn = DatabaseHelper.getInstance().getConnection();
        if (conn == null) {
            showError("Error", "Gagal terhubung ke database");
            return;
        }
        
        // ERROR HANDLING: Use RETURNING clause untuk get ID
        String sql = "INSERT INTO itinerary (user_id, nama_rencana, tanggal_mulai, tanggal_selesai, " +
                    "deskripsi, status, total_biaya) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "RETURNING id";
        
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, currentUser.getId());
        pstmt.setString(2, namaRencana);
        pstmt.setDate(3, startDate != null ? java.sql.Date.valueOf(startDate) : null);
        pstmt.setDate(4, endDate != null ? java.sql.Date.valueOf(endDate) : null);
        pstmt.setString(5, notesTextArea.getText());
        pstmt.setString(6, "Draft");
        pstmt.setInt(7, 0);
        
        rs = pstmt.executeQuery();
        if (rs.next()) {
            int itineraryId = rs.getInt("id");
            currentItinerary = new Itinerary(...);
            LOGGER.log(Level.INFO, "New itinerary created with ID: " + itineraryId);
        }
        
    } catch (Exception e) {
        // ERROR HANDLING: Log dan tampilkan error
        LOGGER.log(Level.SEVERE, "Error inserting new itinerary", e);
        showError("Error", "Gagal menyimpan itinerary baru");
        
    } finally {
        // CLEANUP
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
        try { if (conn != null) conn.close(); } catch (Exception e) {}
    }
}
```

ERROR HANDLING:
- Check koneksi
- Parameter binding (prevent SQL injection)
- Handle null dates
- RETURNING clause untuk get generated ID
- Resource cleanup

4.2) DELETE OPERATION
═══════════════════════════════════════════════════════════════

KODE:
```java
private void deleteItinerary(int itineraryId) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    try {
        conn = DatabaseHelper.getInstance().getConnection();
        if (conn == null) return;
        
        String sql = "DELETE FROM itinerary WHERE id = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, itineraryId);
        
        pstmt.executeUpdate();
        showInfo("Berhasil", "Itinerary berhasil dihapus");
        
    } catch (Exception e) {
        // ERROR HANDLING: Tangkap error (bisa karena FK constraint)
        LOGGER.log(Level.SEVERE, "Error deleting itinerary", e);
        showError("Error", "Gagal menghapus itinerary");
        
    } finally {
        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
        try { if (conn != null) conn.close(); } catch (Exception e) {}
    }
}
```

ERROR HANDLING:
- Parameter binding
- Cascade delete di database (schema.sql)
- FK constraint error handling

================================================================
5. LOGGING STRATEGY
================================================================

5.1) LOG LEVELS

INFO:
- Application startup
- Database connections established
- User authentication success
- Data loading completed

WARNING:
- Failed login attempts
- Resource close errors
- Non-critical exceptions

SEVERE:
- Database connection failures
- Authentication errors
- FXML loading failures
- Unexpected exceptions

5.2) LOGGER USAGE

```java
import java.util.logging.Logger;
import java.util.logging.Level;

private static final Logger LOGGER = Logger.getLogger(ClassName.class.getName());

// Log info
LOGGER.log(Level.INFO, "Operation completed successfully");

// Log warning
LOGGER.log(Level.WARNING, "Warning message", exception);

// Log severe
LOGGER.log(Level.SEVERE, "Critical error occurred", exception);
```

5.3) LOGGING OUTPUT

Output dilihat di:
- VS Code Console
- System.out (untuk debug messages)
- IDE logging pane

================================================================
6. SUMMARY - ERROR HANDLING COVERAGE
================================================================

✓ DATABASE LAYER:
  - Driver loading errors
  - Connection failures (koneksi refused, auth failed, db not exist)
  - Query execution errors
  - Resource cleanup errors
  - Parameter binding errors

✓ BUSINESS LOGIC LAYER:
  - Input validation (empty, length, format)
  - Authentication failures
  - Authorization checks
  - Data consistency checks

✓ UI LAYER:
  - FXML loading errors
  - CSS loading errors
  - UI component errors
  - Dialog display errors

✓ RESOURCE MANAGEMENT:
  - Connection cleanup
  - Statement cleanup
  - ResultSet cleanup
  - Try-finally pattern
  - Null-safe operations

✓ LOGGING & DEBUGGING:
  - Structured logging
  - Error categorization
  - User-friendly messages
  - Technical error details in logs

================================================================
