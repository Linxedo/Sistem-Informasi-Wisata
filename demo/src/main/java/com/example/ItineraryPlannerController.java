package com.example;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * ItineraryPlannerController — Controller untuk itinerary_planner.fxml
 * Kelompok B6
 *
 * [DB] Query utama:
 *      SELECT id.jam_kunjungan, id.estimasi_jam, id.jumlah_tiket,
 *             d.nama, d.lokasi, d.harga
 *      FROM itinerary_detail id
 *      JOIN destinasi d ON d.id = id.destinasi_id
 *      WHERE id.itinerary_id = ?
 *      ORDER BY id.jam_kunjungan ASC
 */
public class ItineraryPlannerController implements Initializable {

    @FXML private Label namaRencanaLabel;
    @FXML private Label tanggalRencanaLabel;
    @FXML private Label durasiLabel;
    @FXML private Label pesertaLabel;
    @FXML private TextArea deskripsiTextArea;
    @FXML private VBox timelineVBox;
    @FXML private Label totalBiayaLabel;
    @FXML private TextField kodeUndanganField;
    @FXML private Label pesertaDetailLabel;
    @FXML private Label lastUpdateLabel;

    // [DB] Set itinerary_id dari navigasi sebelumnya
    private static int currentItineraryId = 1;

    public static void setItineraryId(int id) { currentItineraryId = id; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadItineraryData();
        // [DB] loadTimeline() — untuk mengisi timelineVBox secara programatik
        // Contoh: ItineraryPlannerController akan membuat HBox per item berdasarkan query DB
    }

    /**
     * [DB] Muat header itinerary dari database.
     * Query: SELECT nama, tanggal_mulai, tanggal_akhir, deskripsi, kode_unik
     *        FROM itinerary WHERE id = ?
     */
    private void loadItineraryData() {
        // TODO: Ganti dengan query PostgreSQL menggunakan currentItineraryId
        namaRencanaLabel.setText("Liburan ke Yogyakarta");
        tanggalRencanaLabel.setText("10 – 12 Juni 2024");
        durasiLabel.setText("3 Hari 2 Malam");
        pesertaLabel.setText("4 Orang");
        deskripsiTextArea.setText(
            "Perjalanan wisata budaya ke Yogyakarta, mengunjungi Borobudur, Prambanan, dan kuliner khas."
        );
        totalBiayaLabel.setText("Rp 500.000");
        if (kodeUndanganField != null) kodeUndanganField.setText("TRIP2024YOG");
        if (pesertaDetailLabel != null) pesertaDetailLabel.setText("4 orang (2 Dewasa, 2 Anak-anak)");
        if (lastUpdateLabel != null) lastUpdateLabel.setText("09 Juni 2024, 13:30 WIB");
    }

    @FXML private void handleBack() {
        try { App.setRoot("home"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleSimpan() {
        System.out.println("[ItineraryPlannerController] Simpan itinerary");
        // [DB] UPDATE itinerary SET nama=?, deskripsi=?, updated_at=NOW() WHERE id=?
    }

    @FXML private void handleBagikan() {
        System.out.println("[ItineraryPlannerController] Bagikan itinerary");
        // Salin kode undangan ke clipboard
    }

    @FXML private void handleTambahDestinasi() {
        System.out.println("[ItineraryPlannerController] Tambah destinasi ke itinerary");
        // TODO: Buka dialog pilih destinasi atau navigasi ke home
    }

    @FXML private void handleHapusItinerary() {
        System.out.println("[ItineraryPlannerController] Hapus itinerary");
        // [DB] DELETE FROM itinerary WHERE id = ?
    }

    @FXML private void handleCopyKode() {
        if (kodeUndanganField != null) {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(kodeUndanganField.getText());
            clipboard.setContent(content);
            System.out.println("[ItineraryPlannerController] Kode disalin: " + kodeUndanganField.getText());
        }
    }
}
