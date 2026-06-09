module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.logging;

    opens com.example to javafx.fxml;

    exports com.example;
}
