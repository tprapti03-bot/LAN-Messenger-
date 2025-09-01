package com.example.lanmessenger.service;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import java.util.HashSet;
import java.util.Set;

public class OnlineStatusService {
    private static final Set<String> onlineUsers = new HashSet<>();
    private static ListView<String> userList;

    public static void setUserList(ListView<String> listView) {
        userList = listView;
    }

    public static boolean isOnline(String username) {
        return onlineUsers.contains(username) || username.equals("Room");
    }

    public static void userCameOnline(String username) {
        Platform.runLater(() -> {
            onlineUsers.add(username);
            if (userList != null) userList.refresh();
        });
    }

    public static void userWentOffline(String username) {
        Platform.runLater(() -> {
            onlineUsers.remove(username);
            if (userList != null) userList.refresh();
        });
    }
}
