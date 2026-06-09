package com.example;

import java.time.LocalDate;

/**
 * ItineraryDetail Model - Merepresentasikan detail destinasi dalam itinerary
 * 
 * @author Sistem Eksplorasi Wisata B6
 */
public class ItineraryDetail {
    private int id;
    private int itineraryId;
    private int destinasiId;
    private int urutan;
    private LocalDate tanggalKunjungan;
    private String catatan;
    
    // Untuk keperluan tampilan (tidak disimpan di database)
    private String namaDestinasi;
    private int hargaDestinasi;
    
    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Constructor tanpa parameter
     */
    public ItineraryDetail() {
    }
    
    /**
     * Constructor dengan parameter lengkap (dari database)
     */
    public ItineraryDetail(int id, int itineraryId, int destinasiId, int urutan,
                          LocalDate tanggalKunjungan, String catatan) {
        this.id = id;
        this.itineraryId = itineraryId;
        this.destinasiId = destinasiId;
        this.urutan = urutan;
        this.tanggalKunjungan = tanggalKunjungan;
        this.catatan = catatan;
    }
    
    /**
     * Constructor untuk menambah destinasi baru
     */
    public ItineraryDetail(int itineraryId, int destinasiId, int urutan) {
        this.itineraryId = itineraryId;
        this.destinasiId = destinasiId;
        this.urutan = urutan;
    }
    
    // ============================================================
    // GETTER DAN SETTER
    // ============================================================
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getItineraryId() { return itineraryId; }
    public void setItineraryId(int itineraryId) { this.itineraryId = itineraryId; }
    
    public int getDestinasiId() { return destinasiId; }
    public void setDestinasiId(int destinasiId) { this.destinasiId = destinasiId; }
    
    public int getUrutan() { return urutan; }
    public void setUrutan(int urutan) { this.urutan = urutan; }
    
    public LocalDate getTanggalKunjungan() { return tanggalKunjungan; }
    public void setTanggalKunjungan(LocalDate tanggalKunjungan) { this.tanggalKunjungan = tanggalKunjungan; }
    
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    
    public String getNamaDestinasi() { return namaDestinasi; }
    public void setNamaDestinasi(String namaDestinasi) { this.namaDestinasi = namaDestinasi; }
    
    public int getHargaDestinasi() { return hargaDestinasi; }
    public void setHargaDestinasi(int hargaDestinasi) { this.hargaDestinasi = hargaDestinasi; }
    
    // ============================================================
    // METHOD UTILITY
    // ============================================================
    
    /**
     * Format harga ke format Rupiah
     */
    public String getHargaFormatted() {
        if (hargaDestinasi == 0) return "Gratis";
        return "Rp " + String.format("%,d", hargaDestinasi).replace(",", ".");
    }
    
    // ============================================================
    // OVERRIDE METHOD
    // ============================================================
    
    @Override
    public String toString() {
        return "ItineraryDetail{" +
                "id=" + id +
                ", urutan=" + urutan +
                ", namaDestinasi='" + namaDestinasi + '\'' +
                ", tanggalKunjungan=" + tanggalKunjungan +
                '}';
    }
}
