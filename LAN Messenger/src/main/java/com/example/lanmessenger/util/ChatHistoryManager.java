package com.example.lanmessenger.util;

import com.example.lanmessenger.service.ClientHandler;

import java.io.*;
import java.util.*;

public class ChatHistoryManager {
    private static final String CHAT_FILE = "chats.txt";
    private static final String PRIVATE_FILE = "private_chats.txt";

    public static void loadChatHistory(List<String> messageHistory) {
        try (BufferedReader reader = new BufferedReader(new FileReader(CHAT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                messageHistory.add(line);
            }
            System.out.println("Loaded public chat history.");
        } catch (FileNotFoundException e) {
            System.out.println("No previous public chat history.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadPrivateChats(Map<String, List<String>> privateMessages) {
        try (BufferedReader reader = new BufferedReader(new FileReader(PRIVATE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 3);
                if (parts.length == 3) {
                    String user1 = parts[0];
                    String user2 = parts[1];
                    String msg = parts[2];
                    // keys are alphabetical
                    String key=ChatKeyUtils.getChatKey(user1,user2);
                    privateMessages.computeIfAbsent(key, k -> new ArrayList<>()).add(msg); // creates new list if that key is not present and adds msg in that list
                }
            }
            System.out.println("Loaded private chat history.");
        } catch (FileNotFoundException e) {
            System.out.println("No previous private chat history.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void savePublicMessage(String msg) {
        try (PrintWriter out = new PrintWriter(new FileWriter(CHAT_FILE, true))) {
            out.println(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void savePrivateMessage(String user1, String user2, String msg) {
        try (PrintWriter out = new PrintWriter(new FileWriter(PRIVATE_FILE, true))) {
            out.println(user1 + "|" + user2 + "|" + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
