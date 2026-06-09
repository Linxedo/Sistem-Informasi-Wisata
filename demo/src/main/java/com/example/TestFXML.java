package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class TestFXML extends Application {
    @Override
    public void start(Stage stage) {
        System.out.println("Testing Login FXML...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            loader.load();
            System.out.println("Login FXML loaded successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\nTesting Dashboard FXML...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
            loader.load();
            System.out.println("Dashboard FXML loaded successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        System.exit(0);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
