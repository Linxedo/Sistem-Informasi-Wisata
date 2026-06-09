package com.example;

import java.time.LocalDate;

/**
 * Itinerary Model - Merepresentasikan rencana perjalanan dari tabel itinerary
 * 
 * @author Sistem Eksplorasi Wisata B6
 */
public class Itinerary {
    private int id;
    private int userId;
    private String namaRencana;
    private int totalBiaya;
    private LocalDate tanggalMulai;
    private LocalDate tanggalSelesai;
    private String deskripsi;
    private String status;           // Draft, Finalized, Completed
    
    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Constructor tanpa parameter
     */
    public Itinerary() {
        this.status = "Draft";
        this.totalBiaya = 0;
    }
    
    /**
     * Constructor dengan parameter lengkap
     */
    public Itinerary(int id, int userId, String namaRencana, int totalBiaya,
                     LocalDate tanggalMulai, LocalDate tanggalSelesai, 
                     String deskripsi, String status) {
        this.id = id;
        this.userId = userId;
        this.namaRencana = namaRencana;
        this.totalBiaya = totalBiaya;
        this.tanggalMulai = tanggalMulai;
        this.tanggalSelesai = tanggalSelesai;
        this.deskripsi = deskripsi;
        this.status = status;
    }
    
    /**
     * Constructor dengan parameter minimal
     */
    public Itinerary(int userId, String namaRencana) {
        this.userId = userId;
        this.namaRencana = namaRencana;
        this.status = "Draft";
        this.totalBiaya = 0;
    }
    
    // ============================================================
    // GETTER DAN SETTER
    // ============================================================
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getNamaRencana() { return namaRencana; }
    public void setNamaRencana(String namaRencana) { this.namaRencana = namaRencana; }
    
    public int getTotalBiaya() { return totalBiaya; }
    public void setTotalBiaya(int totalBiaya) { this.totalBiaya = totalBiaya; }
    
    public LocalDate getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(LocalDate tanggalMulai) { this.tanggalMulai = tanggalMulai; }
    
    public LocalDate getTanggalSelesai() { return tanggalSelesai; }
    public void setTanggalSelesai(LocalDate tanggalSelesai) { this.tanggalSelesai = tanggalSelesai; }
    
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    // ============================================================
    // METHOD UTILITY
    // ============================================================
    
    /**
     * Format total biaya ke format Rupiah
     */
    public String getTotalBiayaFormatted() {
        return "Rp " + String.format("%,d", totalBiaya).replace(",", ".");
    }
    
    /**
     * Hitung durasi perjalanan
     */
    public long getDurasiHari() {
        if (tanggalMulai != null && tanggalSelesai != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(tanggalMulai, tanggalSelesai) + 1;
        }
        return 0;
    }
    
    // ============================================================
    // OVERRIDE METHOD
    // ============================================================
    
    @Override
    public String toString() {
        return "Itinerary{" +
                "id=" + id +
                ", userId=" + userId +
                ", namaRencana='" + namaRencana + '\'' +
                ", totalBiaya=" + totalBiaya +
                ", status='" + status + '\'' +
                '}';
    }
}
