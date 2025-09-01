module com.example.lanmessenger {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.lanmessenger to javafx.fxml;
    exports com.example.lanmessenger;
    exports com.example.lanmessenger.service;
    opens com.example.lanmessenger.service to javafx.fxml;
    exports com.example.lanmessenger.controller;
    opens com.example.lanmessenger.controller to javafx.fxml;
    exports com.example.lanmessenger.util;
    opens com.example.lanmessenger.util to javafx.fxml;
}