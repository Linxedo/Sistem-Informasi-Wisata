package com.example;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.VBox;
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

        try {
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
        } catch (Exception e) {
            System.out.println("Error loading destinasi data: " + e.getMessage());
            showError("Error", "Gagal memuat data destinasi: " + e.getMessage());
        }
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
        User user = LoginController.getCurrentUser();
        if (user == null) {
            showInfo("Login", "Harap login untuk menambahkan ke wishlist.");
            return;
        }
        boolean isAdded = DataService.toggleWishlist(user.getId(), currentDestinasiId);
        if (isAdded) {
            showInfo("Wishlist", "Destinasi berhasil ditambahkan ke wishlist!");
        } else {
            showInfo("Wishlist", "Destinasi dihapus dari wishlist.");
        }
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
        List<DataService.Ulasan> ulasanList = DataService.getUlasanByDestinasi(currentDestinasiId);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ulasan Pengguna");
        alert.setHeaderText("Ulasan untuk " + destinasiNamaLabel.getText());
        
        VBox vbox = new VBox(10);
        ListView<String> listView = new ListView<>();
        listView.setPrefHeight(200);
        
        if (ulasanList.isEmpty()) {
            listView.getItems().add("Belum ada ulasan.");
        } else {
            for (DataService.Ulasan u : ulasanList) {
                listView.getItems().add(u.namaUser + " - ⭐ " + u.rating + "\n" + u.komentar + "\n(" + u.tanggal + ")");
            }
        }
        
        vbox.getChildren().add(listView);
        
        User user = LoginController.getCurrentUser();
        if (user != null) {
            javafx.scene.control.Button btnKirim = new javafx.scene.control.Button("Tulis Ulasan");
            btnKirim.setOnAction(e -> {
                ChoiceDialog<Integer> ratingDialog = new ChoiceDialog<>(5, 1, 2, 3, 4, 5);
                ratingDialog.setTitle("Beri Rating");
                ratingDialog.setHeaderText("Berapa bintang untuk tempat ini (1-5)?");
                ratingDialog.showAndWait().ifPresent(rating -> {
                    TextInputDialog komentarDialog = new TextInputDialog();
                    komentarDialog.setTitle("Beri Komentar");
                    komentarDialog.setHeaderText("Tuliskan pengalaman Anda:");
                    komentarDialog.showAndWait().ifPresent(komentar -> {
                        if(DataService.addUlasan(user.getId(), currentDestinasiId, rating, komentar)) {
                            showInfo("Sukses", "Ulasan berhasil dikirim!");
                            alert.close();
                        }
                    });
                });
            });
            vbox.getChildren().add(btnKirim);
        } else {
            vbox.getChildren().add(new Label("Login untuk menulis ulasan."));
        }
        
        alert.getDialogPane().setContent(vbox);
        alert.showAndWait();
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
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                String subject = "Tanya Destinasi: " + destinasiNamaLabel.getText();
                URI mailto = new URI("mailto:b6@wisata.id?subject=" + subject.replace(" ", "%20"));
                Desktop.getDesktop().mail(mailto);
            } else {
                showInfo("Chat", "Sistem email tidak didukung di perangkat ini. Hubungi: b6@wisata.id");
            }
        } catch (Exception e) {
            showError("Error", "Gagal membuka aplikasi email: " + e.getMessage());
        }
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
