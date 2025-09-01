package com.example.lanmessenger.util;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.Base64;

public class UserManager {
    private static final String FILE_PATH = "users.txt";

    private static void ensureFileExists() {
        try
        {
            File file = new File(FILE_PATH);
            if(!file.exists())
            {
                file.createNewFile();
            }
        }
        catch (IOException e)
        {
            System.out.println("Error creating file");
            e.printStackTrace();
        }
    }
    private static String hashPassword(String password)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes()); // digest gives hashed password
            return Base64.getEncoder().encodeToString(bytes);// encodes the hashed pass to base64 content to store in users.txt
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found.");
        }
    }
    public static boolean addUser(String username, String password)
    {
        ensureFileExists();
        if(userExists(username))
        {
            return false;
        }
        String hashedPass = hashPassword(password);
        try
        {
            FileWriter writer = new FileWriter(FILE_PATH, true);
            writer.write(username + ":" + hashedPass + "\n");
            writer.close();
            return true;
        }
        catch (IOException e)
        {
            System.out.println("Error writing to file.");
            e.printStackTrace();
            return false;
        }
    }
    public static boolean userExists(String username)
    {
        ensureFileExists();
        try
        {
            File file = new File(FILE_PATH);
            Scanner sc = new Scanner(file);
            while(sc.hasNextLine())
            {
                String line = sc.nextLine();
                String[] parts = line.split(":");
                if (parts.length >= 1 && parts[0].equals(username)) {
                    sc.close();
                    return true;
                }
            }
            sc.close();
        }
        catch (IOException e)
        {
            System.out.println("Error reading from file.");
            e.printStackTrace();
        }
        return false;
    }
    public static boolean validateUser(String username, String password) {
        ensureFileExists();
        try {
            File file = new File(FILE_PATH);
            Scanner sc = new Scanner(file);
            String hashedPass = hashPassword(password);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(hashedPass)) {
                    sc.close();
                    return true;
                }
            }
            sc.close();
        } catch (IOException e) {
            System.out.println("Error reading file.");
            e.printStackTrace();
        }
        return false;
    }
}
