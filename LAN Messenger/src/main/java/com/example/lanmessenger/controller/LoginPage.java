package com.example.lanmessenger.controller;

import com.example.lanmessenger.util.UserManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginPage {
    private Stage stage;
    private Parent root;
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Label lblmsg;
    @FXML
    private void handleLogin(ActionEvent event) throws IOException {
        String name = username.getText().trim();
        String pass = password.getText();

        if (name.isEmpty() || pass.isEmpty()) {
            lblmsg.setText("Please enter username and password.");
            return;
        }
        boolean valid= UserManager.validateUser(name, pass);
        if (valid) {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lanmessenger/Chat.fxml"));
            root=loader.load();
            ChatUi chatcontoller=(ChatUi)loader.getController();
            chatcontoller.LoggedInUser(name);
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        }
        else {
            lblmsg.setText("Invalid username or password.");
        }
    }
    @FXML
    private void switchToSignUpFromLogin(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/com/example/lanmessenger/SignUp.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
