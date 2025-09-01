package com.example.lanmessenger.util;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;


import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

public class MessageRenderer {

    public static boolean renderMessage( //renders messages sent from the server to the client
            String message,
            String currentUser,
            VBox messageContainer,
            ScrollPane scrollPane,
            Consumer<String> onOpenFile
    )
    {
        // Handle file header message: "FILE:sender:fileName" from history
        if (message.startsWith("FILE:"))
        {
            String[] parts = message.split(":", 3);
            if (parts.length < 3) return false;

            String sender = parts[1].trim();
            String localFileName = parts[2].trim();

            // User label
            Label userLabel = new Label(sender);
            userLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #CCCCCC;");

            // Message text label
            Label msgLabel = new Label("Sent a file: ");
            msgLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

            // Hyperlink to download file
            Hyperlink fileLink = new Hyperlink(localFileName);
            fileLink.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
            fileLink.setOnAction(e -> onOpenFile.accept(localFileName));

            HBox hboxContent = new HBox(msgLabel, fileLink);
            hboxContent.setSpacing(5);
            hboxContent.setAlignment(Pos.CENTER_LEFT);

            // chat bubble
            VBox bubble = new VBox(hboxContent);
            bubble.setPadding(new Insets(10));
            bubble.setMaxWidth(350);
            bubble.setStyle("-fx-background-radius: 12px;");

            // bubble colours
            if (sender.equals(currentUser)) {
                bubble.setStyle(bubble.getStyle() + "-fx-background-color: #0078FF;");
                hboxContent.setAlignment(Pos.CENTER_RIGHT);
            } else {
                bubble.setStyle(bubble.getStyle() + "-fx-background-color: #44475A;");
                hboxContent.setAlignment(Pos.CENTER_LEFT);
            }

            // Final message box with user label and bubble
            VBox vbox_msg = new VBox(userLabel, bubble);
            vbox_msg.setSpacing(2);

            HBox hbox_msg = new HBox(vbox_msg);
            hbox_msg.setPadding(new Insets(5, 10, 5, 10));
            hbox_msg.setAlignment(sender.equals(currentUser) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            // Add to container
            messageContainer.getChildren().add(hbox_msg);
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
            return true;
        }
        // normal chats sender:message
        String[] parts = message.split(":", 2);
        if (parts.length < 2) return false;
        String sender = parts[0].trim();
        String msg = parts[1].trim();

        Label userLabel = new Label(sender);
        userLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #CCCCCC;");

        Label msgLabel = new Label(msg);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(350);
        msgLabel.setMinHeight(Label.USE_PREF_SIZE);
        msgLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 12px;");

        VBox vbox_msg = new VBox(userLabel, msgLabel);
        vbox_msg.setSpacing(2);

        HBox hbox_msg = new HBox(vbox_msg);
        hbox_msg.setPadding(new Insets(5, 10, 5, 10));

        if (sender.equals(currentUser)) {
            msgLabel.setStyle(msgLabel.getStyle() + "-fx-background-color: #0078FF; -fx-text-fill: white;");
            hbox_msg.setAlignment(Pos.CENTER_RIGHT);
        } else {
            msgLabel.setStyle(msgLabel.getStyle() + "-fx-background-color: #44475A; -fx-text-fill: white;");
            hbox_msg.setAlignment(Pos.CENTER_LEFT);
        }

        messageContainer.getChildren().add(hbox_msg);
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
        return true;
    }
}
