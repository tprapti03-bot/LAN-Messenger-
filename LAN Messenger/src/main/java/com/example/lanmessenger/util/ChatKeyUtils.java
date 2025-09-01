package com.example.lanmessenger.util;

public class ChatKeyUtils {

    public static String getChatKey(String user1, String user2) {
        if (user2.equals("Room")) return "Room";
        return (user1.compareTo(user2) < 0) ? user1 + "|" + user2 : user2 + "|" + user1;
    }

    public static String extractOtherUserFromChatKey(String chatKey, String currentUser) {
        if (chatKey.equals("Room")) return "Room";
        String[] parts = chatKey.split("\\|");
        return parts[0].equals(currentUser) ? parts[1] : parts[0]; // gets the other user , for inbox texting
    }
}
