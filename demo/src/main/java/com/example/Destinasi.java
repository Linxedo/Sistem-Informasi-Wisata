package com.example;

/**
 * Destinasi Model - Merepresentasikan data wisata dari tabel destinasi
 * 
 * @author Sistem Eksplorasi Wisata B6
 */
public class Destinasi {
    private int id;
    private String nama;
    private String kategori;
    private int harga;
    private String deskripsi;
    private String koordinat;
    private String lokasi;
    private double rating;
    private String gambarUrl;
    
    // ============================================================
    // CONSTRUCTOR
    // ============================================================
    
    /**
     * Constructor tanpa parameter
     */
    public Destinasi() {
    }
    
    /**
     * Constructor dengan parameter lengkap
     */
    public Destinasi(int id, String nama, String kategori, int harga, String deskripsi,
                     String koordinat, String lokasi, double rating, String gambarUrl) {
        this.id = id;
        this.nama = nama;
        this.kategori = kategori;
        this.harga = harga;
        this.deskripsi = deskripsi;
        this.koordinat = koordinat;
        this.lokasi = lokasi;
        this.rating = rating;
        this.gambarUrl = gambarUrl;
    }
    
    /**
     * Constructor dengan parameter minimal
     */
    public Destinasi(String nama, String kategori, int harga, String lokasi) {
        this.nama = nama;
        this.kategori = kategori;
        this.harga = harga;
        this.lokasi = lokasi;
    }
    
    // ============================================================
    // GETTER DAN SETTER
    // ============================================================
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    
    public int getHarga() { return harga; }
    public void setHarga(int harga) { this.harga = harga; }
    
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    
    public String getKoordinat() { return koordinat; }
    public void setKoordinat(String koordinat) { this.koordinat = koordinat; }
    
    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }
    
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    
    public String getGambarUrl() { return gambarUrl; }
    public void setGambarUrl(String gambarUrl) { this.gambarUrl = gambarUrl; }
    
    // ============================================================
    // METHOD UTILITY
    // ============================================================
    
    /**
     * Format harga ke format Rupiah
     */
    public String getHargaFormatted() {
        if (harga == 0) return "Gratis";
        return "Rp " + String.format("%,d", harga).replace(",", ".");
    }
    
    /**
     * Format rating dengan simbol bintang
     */
    public String getRatingFormatted() {
        return String.format("%.1f ★", rating);
    }
    
    // ============================================================
    // OVERRIDE METHOD
    // ============================================================
    
    @Override
    public String toString() {
        return "Destinasi{" +
                "id=" + id +
                ", nama='" + nama + '\'' +
                ", kategori='" + kategori + '\'' +
                ", harga=" + harga +
                ", lokasi='" + lokasi + '\'' +
                ", rating=" + rating +
                '}';
    }
}
