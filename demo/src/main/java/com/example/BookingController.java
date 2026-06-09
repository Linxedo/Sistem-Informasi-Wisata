package com.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * BookingController — Controller untuk booking.fxml
 * Kelompok B6
 *
 * [DB] Terima destinasi_id, ambil harga dari tabel destinasi/jenis_tiket.
 *      Saat konfirmasi: INSERT INTO booking (...) VALUES (...) status='Pending'
 */
public class BookingController implements Initializable {

    @FXML private Label destinasiNameLabel;

    // Form fields
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

    // Summary labels
    @FXML private Label summaryDestinasiLabel;
    @FXML private Label summaryTanggalLabel;
    @FXML private Label summaryTipeTicketLabel;
    @FXML private Label summaryDewasaLabel;
    @FXML private Label summaryAnakLabel;
    @FXML private Label totalHargaLabel;

    @FXML private Button konfirmasiButton;

    // Harga (ambil dari DB berdasarkan tipe tiket)
    private int hargaReguler = 50000;
    private int hargaVIP     = 100000;
    private int hargaPremium = 150000;

    // Parameter dari halaman sebelumnya
    private static int currentDestinasiId = 1;
    private static String currentDestinasiNama = "Candi Borobudur";

    public static void setDestinasiId(int id) { currentDestinasiId = id; }
    public static void setDestinasiNama(String nama) { currentDestinasiNama = nama; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        destinasiNameLabel.setText(currentDestinasiNama);
        summaryDestinasiLabel.setText(currentDestinasiNama);

        // Setup spinner
        jumlahDewasaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        jumlahAnakSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));

        // Listener auto-update summary
        tanggalKunjunganPicker.valueProperty().addListener((obs, o, n) -> updateSummary());
        jumlahDewasaSpinner.valueProperty().addListener((obs, o, n) -> updateSummary());
        jumlahAnakSpinner.valueProperty().addListener((obs, o, n) -> updateSummary());

        if (regulerRadio.getToggleGroup() != null)
            regulerRadio.getToggleGroup().selectedToggleProperty().addListener((obs, o, n) -> updateSummary());

        // [DB] Ambil harga dari: SELECT harga FROM jenis_tiket WHERE destinasi_id = ?
        updateSummary();
    }

    /** Update summary box secara real-time */
    private void updateSummary() {
        // Tanggal
        LocalDate tgl = tanggalKunjunganPicker.getValue();
        summaryTanggalLabel.setText(tgl != null
            ? tgl.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            : "Pilih tanggal");

        // Tipe tiket & harga
        int hargaSatuan = hargaReguler;
        String tipeNama = "Reguler";
        if (vipRadio.isSelected())     { hargaSatuan = hargaVIP;     tipeNama = "VIP"; }
        if (premiumRadio.isSelected()) { hargaSatuan = hargaPremium; tipeNama = "Premium"; }
        summaryTipeTicketLabel.setText(tipeNama);

        // Jumlah
        int dewasa = jumlahDewasaSpinner.getValue();
        int anak   = jumlahAnakSpinner.getValue();
        int totalDewasa = dewasa * hargaSatuan;
        int totalAnak   = anak   * (hargaSatuan / 2);
        int total       = totalDewasa + totalAnak;

        summaryDewasaLabel.setText(formatRupiah(totalDewasa));
        summaryAnakLabel.setText(formatRupiah(totalAnak));
        totalHargaLabel.setText(formatRupiah(total));
    }

    private String formatRupiah(int amount) {
        return String.format("Rp %,d", amount).replace(",", ".");
    }

    @FXML
    private void handleBack() {
        try { App.setRoot("detail_destinasi"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleKonfirmasi() {
        // Validasi
        if (!agreementCheckBox.isSelected() || !privacyCheckBox.isSelected()) {
            System.out.println("[BookingController] Harap setujui syarat & ketentuan");
            return;
        }
        if (tanggalKunjunganPicker.getValue() == null) {
            System.out.println("[BookingController] Pilih tanggal kunjungan");
            return;
        }
        System.out.println("[BookingController] Konfirmasi pesanan");
        // [DB] INSERT INTO booking (user_id, destinasi_id, tanggal, tipe_tiket,
        //      jml_dewasa, jml_anak, total, metode_bayar, catatan, status)
        //      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pending')
    }
}
