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
public class DashboardController {
    
    // ============================================================
    // FXML COMPONENTS
    // ============================================================
    
    @FXML private Label welcomeLabel;
    @FXML private TableView<Destinasi> destinationTable;
    @FXML private TableColumn<Destinasi, Integer> idColumn;
    @FXML private TableColumn<Destinasi, String> namaColumn;
    @FXML private TableColumn<Destinasi, String> kategoriColumn;
    @FXML private TableColumn<Destinasi, Integer> hargaColumn;
    @FXML private TableColumn<Destinasi, Double> ratingColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> kategoriFilter;
    @FXML private TextArea deskripsiArea;
    @FXML private Label hargaLabel;
    @FXML private Label ratingLabel;
    @FXML private Label lokasiLabel;
    @FXML private Button addToItineraryButton;
    @FXML private Button myItineraryButton;
    @FXML private Button logoutButton;
    
    // ============================================================
    // LOGGER
    // ============================================================
    
    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());
    
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
        idColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        namaColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNama()));
        kategoriColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKategori()));
        hargaColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getHarga()).asObject());
        ratingColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getRating()).asObject());
        
        // Set table items
        destinationTable.setItems(filteredDestinations);
        
        // Add selection listener untuk menampilkan detail
        destinationTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    displayDestinationDetails(newVal);
                }
            }
        );
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
                showError("Database Error", "Gagal terhubung ke database");
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
                    null
                );
                allDestinations.add(destinasi);
            }
            
            LOGGER.log(Level.INFO, "Loaded " + allDestinations.size() + " destinations from database");
            
        } catch (Exception e) {
            // ERROR HANDLING: Exception
            LOGGER.log(Level.SEVERE, "Error loading destinations from database", e);
            System.out.println("==== DATABASE ERROR ====");
            System.out.println("Message: " + e.getMessage());
            System.out.println("========================");
            showError("Error", "Gagal memuat destinasi dari database");
            
        } finally {
            // ERROR HANDLING: Resource cleanup
            try {
                if (rs != null) rs.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
            }
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing PreparedStatement", e);
            }
            try {
                if (conn != null) conn.close();
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
            "Fauna"
        );
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
     * Tampilkan detail destinasi yang dipilih
     */
    private void displayDestinationDetails(Destinasi destinasi) {
        if (destinasi != null) {
            deskripsiArea.setText(destinasi.getDeskripsi() != null ? destinasi.getDeskripsi() : "Tidak ada deskripsi");
            hargaLabel.setText("Harga: " + destinasi.getHargaFormatted());
            ratingLabel.setText("Rating: " + destinasi.getRatingFormatted());
            lokasiLabel.setText("Lokasi: " + destinasi.getLokasi());
        }
    }
    
    // ============================================================
    // ACTION HANDLERS
    // ============================================================
    
    /**
     * Handle tombol "Tambah ke Itinerary"
     */
    @FXML
    private void handleAddToItinerary() {
        Destinasi selectedDestinasi = destinationTable.getSelectionModel().getSelectedItem();
        
        if (selectedDestinasi == null) {
            showInfo("Perhatian", "Pilih destinasi terlebih dahulu!");
            return;
        }
        
        try {
            // Load itinerary view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("itinerary.fxml"));
            Parent root = loader.load();
            
            // Get controller
            ItineraryController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setSelectedDestinasi(selectedDestinasi);
            controller.loadItineraries();
            
            // Open in new window
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            String css = getClass().getResource("application.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            stage.setScene(scene);
            stage.setTitle("Itinerary Planner - " + selectedDestinasi.getNama());
            stage.setWidth(900);
            stage.setHeight(600);
            stage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening itinerary window", e);
            showError("Error", "Gagal membuka Itinerary Planner");
        }
    }
    
    /**
     * Handle tombol "Itinerary Saya"
     */
    @FXML
    private void handleMyItinerary() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("itinerary.fxml"));
            Parent root = loader.load();
            
            ItineraryController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.loadItineraries();
            
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            String css = getClass().getResource("application.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            stage.setScene(scene);
            stage.setTitle("Itinerary Saya");
            stage.setWidth(900);
            stage.setHeight(600);
            stage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening itinerary window", e);
            showError("Error", "Gagal membuka halaman Itinerary");
        }
    }
    
    /**
     * Handle tombol Logout
     */
    @FXML
    private void handleLogout() {
        LoginController.setCurrentUser(null);
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            String css = getClass().getResource("application.css").toExternalForm();
            scene.getStylesheets().add(css);
            
            stage.setScene(scene);
            stage.setTitle("Sistem Eksplorasi Wisata - Login");
            stage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading login page", e);
            showError("Error", "Gagal kembali ke login");
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
