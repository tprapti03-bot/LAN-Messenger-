package com.example.lanmessenger.controller;

import com.example.lanmessenger.util.UserManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import java.io.IOException;

public class SignUpPage {
    private Stage stage;
    private Parent root;
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField confirmpassword;
    @FXML
    private Label lblmsg;
    @FXML
    private void switchToChatScene(ActionEvent event) throws IOException {
        String user = username.getText();
        String pass = password.getText();
        String cPass = confirmpassword.getText();
        if (user.isEmpty() || pass.isEmpty() || cPass.isEmpty()) {
            lblmsg.setText("All fields are required.");
            return;
        }
        if (!pass.equals(cPass)) {
            lblmsg.setText("Passwords do not match.");
            return;
        }
        boolean userAdded = UserManager.addUser(user, pass);
        if(userAdded) {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lanmessenger/Chat.fxml"));
            root=loader.load();
            ChatUi chatcontoller=(ChatUi)loader.getController();
            chatcontoller.LoggedInUser(user);
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        }
        else {
            lblmsg.setText("Username already exists.");
        }
    }

    @FXML
    private void switchToLoginFromSignUp(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/com/example/lanmessenger/Login.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
