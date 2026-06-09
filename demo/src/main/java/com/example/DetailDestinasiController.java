package com.example;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * DetailDestinasiController — Controller untuk detail_destinasi.fxml
 * Kelompok B6
 *
 * [DB] Terima parameter destinasi_id dari HomeController,
 *      lalu query: SELECT * FROM destinasi WHERE id = ?
 */
public class DetailDestinasiController implements Initializable {

    @FXML private Label destinasiNamaLabel;
    @FXML private Label judulLabel;
    @FXML private Label kategoriLabel;
    @FXML private Label ratingLabel;
    @FXML private Label reviewCountLabel;
    @FXML private Label lokasiLabel;
    @FXML private TextArea deskripsiTextArea;
    @FXML private Label jamWeekdayLabel;
    @FXML private Label jamWeekendLabel;
    @FXML private Label statusLabel;
    @FXML private Label hargaMulaiLabel;

    // [DB] Set ini dari navigasi sebelumnya (HomeController)
    private static int currentDestinasiId = 1;

    public static void setDestinasiId(int id) {
        currentDestinasiId = id;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadDestinasiData();
    }

    /**
     * [DB] Muat data destinasi dari PostgreSQL.
     * Query: SELECT d.*, AVG(u.rating) as avg_rating, COUNT(u.id) as jml_ulasan
     *        FROM destinasi d LEFT JOIN ulasan u ON u.destinasi_id = d.id
     *        WHERE d.id = ? GROUP BY d.id
     */
    private void loadDestinasiData() {
        // TODO: Ganti dengan query database menggunakan currentDestinasiId
        destinasiNamaLabel.setText("Candi Borobudur");
        judulLabel.setText("Candi Borobudur");
        kategoriLabel.setText("Kategori: Budaya & Sejarah");
        ratingLabel.setText("Rating: 4.9 / 5.0");
        reviewCountLabel.setText("1.245 ulasan");
        lokasiLabel.setText("Borobudur, Magelang, Jawa Tengah");
        deskripsiTextArea.setText(
            "Candi Borobudur adalah sebuah candi Buddha yang terletak di Borobudur, Magelang, " +
            "Jawa Tengah. Candi ini terletak kurang lebih 100 km di sebelah barat daya Semarang, " +
            "86 km di sebelah barat Yogyakarta. Salah satu monumen Buddha terbesar di dunia."
        );
        jamWeekdayLabel.setText("06:30 – 17:00");
        jamWeekendLabel.setText("06:30 – 17:00");
        statusLabel.setText("Buka");
        hargaMulaiLabel.setText("Rp 50.000");
    }

    @FXML private void handleBack() {
        try { App.setRoot("home"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleWishlist() {
        System.out.println("[DetailDestinasiController] Tambah ke wishlist");
        // [DB] INSERT INTO wishlist (user_id, destinasi_id) VALUES (?, ?)
    }

    @FXML private void handlePesanTiket() {
        BookingController.setDestinasiId(currentDestinasiId);
        try { App.setRoot("booking"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleChatPenjual() {
        System.out.println("[DetailDestinasiController] Buka chat penjual");
    }
}
