package com.example.lanmessenger.service;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/// CONNECTS CLIENT TO SERVER

public class ClientService {

    public static Socket connectToServer(String user, Consumer<String> messageHandler) {
        try {
            Socket socket = new Socket("localhost", 5000);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("LOGIN:" + user);

            Thread readerThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("PRIVATEFILE:") || line.startsWith("FILE:")) {
                            String fileContent = reader.readLine();  // read next line for base64 data
                            String combined = line + "\n" + fileContent;
                            Platform.runLater(() -> messageHandler.accept(combined));
                        } else {
                            String finalLine = line;
                            Platform.runLater(() -> messageHandler.accept(finalLine));
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();
            return socket;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void handleServerMessage( //handles the messages sent by the server like if a users logsin or logsout or any msges
            String message,
            String user,
            String currentChat,
            List<String> allUsers,
            List<String> baseItem,
            Map<String, List<String>> chatHistories,
            Consumer<Void> refreshUserList,
            Consumer<String> addMessage
    ) {
        // these are for list view , online//offline status etc
        if (message.startsWith("NEW_USER:")) {
            String newUser = message.substring("NEW_USER:".length()).trim();
            if (!newUser.equals(user)) {
                if (!allUsers.contains(newUser)) {
                    allUsers.add(newUser);
                    refreshUserList.accept(null);
                }
                Platform.runLater(() -> {
                    OnlineStatusService.userCameOnline(newUser);
                });
            }
        }
        else if (message.startsWith("USER_LOGOUT:")) {
            String offlineUser = message.substring("USER_LOGOUT:".length()).trim(); // getting the username of the person that logged out
            Platform.runLater(() -> {
                OnlineStatusService.userWentOffline(offlineUser); // turning off the online status
            });
        }
        // scrollpane load msges
        else if (message.startsWith("PRIVATECHAT:"))//if server sends PRIVATECHAT:key:username:text
        {
            String[] parts = message.split(":", 3);
            if (parts.length == 3) {
                String chatKey = parts[1];
                String msg = parts[2]; //sender:text

                chatHistories.computeIfAbsent(chatKey, k -> new ArrayList<>()).add(msg);
                if (chatKey.equals(currentChat)) {
                    addMessage.accept(msg);
                }
            }
        }

        else if(message.startsWith("PRIVATEFILE:"))  //if server sends PRIVATEFILE:KEY:FILE:username:filename
        {
            String[] parts = message.split("\n", 2);
            String header = parts[0];
            String base64Content = parts.length > 1 ? parts[1] : "";

            String[] headerParts = header.split(":", 3);
            if (headerParts.length == 3) {
                String chatKey = headerParts[1];
                String formattedMsg = headerParts[2];  //FILE:sender:filename
                chatHistories.computeIfAbsent(chatKey, k -> new ArrayList<>()).add(formattedMsg);

                // Split formattedMsg into sender and filename
                String[] fileParts = formattedMsg.split(":", 3);

                String sender = fileParts[1].trim();
                String fileName = fileParts[2].trim();

                // Decode and save file
                byte[] fileData = Base64.getDecoder().decode(base64Content);
                FileTransferService.saveFile(fileName, fileData);

                if (chatKey.equals(currentChat)) {
                    addMessage.accept(formattedMsg);
                }
            }
        }
        else if(message.startsWith("FILE:")) //client gets FILE:sender:filename from server
        {
            String[] parts = message.split("\n", 2);
            String header = parts[0]; // the first line that contains FILE:sender:filename
            String encoded = parts.length > 1 ? parts[1] : ""; //base64 encoded content
            String[] data = header.split(":",3);

            if (data.length == 3) {
                String sender = data[1].trim();
                String fileName = data[2].trim();
                byte[] fileData = Base64.getDecoder().decode(encoded);
                FileTransferService.saveFile(fileName, fileData);
                chatHistories.computeIfAbsent("Room", k -> new ArrayList<>()).add(header);
                if (currentChat.equals("Room")){
                    addMessage.accept(header);
                }
            }
         }
        else {
            chatHistories.computeIfAbsent("Room", k -> new ArrayList<>()).add(message);
            if (currentChat.equals("Room")) {
                addMessage.accept(message);
            }
        }
    }



}
