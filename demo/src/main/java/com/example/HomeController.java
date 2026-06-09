package com.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * HomeController — Controller untuk home.fxml
 * 
 * Kelompok B6 - Sistem Informasi Wisata
 * 
 * INTEGRASI DATABASE POSTGRESQL:
 * Semua method yang berkaitan dengan DB ditandai [DB].
 * Gunakan DatabaseHelper.getInstance() untuk mendapatkan koneksi.
 * 
 * Contoh query:
 *   String sql = "SELECT id, nama, lokasi, harga, rating FROM destinasi ORDER BY rating DESC";
 *   try (Connection conn = DatabaseHelper.getInstance().getConnection();
 *        PreparedStatement ps = conn.prepareStatement(sql);
 *        ResultSet rs = ps.executeQuery()) {
 *       while (rs.next()) { ... }
 *   }
 */
public class HomeController implements Initializable {

    // ========== FXML NODES (harus cocok dengan fx:id di home.fxml) ==========

    @FXML private Button navHome;
    @FXML private Button navBooking;
    @FXML private Button navWishlist;
    @FXML private Button navItinerary;
    @FXML private Button profileButton;

    @FXML private TextField searchField;
    @FXML private Button searchButton;

    @FXML private ComboBox<String> kategoriFilter;
    @FXML private ComboBox<String> ratingFilter;
    @FXML private ComboBox<String> hargaFilter;
    @FXML private Button resetFilterButton;

    @FXML private FlowPane destinationFlowPane;
    @FXML private FlowPane categoryFlowPane;

    // ========== INITIALIZE ==========

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilters();
        // [DB] Muat data dari database
        loadDestinations();
    }

    /**
     * [DB] Isi ComboBox filter dari data database.
     * Kategori: SELECT DISTINCT kategori FROM destinasi
     * Rating  : nilai statis
     * Harga   : nilai statis (range)
     */
    private void setupFilters() {
        // Kategori — [DB] ganti dengan query database
        kategoriFilter.setItems(FXCollections.observableArrayList(
            "Semua Kategori", "Pantai", "Pegunungan",
            "Budaya & Sejarah", "Alam Terbuka",
            "Taman Hiburan", "Kuliner"
        ));
        kategoriFilter.getSelectionModel().selectFirst();

        // Rating
        ratingFilter.setItems(FXCollections.observableArrayList(
            "Semua Rating", "4.5+", "4.0+",
            "3.0+"
        ));
        ratingFilter.getSelectionModel().selectFirst();

        // Harga
        hargaFilter.setItems(FXCollections.observableArrayList(
            "Semua Harga", "< Rp 50.000", "Rp 50.000 – 100.000",
            "Rp 100.000 – 200.000", "> Rp 200.000"
        ));
        hargaFilter.getSelectionModel().selectFirst();
    }

    /**
     * [DB] Muat destinasi dari database dan render sebagai card ke destinationFlowPane.
     * Query: SELECT id, nama, lokasi, harga, rating, gambar_url
     *        FROM destinasi WHERE aktif = true ORDER BY rating DESC LIMIT 12
     */
    private void loadDestinations() {
        // Bersihkan placeholder
        destinationFlowPane.getChildren().clear();

        // TODO: Ganti data statis ini dengan query PostgreSQL
        String[][] dummyData = {
            {"Candi Borobudur", "Magelang, Jawa Tengah", "Rating: 4.9", "Rp 50.000"},
            {"Pantai Kuta", "Badung, Bali", "Rating: 4.8", "Rp 30.000"},
            {"Kawah Ijen", "Banyuwangi, Jawa Timur", "Rating: 4.7", "Rp 25.000"},
            {"Raja Ampat", "Papua Barat", "Rating: 4.9", "Rp 150.000"},
            {"Taman Nasional Komodo", "NTT", "Rating: 4.8", "Rp 100.000"},
            {"Prambanan", "Sleman, DI Yogyakarta", "Rating: 4.7", "Rp 75.000"},
        };

        for (String[] d : dummyData) {
            destinationFlowPane.getChildren().add(createDestinationCard(d[0], d[1], d[2], d[3]));
        }
    }

    /**
     * Membuat VBox card destinasi secara programatik.
     * Ini adalah template card sesuai wireframe.
     */
    private VBox createDestinationCard(String nama, String lokasi, String rating, String harga) {
        VBox card = new VBox();
        card.getStyleClass().add("destination-card");
        card.setPrefWidth(280);

        // Placeholder gambar (ganti ImageView dengan URL dari DB)
        VBox imgPlaceholder = new VBox();
        imgPlaceholder.setMinHeight(160);
        imgPlaceholder.setStyle("-fx-background-color: #252525; -fx-alignment: center;");
        Label imgIcon = new Label("IMAGE");
        imgIcon.setStyle("-fx-font-size: 20; -fx-text-fill: #555555; -fx-font-weight: bold; -fx-padding: 50 0;");
        imgPlaceholder.getChildren().add(imgIcon);

        // Info Panel
        VBox info = new VBox(8);
        info.setStyle("-fx-padding: 14 15 16 15;");

        Label namaLabel = new Label(nama);
        namaLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #f5f5f5;");
        namaLabel.setWrapText(true);

        Label lokasiLabel = new Label(lokasi);
        lokasiLabel.getStyleClass().add("subtitle");

        HBox ratingRow = new HBox(8);
        ratingRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label ratingLabel = new Label(rating);
        ratingLabel.getStyleClass().add("rating-badge");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label hargaLabel = new Label(harga);
        hargaLabel.getStyleClass().add("price-tag");
        ratingRow.getChildren().addAll(ratingLabel, spacer, hargaLabel);

        Button detailBtn = new Button("Lihat Detail →");
        detailBtn.setStyle("-fx-padding: 9 15; -fx-font-size: 12; -fx-border-radius: 10; -fx-background-radius: 10;");
        detailBtn.setMaxWidth(Double.MAX_VALUE);
        // [CONTROLLER] onAction → navigasi ke detail_destinasi.fxml dengan parameter id
        detailBtn.setOnAction(e -> handleLihatDetail(nama));

        info.getChildren().addAll(namaLabel, lokasiLabel, ratingRow, detailBtn);
        card.getChildren().addAll(imgPlaceholder, info);
        return card;
    }

    // ========== EVENT HANDLERS ==========

    /** [CONTROLLER] Tombol Cari — filter destinasi dari DB */
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        System.out.println("[HomeController] Search: " + keyword);
        // [DB] SELECT * FROM destinasi WHERE nama ILIKE '%keyword%' OR lokasi ILIKE '%keyword%'
    }

    /** [CONTROLLER] Reset semua filter */
    @FXML
    private void handleResetFilter() {
        kategoriFilter.getSelectionModel().selectFirst();
        ratingFilter.getSelectionModel().selectFirst();
        hargaFilter.getSelectionModel().selectFirst();
        loadDestinations();
    }

    /** [CONTROLLER] Lihat semua destinasi */
    @FXML
    private void handleLihatSemua() {
        System.out.println("[HomeController] Lihat semua destinasi");
    }

    /** [CONTROLLER] Navigasi ke halaman detail destinasi */
    private void handleLihatDetail(String destinasiNama) {
        System.out.println("[HomeController] Lihat detail: " + destinasiNama);
        // TODO: App.setRoot("detail_destinasi") dan kirim parameter destinasi_id
        try {
            App.setRoot("detail_destinasi");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** [CONTROLLER] Navigasi ke booking */
    @FXML
    private void handleNavBooking() {
        try { App.setRoot("booking"); } catch (Exception e) { e.printStackTrace(); }
    }

    /** [CONTROLLER] Navigasi ke itinerary */
    @FXML
    private void handleNavItinerary() {
        try { App.setRoot("itinerary_planner"); } catch (Exception e) { e.printStackTrace(); }
    }

    /** [CONTROLLER] Navigasi ke login/profile */
    @FXML
    private void handleLogin() {
        try { App.setRoot("login"); } catch (Exception e) { e.printStackTrace(); }
    }

    /** [CONTROLLER] Navigasi ke home (refresh) */
    @FXML
    private void handleNavHome() {
        try { App.setRoot("home"); } catch (Exception e) { e.printStackTrace(); }
    }

    /** [CONTROLLER] Navigasi ke wishlist (belum sedia) */
    @FXML
    private void handleNavWishlist() {
        System.out.println("[HomeController] Fitur Wishlist belum diimplementasi");
    }
}
