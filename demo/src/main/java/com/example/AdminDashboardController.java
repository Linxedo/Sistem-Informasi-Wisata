package com.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DashboardController - Menampilkan daftar destinasi wisata
 * 
 * Fitur:
 * - Load destinasi dari database ke TableView
 * - Pencarian pintar berdasarkan nama/kategori
 * - Detail destinasi dengan deskripsi
 * - Tombol untuk menambah ke itinerary
 * - Error handling yang kuat
 * 
 * @author Sistem Eksplorasi Wisata B6
 */
public class AdminDashboardController {

    // ============================================================
    // FXML COMPONENTS
    // ============================================================

    @FXML
    private Label welcomeLabel;
    @FXML
    private TableView<Destinasi> destinationTable;
    @FXML
    private TableColumn<Destinasi, Integer> idColumn;
    @FXML
    private TableColumn<Destinasi, String> namaColumn;
    @FXML
    private TableColumn<Destinasi, String> kategoriColumn;
    @FXML
    private TableColumn<Destinasi, Integer> hargaColumn;
    @FXML
    private TableColumn<Destinasi, Double> ratingColumn;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> kategoriFilter;
    @FXML private TextField formNama;
    @FXML private ComboBox<String> formKategori;
    @FXML private TextField formHarga;
    @FXML private TextField formRating;
    @FXML private TextField formLokasi;
    @FXML private TextArea formDeskripsi;
    
    @FXML private Button btnSimpan;
    @FXML private Button btnUpdate;
    @FXML private Button btnHapus;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> userIdColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> namaUserColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    
    @FXML private TextField searchUserField;
    @FXML private ComboBox<String> roleFilter;
    
    @FXML private TextField formUsername;
    @FXML private TextField formNamaUser;
    @FXML private TextField formEmail;
    @FXML private TextField formPassword;
    @FXML private ComboBox<String> formRole;

    @FXML private Button btnSimpanUser;
    @FXML private Button btnUpdateUser;
    @FXML private Button btnHapusUser;

    @FXML private Button myItineraryButton; // legacy but keeping variable name if it was somehow used, wait I removed it
    @FXML
    private Button logoutButton;

    // ============================================================
    // LOGGER
    // ============================================================

    private static final Logger LOGGER = Logger.getLogger(AdminDashboardController.class.getName());

    // ============================================================
    // DATA STORAGE
    // ============================================================

    private User currentUser;
    private ObservableList<Destinasi> allDestinations;
    private ObservableList<Destinasi> filteredDestinations;
    private ObservableList<User> allUsers;
    private ObservableList<User> filteredUsers;

    // ============================================================
    // INITIALIZE
    // ============================================================

    /**
     * Initialize controller - dipanggil otomatis saat FXML di-load
     */
    @FXML
    public void initialize() {
        allDestinations = FXCollections.observableArrayList();
        filteredDestinations = FXCollections.observableArrayList();
        allUsers = FXCollections.observableArrayList();
        filteredUsers = FXCollections.observableArrayList();

        setupTableColumns();
        setupUserTableColumns();
        setupSearchListener();
        loadKategoriFilter();
        
        loadUsers();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        namaColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNama()));
        kategoriColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKategori()));
        hargaColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getHarga()).asObject());
        ratingColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getRating()).asObject());

        destinationTable.setItems(filteredDestinations);
        destinationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) displayDestinationDetails(newVal);
        });
                
        formKategori.getItems().addAll("Alam", "Pantai", "Budaya Bersejarah", "Fauna", "Hiburan", "Kuliner");
    }

    private void setupUserTableColumns() {
        userIdColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        usernameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        namaUserColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNamaLengkap()));
        emailColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        roleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole()));

        userTable.setItems(filteredUsers);
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) displayUserDetails(newVal);
        });
        
        formRole.getItems().addAll("Wisatawan", "Pengelola", "Admin");
        roleFilter.getItems().addAll("Semua Role", "Wisatawan", "Pengelola", "Admin");
        roleFilter.setValue("Semua Role");
        roleFilter.setOnAction(e -> filterUsers());
        searchUserField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers());
    }

    /**
     * Setup search listener untuk real-time search
     */
    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterDestinations(newVal);
        });
    }

    // ============================================================
    // PUBLIC METHODS (Called from LoginController)
    // ============================================================

    /**
     * Set current user yang login
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            welcomeLabel.setText("Selamat datang, " + user.getNamaLengkap() + "!");
        }
    }

    /**
     * Load destinasi dari database
     */
    public void loadDestinations() {
        new Thread(() -> {
            try {
                loadDestinationsFromDatabase();
                // Update UI di JavaFX Thread
                javafx.application.Platform.runLater(() -> {
                    filteredDestinations.setAll(allDestinations);
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading destinations", e);
            }
        }).start();
    }

    // ============================================================
    // LOAD DATA FROM DATABASE
    // ============================================================

    /**
     * Load destinasi dari database
     * 
     * ERROR HANDLING:
     * - Try-catch untuk SQLException
     * - Resource cleanup dengan try-with-resources
     * - Validasi koneksi database
     */
    private void loadDestinationsFromDatabase() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // ========== KONEKSI DATABASE ==========
            conn = DatabaseHelper.getInstance().getConnection();
            if (conn == null) {
                LOGGER.log(Level.SEVERE, "Database connection failed");
                javafx.application.Platform.runLater(() ->
                    showError("Database Error", "Gagal terhubung ke database"));
                return;
            }

            // ========== QUERY DESTINASI ==========
            String sql = "SELECT id, nama, kategori, harga, deskripsi, koordinat, lokasi, rating " +
                    "FROM destinasi ORDER BY nama ASC";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // ========== PARSE RESULT ==========
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
                        null);
                allDestinations.add(destinasi);
            }

            LOGGER.log(Level.INFO, "Loaded " + allDestinations.size() + " destinations from database");

        } catch (Exception e) {
            // ERROR HANDLING: Exception
            LOGGER.log(Level.SEVERE, "Error loading destinations from database", e);
            System.out.println("==== DATABASE ERROR ====");
            System.out.println("Message: " + e.getMessage());
            System.out.println("========================");
            javafx.application.Platform.runLater(() ->
                showError("Error", "Gagal memuat destinasi dari database"));

        } finally {
            // ERROR HANDLING: Resource cleanup
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
            }
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing PreparedStatement", e);
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing Connection", e);
            }
        }
    }

    // ============================================================
    // LOAD USERS FROM DATABASE
    // ============================================================
    public void loadUsers() {
        new Thread(() -> {
            try {
                loadUsersFromDatabase();
                javafx.application.Platform.runLater(() -> {
                    filteredUsers.setAll(allUsers);
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading users", e);
            }
        }).start();
    }

    private void loadUsersFromDatabase() {
        String sql = "SELECT id, username, nama_lengkap, email, role FROM users ORDER BY id ASC";
        try (Connection conn = DatabaseHelper.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            allUsers.clear();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setNamaLengkap(rs.getString("nama_lengkap"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                allUsers.add(user);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal meload user", e);
            javafx.application.Platform.runLater(() ->
                showError("Error", "Gagal memuat pengguna dari database"));
        }
    }

    /**
     * Load kategori untuk filter
     */
    private void loadKategoriFilter() {
        kategoriFilter.getItems().add("Semua Kategori");
        kategoriFilter.getItems().addAll(
                "Alam",
                "Pantai",
                "Budaya Bersejarah",
                "Fauna");
        kategoriFilter.setValue("Semua Kategori");

        // Add listener
        kategoriFilter.setOnAction(e -> {
            filterDestinations(searchField.getText());
        });
    }

    // ============================================================
    // SEARCH & FILTER
    // ============================================================

    /**
     * Filter destinasi berdasarkan search text dan kategori
     * 
     * Fitur:
     * - Search by nama (case-insensitive)
     * - Search by kategori
     * - Kombinasi search dan filter kategori
     * 
     * @param searchText Text yang dicari
     */
    private void filterDestinations(String searchText) {
        filteredDestinations.clear();
        String selectedKategori = kategoriFilter.getValue();
        String searchLower = searchText.toLowerCase().trim();

        for (Destinasi destinasi : allDestinations) {
            // Check kategori filter
            boolean kategoriMatch = selectedKategori.equals("Semua Kategori") ||
                    destinasi.getKategori().equalsIgnoreCase(selectedKategori);

            // Check search match (nama atau kategori)
            boolean searchMatch = searchLower.isEmpty() ||
                    destinasi.getNama().toLowerCase().contains(searchLower) ||
                    destinasi.getKategori().toLowerCase().contains(searchLower) ||
                    destinasi.getLokasi().toLowerCase().contains(searchLower);

            if (kategoriMatch && searchMatch) {
                filteredDestinations.add(destinasi);
            }
        }
    }

    private void filterUsers() {
        filteredUsers.clear();
        String selectedRole = roleFilter.getValue();
        String searchLower = searchUserField.getText().toLowerCase().trim();

        for (User user : allUsers) {
            boolean roleMatch = selectedRole == null || selectedRole.equals("Semua Role") || user.getRole().equalsIgnoreCase(selectedRole);
            boolean searchMatch = searchLower.isEmpty() || 
                user.getUsername().toLowerCase().contains(searchLower) || 
                user.getNamaLengkap().toLowerCase().contains(searchLower);

            if (roleMatch && searchMatch) {
                filteredUsers.add(user);
            }
        }
    }

    // ============================================================
    // DETAIL DISPLAY
    // ============================================================

    /**
     * Tampilkan detail destinasi ke Form Pengelola
     */
    private void displayDestinationDetails(Destinasi destinasi) {
        if (destinasi != null) {
            formNama.setText(destinasi.getNama());
            formKategori.setValue(destinasi.getKategori());
            formHarga.setText(String.valueOf(destinasi.getHarga()));
            formRating.setText(String.valueOf(destinasi.getRating()));
            formLokasi.setText(destinasi.getLokasi());
            formDeskripsi.setText(destinasi.getDeskripsi() != null ? destinasi.getDeskripsi() : "");
            
            btnSimpan.setDisable(true); // Disable simpan if item selected
        }
    }
    
    @FXML
    private void handleClearForm() {
        destinationTable.getSelectionModel().clearSelection();
        formNama.clear();
        formKategori.setValue(null);
        formHarga.clear();
        formRating.clear();
        formLokasi.clear();
        formDeskripsi.clear();
        btnSimpan.setDisable(false);
    }
    
    @FXML
    private void handleSimpan() {
        if (!validateForm()) return;

        String query = "INSERT INTO destinasi (nama, kategori, harga, rating, lokasi, deskripsi, koordinat, gambar_url, aktif) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, formNama.getText().trim());
            pstmt.setString(2, formKategori.getValue() != null ? formKategori.getValue() : "Alam");
            pstmt.setInt(3, Integer.parseInt(formHarga.getText().trim()));
            pstmt.setDouble(4, Double.parseDouble(formRating.getText().trim()));
            pstmt.setString(5, formLokasi.getText().trim());
            pstmt.setString(6, formDeskripsi.getText().trim());
            pstmt.setString(7, ""); // koordinat
            pstmt.setString(8, ""); // img
            pstmt.setBoolean(9, true); // aktif
            
            pstmt.executeUpdate();
            showInfo("Sukses", "Data destinasi berhasil ditambahkan.");
            handleClearForm();
            loadDestinations(); // Refresh
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal menyimpan", e);
            showError("Error", "Gagal menambahkan destinasi: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdate() {
        Destinasi selected = destinationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Peringatan", "Pilih destinasi di tabel dahulu.");
            return;
        }
        if (!validateForm()) return;

        String query = "UPDATE destinasi SET nama=?, kategori=?, harga=?, rating=?, lokasi=?, deskripsi=? WHERE id=?";
        try (Connection conn = DatabaseHelper.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, formNama.getText().trim());
            pstmt.setString(2, formKategori.getValue() != null ? formKategori.getValue() : "Alam");
            pstmt.setInt(3, Integer.parseInt(formHarga.getText().trim()));
            pstmt.setDouble(4, Double.parseDouble(formRating.getText().trim()));
            pstmt.setString(5, formLokasi.getText().trim());
            pstmt.setString(6, formDeskripsi.getText().trim());
            pstmt.setInt(7, selected.getId());
            
            pstmt.executeUpdate();
            showInfo("Sukses", "Data destinasi berhasil diperbarui.");
            handleClearForm();
            loadDestinations(); // Refresh
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal update", e);
            showError("Error", "Gagal mengupdate destinasi: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleHapus() {
        Destinasi selected = destinationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Peringatan", "Pilih destinasi di tabel dahulu.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setContentText("Hapus destinasi " + selected.getNama() + " secara permanen?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String query = "DELETE FROM destinasi WHERE id=?";
            try (Connection conn = DatabaseHelper.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();
                showInfo("Sukses", "Data destinasi dihapus.");
                handleClearForm();
                loadDestinations(); // Refresh
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Gagal menghapus", e);
                showError("Error", "Gagal menghapus destinasi: " + e.getMessage());
            }
        }
    }
    
    private boolean validateForm() {
        if (formNama.getText() == null || formNama.getText().trim().isEmpty()) {
            showError("Validasi", "Nama Destinasi harus diisi!");
            return false;
        }
        try {
            Integer.parseInt(formHarga.getText().trim());
            Double.parseDouble(formRating.getText().trim());
        } catch (Exception e) {
            showError("Validasi", "Harga harus angka bulat dan Rating harus angka desimal (contoh: 4.5)!");
            return false;
        }
        return true;
    }

    // ============================================================
    // USER FORM HANDLING
    // ============================================================

    private void displayUserDetails(User user) {
        if (user != null) {
            formUsername.setText(user.getUsername());
            formNamaUser.setText(user.getNamaLengkap());
            formEmail.setText(user.getEmail());
            formRole.setValue(user.getRole());
            formPassword.clear(); // Always clear password for security
            
            btnSimpanUser.setDisable(true);
        }
    }

    @FXML
    private void handleClearUserForm() {
        userTable.getSelectionModel().clearSelection();
        formUsername.clear();
        formNamaUser.clear();
        formEmail.clear();
        formPassword.clear();
        formRole.setValue(null);
        btnSimpanUser.setDisable(false);
    }

    @FXML
    private void handleSimpanUser() {
        if (!validateUserForm(true)) return;

        String query = "INSERT INTO users (username, password, nama_lengkap, email, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, formUsername.getText().trim());
            pstmt.setString(2, formPassword.getText().trim());
            pstmt.setString(3, formNamaUser.getText().trim());
            pstmt.setString(4, formEmail.getText().trim());
            pstmt.setString(5, formRole.getValue() != null ? formRole.getValue() : "Wisatawan");
            
            pstmt.executeUpdate();
            showInfo("Sukses", "Pengguna berhasil ditambahkan.");
            handleClearUserForm();
            loadUsers();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal menyimpan user: " + e.getMessage(), e);
            if(e.getMessage().contains("duplicate key") || e.getMessage().contains("users_username_key")) {
                showError("Gagal", "Username sudah digunakan!");
            } else {
                showError("Error", "Gagal menambahkan user.");
            }
        }
    }

    @FXML
    private void handleUpdateUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Peringatan", "Pilih user di tabel dahulu.");
            return;
        }
        if (!validateUserForm(false)) return;

        boolean updatePass = formPassword.getText() != null && !formPassword.getText().trim().isEmpty();
        String query = updatePass 
            ? "UPDATE users SET username=?, password=?, nama_lengkap=?, email=?, role=? WHERE id=?"
            : "UPDATE users SET username=?, nama_lengkap=?, email=?, role=? WHERE id=?";
            
        try (Connection conn = DatabaseHelper.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, formUsername.getText().trim());
            if (updatePass) {
                pstmt.setString(2, formPassword.getText().trim());
                pstmt.setString(3, formNamaUser.getText().trim());
                pstmt.setString(4, formEmail.getText().trim());
                pstmt.setString(5, formRole.getValue() != null ? formRole.getValue() : "Wisatawan");
                pstmt.setInt(6, selected.getId());
            } else {
                pstmt.setString(2, formNamaUser.getText().trim());
                pstmt.setString(3, formEmail.getText().trim());
                pstmt.setString(4, formRole.getValue() != null ? formRole.getValue() : "Wisatawan");
                pstmt.setInt(5, selected.getId());
            }
            
            pstmt.executeUpdate();
            showInfo("Sukses", "Data pengguna diperbarui.");
            handleClearUserForm();
            loadUsers();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal update user", e);
            if(e.getMessage().contains("duplicate key") || e.getMessage().contains("users_username_key")) {
                showError("Gagal", "Username sudah digunakan!");
            } else {
                showError("Error", "Gagal mengupdate user.");
            }
        }
    }

    @FXML
    private void handleHapusUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        if (selected.getId() == currentUser.getId()) {
            showError("Dilarang", "Anda tidak dapat menghapus akun Anda sendiri yang sedang login!");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setContentText("Hapus pengguna " + selected.getUsername() + "?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String query = "DELETE FROM users WHERE id=?";
            try (Connection conn = DatabaseHelper.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, selected.getId());
                pstmt.executeUpdate();
                showInfo("Sukses", "Pengguna dihapus.");
                handleClearUserForm();
                loadUsers();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Gagal menghapus user", e);
                showError("Error", "User ini mungkin memiliki riwayat booking/itinerary sehingga tidak dapat dihapus permanen.");
            }
        }
    }

    private boolean validateUserForm(boolean isNew) {
        if (formUsername.getText().trim().isEmpty() || formNamaUser.getText().trim().isEmpty()) {
            showError("Validasi", "Username dan Nama Lengkap harus diisi!");
            return false;
        }
        if (isNew && formPassword.getText().trim().isEmpty()) {
            showError("Validasi", "Password wajib diisi untuk user baru!");
            return false;
        }
        return true;
    }

    // ============================================================
    // ACTION HANDLERS
    // ============================================================



    /**
     * Handle tombol Logout
     */
    @FXML
    private void handleLogout() {
        LoginController.setCurrentUser(null);

        try {
            App.setRoot("home");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading home page", e);
            showError("Error", "Gagal kembali ke halaman utama");
        }
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Tampilkan error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Tampilkan info dialog
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
