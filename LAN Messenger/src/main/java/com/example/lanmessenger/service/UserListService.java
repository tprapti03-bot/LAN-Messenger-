package com.example.lanmessenger.service;

import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class UserListService {

    public static void loadAllUsers(String currentUser, ObservableList<String> allUsers) {
        allUsers.clear();
        try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 1 && !parts[0].equals(currentUser)) {
                    allUsers.add(parts[0]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void refreshUserList(ObservableList<String> allUsers, ObservableList<String> room, ObservableList<String> targetList) {
        targetList.clear();
        targetList.addAll(room);
        targetList.addAll(allUsers);
    }
}
