package com.example;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class BookingController implements Initializable {

    @FXML private Label destinasiNameLabel;
    @FXML private DatePicker tanggalKunjunganPicker;
    @FXML private RadioButton regulerRadio;
    @FXML private RadioButton vipRadio;
    @FXML private RadioButton premiumRadio;
    @FXML private Spinner<Integer> jumlahDewasaSpinner;
    @FXML private Spinner<Integer> jumlahAnakSpinner;
    @FXML private TextField namaTextField;
    @FXML private TextField emailTextField;
    @FXML private TextField teleponTextField;
    @FXML private TextArea catatanTextArea;
    @FXML private RadioButton tunaiRadio;
    @FXML private RadioButton transferRadio;
    @FXML private RadioButton eWalletRadio;
    @FXML private CheckBox agreementCheckBox;
    @FXML private CheckBox privacyCheckBox;
    @FXML private Label summaryDestinasiLabel;
    @FXML private Label summaryTanggalLabel;
    @FXML private Label summaryTipeTicketLabel;
    @FXML private Label summaryDewasaLabel;
    @FXML private Label summaryAnakLabel;
    @FXML private Label totalHargaLabel;
    @FXML private Button konfirmasiButton;

    private int hargaReguler = 50000;
    private int hargaVIP = 100000;
    private int hargaPremium = 150000;

    private static int currentDestinasiId = 1;
    private static String currentDestinasiNama = "Destinasi Wisata";

    public static void setDestinasiId(int id) {
        currentDestinasiId = id;
    }

    public static void setDestinasiNama(String nama) {
        currentDestinasiNama = nama;
    }

    public static void setBaseHarga(int harga) {
        if (harga > 0) {
            // Harga tiket dihitung dari harga dasar destinasi
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Destinasi d = DataService.getDestinasiById(currentDestinasiId);
        if (d != null) {
            currentDestinasiNama = d.getNama();
            int base = d.getHarga() > 0 ? d.getHarga() : 50000;
            hargaReguler = base;
            hargaVIP = (int) (base * 1.5);
            hargaPremium = base * 2;
        }

        destinasiNameLabel.setText(currentDestinasiNama);
        summaryDestinasiLabel.setText(currentDestinasiNama);

        User user = LoginController.getCurrentUser();
        if (user != null) {
            namaTextField.setText(user.getNamaLengkap());
            emailTextField.setText(user.getEmail());
            teleponTextField.setText(user.getNoTelepon());
        }

        jumlahDewasaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        jumlahAnakSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));

        tanggalKunjunganPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now().plusDays(1)));
            }
        });

        tanggalKunjunganPicker.valueProperty().addListener((obs, o, n) -> updateSummary());
        jumlahDewasaSpinner.valueProperty().addListener((obs, o, n) -> updateSummary());
        jumlahAnakSpinner.valueProperty().addListener((obs, o, n) -> updateSummary());

        if (regulerRadio.getToggleGroup() != null) {
            regulerRadio.getToggleGroup().selectedToggleProperty().addListener((obs, o, n) -> updateSummary());
        }

        updateSummary();
    }

    private void updateSummary() {
        LocalDate tgl = tanggalKunjunganPicker.getValue();
        summaryTanggalLabel.setText(tgl != null
                ? tgl.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                : "Pilih tanggal");

        int hargaSatuan = hargaReguler;
        String tipeNama = "Reguler";
        if (vipRadio.isSelected()) {
            hargaSatuan = hargaVIP;
            tipeNama = "VIP";
        }
        if (premiumRadio.isSelected()) {
            hargaSatuan = hargaPremium;
            tipeNama = "Premium";
        }
        summaryTipeTicketLabel.setText(tipeNama);

        int dewasa = jumlahDewasaSpinner.getValue();
        int anak = jumlahAnakSpinner.getValue();
        int totalDewasa = dewasa * hargaSatuan;
        int totalAnak = anak * (hargaSatuan / 2);
        int total = totalDewasa + totalAnak;

        summaryDewasaLabel.setText(DataService.formatRupiah(totalDewasa));
        summaryAnakLabel.setText(DataService.formatRupiah(totalAnak));
        totalHargaLabel.setText(DataService.formatRupiah(total));
    }

    @FXML
    private void handleBack() {
        try {
            App.setRoot("detail_destinasi");
        } catch (Exception e) {
            showError("Error", "Gagal kembali ke halaman detail.");
        }
    }

    @FXML
    private void handleKonfirmasi() {
        if (!agreementCheckBox.isSelected() || !privacyCheckBox.isSelected()) {
            showError("Validasi", "Harap setujui syarat & ketentuan serta kebijakan privasi.");
            return;
        }
        if (tanggalKunjunganPicker.getValue() == null) {
            showError("Validasi", "Pilih tanggal kunjungan terlebih dahulu.");
            return;
        }
        if (namaTextField.getText().trim().isEmpty()) {
            showError("Validasi", "Nama pemesan wajib diisi.");
            return;
        }
        if (emailTextField.getText().trim().isEmpty()) {
            showError("Validasi", "Email wajib diisi.");
            return;
        }
        if (teleponTextField.getText().trim().isEmpty()) {
            showError("Validasi", "Nomor telepon wajib diisi.");
            return;
        }

        String tipeTiket = "Reguler";
        int hargaSatuan = hargaReguler;
        if (vipRadio.isSelected()) {
            tipeTiket = "VIP";
            hargaSatuan = hargaVIP;
        } else if (premiumRadio.isSelected()) {
            tipeTiket = "Premium";
            hargaSatuan = hargaPremium;
        }

        int dewasa = jumlahDewasaSpinner.getValue();
        int anak = jumlahAnakSpinner.getValue();
        int total = (dewasa * hargaSatuan) + (anak * (hargaSatuan / 2));

        String metodeBayar = "Tunai di Lokasi";
        if (transferRadio.isSelected()) {
            metodeBayar = "Transfer Bank";
        } else if (eWalletRadio.isSelected()) {
            metodeBayar = "E-Wallet";
        }

        User user = LoginController.getCurrentUser();
        int userId = user != null ? user.getId() : 0;

        int pesananId = DataService.savePesanan(
                userId,
                currentDestinasiId,
                tanggalKunjunganPicker.getValue(),
                tipeTiket,
                dewasa,
                anak,
                namaTextField.getText().trim(),
                emailTextField.getText().trim(),
                teleponTextField.getText().trim(),
                catatanTextArea.getText().trim(),
                total,
                metodeBayar);

        if (pesananId > 0) {
            showInfo("Berhasil",
                    "Pesanan #" + pesananId + " berhasil dibuat!\n" +
                    "Total: " + DataService.formatRupiah(total) + "\n" +
                    "Status: Pending\n" +
                    "E-tiket akan dikirim ke " + emailTextField.getText().trim());
            try {
                App.setRoot("home");
            } catch (Exception e) {
                showError("Error", "Pesanan tersimpan tetapi gagal kembali ke home.");
            }
        } else {
            showError("Error",
                    "Gagal menyimpan pesanan ke database.\n" +
                    "Pastikan tabel 'pesanan' sudah dibuat. Jalankan schema.sql terbaru.");
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
