package com.example.lanmessenger.service;

import com.example.lanmessenger.util.ChatHistoryManager;
import com.example.lanmessenger.util.ChatKeyUtils;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.*;

/// HANDLES SERVER SIDE THREAD FOR INDIVIDUAL CLIENT
//handles the data sent by a client

public class ClientHandler extends Thread {
    private final Socket socket;
    private final List<String> messageHistory;
    private final Set<PrintWriter> clientWriters;
    private final Map<String, PrintWriter> userWriters;
    private final Map<String, List<String>> privateMessages;

    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket, List<String> messageHistory,
                         Set<PrintWriter> clientWriters, Map<String, PrintWriter> userWriters,
                         Map<String, List<String>> privateMessages) {
        this.socket = socket;
        this.messageHistory = messageHistory;
        this.clientWriters = clientWriters;
        this.userWriters = userWriters;
        this.privateMessages = privateMessages;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) //server reads from client
        {
            out = new PrintWriter(socket.getOutputStream(), true); //object to send messages to the client

            String loginLine = in.readLine(); //read line from the client
            if (loginLine != null && loginLine.startsWith("LOGIN:")) //if that line starts with LOGIN
            {
                username = loginLine.substring(6).trim();
                userWriters.put(username, out); //users
                clientWriters.add(out); //writers for users

                synchronized (messageHistory) {
                    for (int i = 0; i < messageHistory.size(); i++) {
                        String m = messageHistory.get(i);
                        out.println(m);
                        // checks if file content is there
                        if (m.startsWith("FILE:") && i + 1 < messageHistory.size()) {
                            String content = messageHistory.get(i + 1);
                            out.println(content);
                            i++; // skip next line containing base64 content
                        }
                    }
                }

                synchronized (userWriters) {
                    for (String onlineUser : userWriters.keySet()) {
                        if (!onlineUser.equals(username)) {
                            out.println("NEW_USER:" + onlineUser); //  tells the user that these other users are already online
                        }
                    }
                }


                for (String key : privateMessages.keySet()) {
                    String[] parts = key.split("\\|");
                    if (parts.length == 2 && (parts[0].equals(username) || parts[1].equals(username))) {
                        for (String pm : privateMessages.get(key)) {
                            out.println("PRIVATECHAT:" + key + ":" + pm); //send the client the private chat
                        }
                    } else if (parts.length == 3 && parts[2].startsWith("FILE:") &&
                            (parts[0].equals(username) || parts[1].equals(username))) {
                        String filename = parts[2].substring(5);
                        String otherUser = parts[0].equals(username) ? parts[1] : parts[0];
                        for (String content : privateMessages.get(key))
                        {
                            out.println("PRIVATEFILE:" + otherUser + ":" + filename); //send PRIVATEFILE:receiver:filename to the client
                            out.println(content);
                        }
                    }
                }

                for (PrintWriter pw : clientWriters) {
                    if (pw != out) {
                        pw.println("NEW_USER:" + username);
                    }
                }

            }

            String msg;
            while ((msg = in.readLine()) != null) //listen what client says until null
            {
                if (msg.startsWith("PRIVATE:")) //jodi client PRIVATE:receiver:msg pathay
                    // tahole handlePrivateMessage onujayi server client k PRIVATECHAT:key:formatted pathay
                {
                    handlePrivateMessage(msg);
                }
                else if (msg.startsWith("PRIVATEFILE:"))
                {
                    handlePrivateFile(msg, in);
                }
                else if (msg.startsWith("FILE:")) //send files to public room
                {
                    handlePublicFile(msg,in);
                }
                else if (msg.startsWith("USER_LOGOUT:")) {
                    String offlineUser = msg.substring("USER_LOGOUT:".length()).trim();
                    Platform.runLater(() -> {
                        OnlineStatusService.userWentOffline(offlineUser);
                    });
                }
                else
                { //send text to the public room
                    broadcast(msg);
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + username);
        } finally {
            if (username != null) {
                userWriters.remove(username);
                //notifying logout message to others if a user logsout
                for (PrintWriter pw : clientWriters) {
                    if (pw != out) {
                        pw.println("USER_LOGOUT:" + username);
                    }
                }
            }
            clientWriters.remove(out);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void handlePrivateMessage(String msg) { //msg = PRIVATE:receiver:text
        String[] parts = msg.split(":", 3);
        if (parts.length < 3) return;

        String receiver = parts[1].trim();
        String text = parts[2].trim();
        String key=ChatKeyUtils.getChatKey(username, receiver);
        String formatted = username + ": " + text;

        privateMessages.computeIfAbsent(key, k -> new ArrayList<>()).add(formatted);
        ChatHistoryManager.savePrivateMessage(username, receiver, formatted);

        PrintWriter receiverOut = userWriters.get(receiver);
        if (receiverOut != null) {
            receiverOut.println("PRIVATECHAT:" + key + ":" + formatted);
        }
        out.println("PRIVATECHAT:" + key + ":" + formatted); //server sends PRIVATECHAT:key:formatted to the client
    }

    private void handlePrivateFile(String msg, BufferedReader in) throws IOException {
        String[] parts = msg.split(":", 3); //msg comes from client
        //format: PRIVATEFILE:receiver:filename and file content in next line
        if (parts.length < 3) return;

        String receiver = parts[1].trim();
        String fileName = parts[2].trim();

        String pair=ChatKeyUtils.getChatKey(username, receiver);

        String formatted = "FILE:" + username + ":" + fileName;

        privateMessages.computeIfAbsent(pair, k -> new ArrayList<>()).add(formatted);  //checks if that pair exixts in the map  if not creates the new pair and then add private msg for  that userpair
        ChatHistoryManager.savePrivateMessage(username, receiver, formatted);

        String content = in.readLine();

        PrintWriter receiverOut = userWriters.get(receiver);
        if (receiverOut != null) {
            receiverOut.println("PRIVATEFILE:" + pair + ":" + formatted);
            receiverOut.println(content);
        }
        out.println("PRIVATEFILE:" + pair + ":" + formatted);
        //server client k PRIVATEFILE:key:username:filename pathay
        //formatted-> username:filename
        out.println(content);
    }
    public void handlePublicFile(String msg,BufferedReader in) throws IOException {

        String content = in.readLine(); // taking the base64 content as it is in the next line of msges that strts wilth FILE/PRiVATEFILE
        synchronized (messageHistory) {
            messageHistory.add(msg);
            messageHistory.add(content);
        }
        ChatHistoryManager.savePublicMessage(msg);
       // ChatHistoryManager.savePublicMessage(content);
        for (PrintWriter pw : clientWriters) {
            pw.println(msg);      // Send FILE:sender:filename
            pw.println(content);  // Send file base64
        }
    }
    private void broadcast(String message) {
        synchronized (messageHistory) {
            messageHistory.add(message);
        }
        ChatHistoryManager.savePublicMessage(message);

        for (PrintWriter pw : clientWriters) {
            pw.println(message);
        }
    }
}
