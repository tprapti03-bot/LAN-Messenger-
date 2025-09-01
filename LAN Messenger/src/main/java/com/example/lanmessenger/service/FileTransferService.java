package com.example.lanmessenger.service;

import javafx.application.Platform;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Base64;

public class FileTransferService {

    public static void sendFile(File file, PrintWriter writer, String senderUsername) {
        try {
            if (writer == null) return;
            writer.println("FILE:" + senderUsername + ":" + file.getName());
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String base64Content = Base64.getEncoder().encodeToString(fileBytes);
            writer.println(base64Content);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendPrivateFile(File file, PrintWriter writer, String senderUsername, String receiver)
    {
        try
        {
            if (writer == null) return;
            writer.println("PRIVATEFILE:" + receiver + ":" + file.getName()); //this is what server gets from the client
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String base64Content = Base64.getEncoder().encodeToString(fileBytes);
            writer.println(base64Content);
            writer.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void openFile(String fileName) {
        File file = new File("downloads", fileName);
        if (file.exists()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File not found: " + file.getAbsolutePath());
        }
    }

    static void saveFile(String fileName, byte[] data) {
        try {
            File dir = new File("downloads"); // from downloads file.
            if (!dir.exists()) dir.mkdirs(); // creates folder with parent directories if it doesnt exist
            File file = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
            }
            System.out.println("File saved: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
