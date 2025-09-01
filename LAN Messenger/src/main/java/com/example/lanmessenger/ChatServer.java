package com.example.lanmessenger;

import com.example.lanmessenger.service.ClientHandler;
import com.example.lanmessenger.util.ChatHistoryManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer {

    private static final int PORT = 5000;
    //using collection.synchronized so that only one thread works at a time
    static final List<String> messageHistory = Collections.synchronizedList(new ArrayList<>());
    static final Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());
    static final Map<String, PrintWriter> userWriters = Collections.synchronizedMap(new HashMap<>());
    static final Map<String, List<String>> privateMessages = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        System.out.println("Server starting on port " + PORT + "...");

        // Load chat histories
        ChatHistoryManager.loadChatHistory(messageHistory);
        ChatHistoryManager.loadPrivateChats(privateMessages);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, messageHistory, clientWriters, userWriters, privateMessages);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
