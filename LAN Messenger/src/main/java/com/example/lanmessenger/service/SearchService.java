package com.example.lanmessenger.service;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SearchService {

    public static void searchMessages(String query, VBox vbox_messages, ScrollPane scrollpane_main) {
        boolean firstMatchFound = false;
        for (Node node : vbox_messages.getChildren()) {
            if (node instanceof HBox hbox) {
                boolean isOwnMessage=(hbox.getAlignment() == Pos.CENTER_RIGHT); // checks if the msg is on the right(own msg) or left side(others sent msgs)

                for (Node inner : hbox.getChildren()) {
                    if (inner instanceof VBox vbox) {
                        Label msgLabel = null;

                        for (Node child : vbox.getChildren()) {
                            if (child instanceof Label lbl && lbl.getFont().getSize() >= 13) { // as the msgs are 13px and the usernames are smaller
                                msgLabel = lbl;
                            }
                        }
                        if (msgLabel == null) continue;
                        String msgText = msgLabel.getText().toLowerCase();
                        // normal
                        if (isOwnMessage) {
                            msgLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 12px; -fx-background-color: #0078FF; -fx-text-fill: white;");
                        } else {
                            msgLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 12px; -fx-background-color: #44475A; -fx-text-fill: white;");
                        }

                        // Highlighted
                        if (!query.isEmpty() && msgText.contains(query)) {
                            msgLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 12px; -fx-background-color: yellow; -fx-text-fill: black;");
                            if (!firstMatchFound) {
                                firstMatchFound = true;
                                Platform.runLater(() -> {
                                    double contentHeight = vbox_messages.getHeight();
                                    double viewportHeight = scrollpane_main.getViewportBounds().getHeight();
                                    double y = hbox.getBoundsInParent().getMinY();
                                    double vvalue = (y - viewportHeight / 2) / (contentHeight - viewportHeight);
                                    vvalue = Math.max(0, Math.min(1, vvalue));
                                    scrollpane_main.setVvalue(vvalue);
                                });
                            }
                        }
                    }
                }
            }
        }
    }


    public static void toggleSearchBarVisibility(boolean visible, TextField searchHistory, TextField tf_message) {
        searchHistory.setVisible(visible);
        if (visible) {
            searchHistory.requestFocus();
        } else {
            tf_message.requestFocus();
        }
    }
}


