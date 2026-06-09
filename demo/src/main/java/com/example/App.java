package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * App - Main Class untuk Sistem Eksplorasi Wisata
 * 
 * Fitur:
 * - Inisialisasi JavaFX application
 * - Test koneksi database saat startup
 * - Load halaman login sebagai first screen
 * - Apply dark theme CSS
 * 
 * @author Sistem Eksplorasi Wisata B6
 */
public class App extends Application {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        LOGGER.log(Level.INFO, "Starting Sistem Eksplorasi Wisata Application");
        
        // ========== TEST DATABASE CONNECTION ==========
        System.out.println("═══════════════════════════════════════════");
        System.out.println("  SISTEM EKSPLORASI WISATA - LOGIN");
        System.out.println("═══════════════════════════════════════════");
        System.out.println();
        System.out.println("Checking database connection...");
        
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        if (!dbHelper.testConnection()) {
            System.out.println();
            System.out.println("⚠️  WARNING: Database connection failed!");
            System.out.println("Pastikan:");
            System.out.println("1. PostgreSQL server sudah berjalan");
            System.out.println("2. Database 'wisata_db' sudah dibuat");
            System.out.println("3. Konfigurasi DB_URL, DB_USER, DB_PASSWORD di DatabaseHelper.java");
            System.out.println();
        }
        System.out.println();
        
        // ========== LOAD HOME FXML (Halaman Utama) ==========
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("home.fxml"));
        Parent root = fxmlLoader.load();
        
        // ========== CREATE SCENE WITH DARK THEME CSS ==========
        scene = new Scene(root, 1400, 900);
        
        // Apply dark theme CSS (style.css)
        String css = getClass().getResource("style.css").toExternalForm();
        scene.getStylesheets().add(css);
        
        // ========== SETUP STAGE ==========
        stage.setTitle("Sistem Informasi Wisata - Kelompok B6");
        stage.setScene(scene);
        stage.setWidth(1400);
        stage.setHeight(900);
        stage.setResizable(true);
        stage.setOnCloseRequest(e -> {
            LOGGER.log(Level.INFO, "Application closed");
            System.exit(0);
        });
        
        stage.show();
        
        LOGGER.log(Level.INFO, "Application started successfully");
    }

    /**
     * Set root scene dengan FXML file baru
     * 
     * @param fxml Nama file FXML (tanpa ekstensi .fxml)
     * @throws IOException jika gagal load FXML
     */
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Load FXML file
     * 
     * @param fxml Nama file FXML (tanpa ekstensi .fxml)
     * @return Parent node dari FXML
     * @throws IOException jika gagal load FXML
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /**
     * Main method - entry point
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("Launching Sistem Eksplorasi Wisata...");
        System.out.println("═══════════════════════════════════════════");
        launch();
    }

}