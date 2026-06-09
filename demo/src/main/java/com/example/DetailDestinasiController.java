package com.example;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.net.URL;
import java.util.ResourceBundle;

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

    private static int currentDestinasiId = 1;

    public static void setDestinasiId(int id) {
        currentDestinasiId = id;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadDestinasiData();
    }

    private void loadDestinasiData() {
        Destinasi d = DataService.getDestinasiById(currentDestinasiId);
        if (d == null) {
            showError("Error", "Destinasi tidak ditemukan.");
            return;
        }

        destinasiNamaLabel.setText(d.getNama());
        judulLabel.setText(d.getNama());
        kategoriLabel.setText("Kategori: " + (d.getKategori() != null ? d.getKategori() : "-"));
        ratingLabel.setText("Rating: " + String.format("%.1f", d.getRating()) + " / 5.0");
        reviewCountLabel.setText("Berdasarkan data destinasi");
        lokasiLabel.setText(d.getLokasi() != null ? d.getLokasi() : "-");
        deskripsiTextArea.setText(d.getDeskripsi() != null ? d.getDeskripsi() : "Tidak ada deskripsi.");
        jamWeekdayLabel.setText("08:00 – 17:00");
        jamWeekendLabel.setText("08:00 – 20:00");
        statusLabel.setText("Buka");
        hargaMulaiLabel.setText(d.getHargaFormatted());
    }

    @FXML
    private void handleBack() {
        try {
            App.setRoot("home");
        } catch (Exception e) {
            showError("Error", "Gagal kembali ke halaman utama.");
        }
    }

    @FXML
    private void handleWishlist() {
        showInfo("Wishlist", "Destinasi ditambahkan ke wishlist (simulasi).");
    }

    @FXML
    private void handleShare() {
        Destinasi d = DataService.getDestinasiById(currentDestinasiId);
        if (d == null) {
            return;
        }
        String shareText = d.getNama() + " - " + d.getLokasi() + " | " + d.getHargaFormatted();
        ClipboardContent content = new ClipboardContent();
        content.putString(shareText);
        Clipboard.getSystemClipboard().setContent(content);
        showInfo("Bagikan", "Informasi destinasi telah disalin ke clipboard.");
    }

    @FXML
    private void handleLihatSemuaUlasan() {
        showInfo("Ulasan", "Belum ada ulasan untuk destinasi ini. Jadilah yang pertama memberikan ulasan!");
    }

    @FXML
    private void handlePesanTiket() {
        Destinasi d = DataService.getDestinasiById(currentDestinasiId);
        BookingController.setDestinasiId(currentDestinasiId);
        if (d != null) {
            BookingController.setDestinasiNama(d.getNama());
            BookingController.setBaseHarga(d.getHarga());
        }
        try {
            App.setRoot("booking");
        } catch (Exception e) {
            showError("Error", "Gagal membuka halaman booking.");
        }
    }

    @FXML
    private void handleChatPenjual() {
        showInfo("Chat Penjual", "Fitur chat akan segera tersedia. Hubungi b6@wisata.id untuk bantuan.");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
