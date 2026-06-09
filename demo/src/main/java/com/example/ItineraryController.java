package com.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ItineraryController - Mengelola rencana perjalanan (itinerary)
 * 
 * Fitur:
 * - Membuat itinerary baru
 * - Menambah destinasi ke itinerary
 * - Menampilkan detail itinerary
 * - Menghitung total biaya otomatis
 * - Error handling yang kuat
 * 
 * @author Sistem Eksplorasi Wisata B6
 */
public class ItineraryController {

    // ============================================================
    // FXML COMPONENTS
    // ============================================================

    @FXML
    private ComboBox<String> itineraryComboBox;
    @FXML
    private Button createNewButton;
    @FXML
    private TextField itineraryNameField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextArea notesTextArea;
    @FXML
    private TableView<ItineraryDetail> detailTable;
    @FXML
    private TableColumn<ItineraryDetail, Integer> urutanColumn;
    @FXML
    private TableColumn<ItineraryDetail, String> namaDestinasiColumn;
    @FXML
    private TableColumn<ItineraryDetail, String> hargaColumn;
    @FXML
    private TableColumn<ItineraryDetail, LocalDate> tanggalColumn;
    @FXML
    private Label totalBiayaLabel;
    @FXML
    private Label durasiLabel;
    @FXML
    private Button addDestinationButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button closeButton;

    // ============================================================
    // LOGGER
    // ============================================================

    private static final Logger LOGGER = Logger.getLogger(ItineraryController.class.getName());

    // ============================================================
    // DATA STORAGE
    // ============================================================

    private User currentUser;
    private Destinasi selectedDestinasi; // Destinasi yang dipilih dari dashboard
    private Itinerary currentItinerary;
    private ObservableList<Itinerary> allItineraries;
    private ObservableList<ItineraryDetail> currentDetails;

    // ============================================================
    // INITIALIZE
    // ============================================================

    /**
     * Initialize controller
     */
    @FXML
    public void initialize() {
        allItineraries = FXCollections.observableArrayList();
        currentDetails = FXCollections.observableArrayList();

        // Setup TableView
        setupTableColumns();

        // Setup ComboBox listener
        itineraryComboBox.setOnAction(e -> handleSelectItinerary());
    }

    /**
     * Setup kolom di TableView
     */
    private void setupTableColumns() {
        urutanColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getUrutan())
                        .asObject());
        namaDestinasiColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNamaDestinasi()));
        hargaColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getHargaFormatted()));
        tanggalColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getTanggalKunjungan()));

        detailTable.setItems(currentDetails);
    }

    // ============================================================
    // PUBLIC METHODS
    // ============================================================

    /**
     * Set current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Set destinasi yang dipilih dari dashboard
     */
    public void setSelectedDestinasi(Destinasi destinasi) {
        this.selectedDestinasi = destinasi;
    }

    /**
     * Load itineraries dari database
     */
    public void loadItineraries() {
        new Thread(() -> {
            try {
                loadItinerariesFromDatabase();
                javafx.application.Platform.runLater(() -> {
                    updateItineraryComboBox();
                    // Jika ada destinasi yang dipilih, fokus ke create new
                    if (selectedDestinasi != null) {
                        createNewButton.fire();
                    }
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading itineraries", e);
            }
        }).start();
    }

    // ============================================================
    // LOAD DATA FROM DATABASE
    // ============================================================

    /**
     * Load itineraries dari database
     * 
     * ERROR HANDLING:
     * - Try-catch untuk SQLException
     * - Resource cleanup
     */
    private void loadItinerariesFromDatabase() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getInstance().getConnection();
            if (conn == null) {
                LOGGER.log(Level.SEVERE, "Database connection failed");
                showError("Error", "Gagal terhubung ke database");
                return;
            }

            String sql = "SELECT id, nama_rencana, total_biaya, tanggal_mulai, tanggal_selesai, deskripsi, status " +
                    "FROM itinerary WHERE user_id = ? ORDER BY created_at DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUser.getId());
            rs = pstmt.executeQuery();

            allItineraries.clear();
            while (rs.next()) {
                Itinerary itinerary = new Itinerary(
                        rs.getInt("id"),
                        currentUser.getId(),
                        rs.getString("nama_rencana"),
                        rs.getInt("total_biaya"),
                        rs.getDate("tanggal_mulai") != null ? rs.getDate("tanggal_mulai").toLocalDate() : null,
                        rs.getDate("tanggal_selesai") != null ? rs.getDate("tanggal_selesai").toLocalDate() : null,
                        rs.getString("deskripsi"),
                        rs.getString("status"));
                allItineraries.add(itinerary);
            }

            LOGGER.log(Level.INFO,
                    "Loaded " + allItineraries.size() + " itineraries for user: " + currentUser.getUsername());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading itineraries from database", e);
            showError("Error", "Gagal memuat itinerary dari database");
        } finally {
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
     * Load detail itinerary dari database
     */
    private void loadItineraryDetails(int itineraryId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getInstance().getConnection();
            if (conn == null)
                return;

            String sql = "SELECT id.id, id.itinerary_id, id.destinasi_id, id.urutan, " +
                    "id.tanggal_kunjungan, id.catatan, d.nama, d.harga " +
                    "FROM itinerary_detail id " +
                    "JOIN destinasi d ON id.destinasi_id = d.id " +
                    "WHERE id.itinerary_id = ? " +
                    "ORDER BY id.urutan ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, itineraryId);
            rs = pstmt.executeQuery();

            currentDetails.clear();
            int totalCost = 0;

            while (rs.next()) {
                ItineraryDetail detail = new ItineraryDetail(
                        rs.getInt("id"),
                        rs.getInt("itinerary_id"),
                        rs.getInt("destinasi_id"),
                        rs.getInt("urutan"),
                        rs.getDate("tanggal_kunjungan") != null ? rs.getDate("tanggal_kunjungan").toLocalDate() : null,
                        rs.getString("catatan"));
                detail.setNamaDestinasi(rs.getString("nama"));
                detail.setHargaDestinasi(rs.getInt("harga"));

                currentDetails.add(detail);
                totalCost += rs.getInt("harga");
            }

            // Update total biaya label
            final int finalTotalCost = totalCost;
            javafx.application.Platform.runLater(() -> {
                totalBiayaLabel.setText("Total Biaya: Rp " + String.format("%,d", finalTotalCost).replace(",", "."));
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading itinerary details", e);
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception e) {
            }
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (Exception e) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
            }
        }
    }

    // ============================================================
    // ACTION HANDLERS
    // ============================================================

    /**
     * Handle pemilihan itinerary dari dropdown
     */
    @FXML
    private void handleSelectItinerary() {
        String selected = itineraryComboBox.getValue();
        if (selected == null || selected.isEmpty())
            return;

        // Find itinerary
        for (Itinerary itinerary : allItineraries) {
            if (itinerary.getNamaRencana().equals(selected)) {
                currentItinerary = itinerary;
                displayItineraryDetails(itinerary);
                loadItineraryDetails(itinerary.getId());
                break;
            }
        }
    }

    /**
     * Handle tombol "Buat Baru"
     */
    @FXML
    private void handleCreateNew() {
        // Clear fields
        itineraryNameField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        notesTextArea.clear();
        currentDetails.clear();
        currentItinerary = null;

        // Set default name jika ada destinasi yang dipilih
        if (selectedDestinasi != null) {
            itineraryNameField.setText("Rencana Kunjung " + selectedDestinasi.getNama());
        }

        itineraryNameField.requestFocus();
    }

    /**
     * Handle tombol "Simpan" itinerary
     */
    @FXML
    private void handleSaveItinerary() {
        String namaRencana = itineraryNameField.getText().trim();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // Validasi
        if (namaRencana.isEmpty()) {
            showError("Validasi", "Nama rencana tidak boleh kosong");
            return;
        }

        if (currentItinerary == null) {
            // INSERT BARU
            insertNewItinerary(namaRencana, startDate, endDate);

            // Jika ada destinasi yang dipilih dari dashboard, tambahkan otomatis
            if (selectedDestinasi != null && currentItinerary != null) {
                addDestinationToItinerary();
            }
        } else {
            // UPDATE
            updateItinerary(currentItinerary.getId(), namaRencana, startDate, endDate);
        }

        showInfo("Berhasil", "Itinerary berhasil disimpan");
        loadItineraries();
    }

    /**
     * Insert itinerary baru ke database
     * 
     * ERROR HANDLING: Try-catch dengan resource cleanup
     */
    private void insertNewItinerary(String namaRencana, LocalDate startDate, LocalDate endDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getInstance().getConnection();
            if (conn == null) {
                showError("Error", "Gagal terhubung ke database");
                return;
            }

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
                currentItinerary = new Itinerary(itineraryId, currentUser.getId(), namaRencana, 0, startDate, endDate,
                        notesTextArea.getText(), "Draft");
                LOGGER.log(Level.INFO, "New itinerary created with ID: " + itineraryId);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inserting new itinerary", e);
            showError("Error", "Gagal menyimpan itinerary baru");
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception e) {
            }
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (Exception e) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Update itinerary existing
     */
    private void updateItinerary(int itineraryId, String namaRencana, LocalDate startDate, LocalDate endDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseHelper.getInstance().getConnection();
            if (conn == null)
                return;

            String sql = "UPDATE itinerary SET nama_rencana = ?, tanggal_mulai = ?, tanggal_selesai = ?, " +
                    "deskripsi = ? WHERE id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, namaRencana);
            pstmt.setDate(2, startDate != null ? java.sql.Date.valueOf(startDate) : null);
            pstmt.setDate(3, endDate != null ? java.sql.Date.valueOf(endDate) : null);
            pstmt.setString(4, notesTextArea.getText());
            pstmt.setInt(5, itineraryId);

            pstmt.executeUpdate();
            LOGGER.log(Level.INFO, "Itinerary updated: " + itineraryId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating itinerary", e);
            showError("Error", "Gagal mengupdate itinerary");
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (Exception e) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Handle tombol "Tambah Destinasi"
     */
    @FXML
    private void handleAddDestination() {
        if (currentItinerary == null) {
            showError("Perhatian", "Simpan itinerary terlebih dahulu");
            return;
        }

        if (selectedDestinasi != null) {
            addDestinationToItinerary();
        }
    }

    /**
     * Tambah destinasi ke itinerary
     */
    private void addDestinationToItinerary() {
        if (currentItinerary == null || selectedDestinasi == null)
            return;

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseHelper.getInstance().getConnection();
            if (conn == null)
                return;

            // Cek destinasi sudah ada atau belum
            int nextUrutan = currentDetails.size() + 1;

            String sql = "INSERT INTO itinerary_detail (itinerary_id, destinasi_id, urutan, tanggal_kunjungan) " +
                    "VALUES (?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentItinerary.getId());
            pstmt.setInt(2, selectedDestinasi.getId());
            pstmt.setInt(3, nextUrutan);
            pstmt.setDate(4, null);

            pstmt.executeUpdate();

            loadItineraryDetails(currentItinerary.getId());
            showInfo("Berhasil", "Destinasi berhasil ditambahkan ke itinerary");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding destination to itinerary", e);
            showError("Error", "Gagal menambahkan destinasi");
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (Exception e) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Handle tombol "Hapus"
     */
    @FXML
    private void handleRemove() {
        ItineraryDetail selected = detailTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Perhatian", "Pilih destinasi yang akan dihapus");
            return;
        }

        removeDestinationFromItinerary(selected.getId());
    }

    /**
     * Hapus destinasi dari itinerary
     */
    private void removeDestinationFromItinerary(int detailId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseHelper.getInstance().getConnection();
            if (conn == null)
                return;

            String sql = "DELETE FROM itinerary_detail WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, detailId);

            pstmt.executeUpdate();
            loadItineraryDetails(currentItinerary.getId());
            showInfo("Berhasil", "Destinasi berhasil dihapus dari itinerary");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error removing destination", e);
            showError("Error", "Gagal menghapus destinasi");
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (Exception e) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Handle tombol "Hapus Itinerary"
     */
    @FXML
    private void handleDelete() {
        if (currentItinerary == null) {
            showError("Perhatian", "Pilih itinerary yang akan dihapus");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi");
        confirm.setHeaderText("Hapus Itinerary?");
        confirm.setContentText("Apakah Anda yakin ingin menghapus itinerary ini?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            deleteItinerary(currentItinerary.getId());
            loadItineraries();
            handleCreateNew();
        }
    }

    /**
     * Hapus itinerary dari database
     */
    private void deleteItinerary(int itineraryId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseHelper.getInstance().getConnection();
            if (conn == null)
                return;

            String sql = "DELETE FROM itinerary WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, itineraryId);

            pstmt.executeUpdate();
            showInfo("Berhasil", "Itinerary berhasil dihapus");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting itinerary", e);
            showError("Error", "Gagal menghapus itinerary");
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (Exception e) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Handle tombol Close
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /**
     * Update ComboBox dengan list itinerary
     */
    private void updateItineraryComboBox() {
        ObservableList<String> itineraryNames = FXCollections.observableArrayList();
        for (Itinerary itinerary : allItineraries) {
            itineraryNames.add(itinerary.getNamaRencana());
        }
        itineraryComboBox.setItems(itineraryNames);
    }

    /**
     * Tampilkan detail itinerary
     */
    private void displayItineraryDetails(Itinerary itinerary) {
        itineraryNameField.setText(itinerary.getNamaRencana());
        startDatePicker.setValue(itinerary.getTanggalMulai());
        endDatePicker.setValue(itinerary.getTanggalSelesai());
        notesTextArea.setText(itinerary.getDeskripsi() != null ? itinerary.getDeskripsi() : "");

        if (itinerary.getTanggalMulai() != null && itinerary.getTanggalSelesai() != null) {
            long duration = java.time.temporal.ChronoUnit.DAYS.between(itinerary.getTanggalMulai(),
                    itinerary.getTanggalSelesai()) + 1;
            durasiLabel.setText("Durasi: " + duration + " hari");
        }
    }

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
