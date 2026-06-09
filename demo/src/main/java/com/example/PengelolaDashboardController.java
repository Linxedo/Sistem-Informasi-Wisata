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
public class PengelolaDashboardController {

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
    @FXML
    private TextField formNama;
    @FXML
    private ComboBox<String> formKategori;
    @FXML
    private TextField formHarga;
    @FXML
    private TextField formRating;
    @FXML
    private TextField formLokasi;
    @FXML
    private TextArea formDeskripsi;
    
    @FXML
    private Button btnSimpan;
    @FXML
    private Button btnUpdate;
    @FXML
    private Button btnHapus;

    @FXML
    private Button myItineraryButton; // legacy but keeping variable name if it was somehow used, wait I removed it
    @FXML
    private Button logoutButton;

    // ============================================================
    // LOGGER
    // ============================================================

    private static final Logger LOGGER = Logger.getLogger(PengelolaDashboardController.class.getName());

    // ============================================================
    // DATA STORAGE
    // ============================================================

    private User currentUser;
    private ObservableList<Destinasi> allDestinations;
    private ObservableList<Destinasi> filteredDestinations;

    // ============================================================
    // INITIALIZE
    // ============================================================

    /**
     * Initialize controller - dipanggil otomatis saat FXML di-load
     */
    @FXML
    public void initialize() {
        // Inisialisasi ObservableList
        allDestinations = FXCollections.observableArrayList();
        filteredDestinations = FXCollections.observableArrayList();

        // Setup TableView columns
        setupTableColumns();

        // Setup search listener
        setupSearchListener();

        // Load filter options
        loadKategoriFilter();
    }

    /**
     * Setup kolom-kolom di TableView
     */
    private void setupTableColumns() {
        idColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        namaColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNama()));
        kategoriColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKategori()));
        hargaColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getHarga()).asObject());
        ratingColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getRating()).asObject());

        // Set table items
        destinationTable.setItems(filteredDestinations);

        // Add selection listener untuk menampilkan detail
        destinationTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        displayDestinationDetails(newVal);
                    }
                });
                
        // Set up the Kategori Combo box in form
        formKategori.getItems().addAll("Alam", "Pantai", "Budaya Bersejarah", "Fauna", "Hiburan", "Kuliner");
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
