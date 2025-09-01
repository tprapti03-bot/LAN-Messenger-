package com.example.lanmessenger.controller;

import com.example.lanmessenger.service.*;
import com.example.lanmessenger.util.ChatKeyUtils;
import com.example.lanmessenger.util.MessageRenderer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.*;

public class ChatUi implements Initializable {
    @FXML
    private ImageView sendIcon;
    @FXML
    private TextField tf_message;
    @FXML
    private VBox vbox_messages;
    @FXML
    private ScrollPane scrollpane_main;
    @FXML
    private ImageView fileShareIcon;
    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> userList;
    @FXML
    private Label loggedInUsername;
    private Stage stage;
    private Parent root;
    private Socket socket;
    private PrintWriter writer;

    private String user;
    private String currentChat = "Room";
    private final ObservableList<String> allUsers = FXCollections.observableArrayList();
    private final ObservableList<String> baseItem = FXCollections.observableArrayList("Room");
    private final ObservableList<String> control = FXCollections.observableArrayList(baseItem);
    private FilteredList<String> filteredUsers;

    // map chatName= list of messages
    private final Map<String, List<String>> chatHistories = new HashMap<>();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //username search field implementation
        filteredUsers = new FilteredList<>(control, s -> true);
        userList.setItems(filteredUsers);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filteredUsers.setPredicate(item -> baseItem.contains(item) || item.toLowerCase().contains(newVal.toLowerCase())));

        userList.getSelectionModel().selectedItemProperty().addListener((o, oldV, newV) -> {
            if (newV != null) {
                currentChat = ChatKeyUtils.getChatKey(user, newV); // creates the chatkey of the user and the usrname he clicked
                loadMessages(currentChat); // load previous msges of inbox chats
            }
        });
        userList.getSelectionModel().select("Room");
        vbox_messages.setSpacing(2);
        vbox_messages.setPadding(new Insets(10));
        setupUserListCellFactory();
        // for search history :
        searchHistory.textProperty().addListener((obs, oldText, newText) -> {
            searchMessage(new ActionEvent());
        });
        // auto scroll down
        vbox_messages.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollpane_main.setVvalue(1.0);
        });

        OnlineStatusService.setUserList(userList);

    }
    public void connectToServer() {
        socket = ClientService.connectToServer(user, message -> {
            ClientService.handleServerMessage(message, user, currentChat, allUsers, baseItem, chatHistories, v -> UserListService.refreshUserList(allUsers, baseItem, control), this::addMessage);
        });
        try {
            if (socket != null) {
                writer = new PrintWriter(socket.getOutputStream(), true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void LoggedInUser(String username) {
        this.user = username;
        connectToServer();
        allUsers.clear();
        UserListService.loadAllUsers(user, allUsers);
        UserListService.refreshUserList(allUsers, baseItem, control);
        currentChat = "Room";
        loadMessages(currentChat);
        loggedInUsername.setText(user); // shows the username of the client that logged in
        Platform.runLater(() -> tf_message.requestFocus());
    }

    private void loadMessages(String chatName) {
        vbox_messages.getChildren().clear();
        List<String> messages = chatHistories.getOrDefault(chatName, new ArrayList<>());
        for (String msg : messages) {
            addMessage(msg);
        }
    }
    //  calling message renderer to add message in the chatbox
    public void addMessage(String message) {
        Platform.runLater(() -> {
            MessageRenderer.renderMessage(message, user, vbox_messages, scrollpane_main, this::openFile);
            Platform.runLater(() -> scrollpane_main.setVvalue(1.0));
        });
    }

    public void sendText(Event e) {
        String msg = tf_message.getText();
        if (!msg.isEmpty() && writer != null) {
            if (currentChat.equals("Room")) {
                writer.println(user + ": " + msg);
            }
            else
            {
                String receiver = ChatKeyUtils.extractOtherUserFromChatKey(currentChat, user);
                writer.println("PRIVATE:" + receiver + ":" + msg); //sends PRIVATE:RECEIVER:MSG to the server
            }
            tf_message.clear();
        }
    }
     //file transfer service :
     private void sendFile(File file)
     {
        if(currentChat.equals("Room")) {
            FileTransferService.sendFile(file, writer, user);
        }
        else
        {
            String receiver =ChatKeyUtils.extractOtherUserFromChatKey(currentChat, user);
            FileTransferService.sendPrivateFile(file, writer, user, receiver);
        }
     }

     private void openFile(String fileName) {
        FileTransferService.openFile(fileName);
     }

   // using the file sharing icon
    @FXML
    private void handleFileShare(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");
        File selectedFile = fileChooser.showOpenDialog(fileShareIcon.getScene().getWindow());
        if (selectedFile != null) {
            sendFile(selectedFile);
        }
    }
   //using the logout button
    @FXML
    private void LogOut(ActionEvent event) throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        userList.getItems().remove(user);
        root = FXMLLoader.load(getClass().getResource("/com/example/lanmessenger/Login.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
    // emoji unicode black&white
    @FXML
    private ImageView emojiIcon;
    private Popup emojiPopup;
    @FXML
    private void openEmojiPicker(MouseEvent event) {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.hide();
            return;
        }
        emojiPopup= new Popup();
        GridPane emojiGrid = new GridPane();
        emojiGrid.setHgap(5);
        emojiGrid.setVgap(5);
        emojiGrid.setPadding(new Insets(5));
        emojiGrid.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                emojiPopup.hide();
            }
        });
        String[] emojis = {"😀", "😂", "😍", "😎", "😢", "😡", "👍", "🙏", "🎉", "💯", "🔥", "🤔", "😴", "😇", "🌟","❤"};
        int cols = 4;
        for (int i = 0; i < emojis.length; i++) {
            String emoji = emojis[i];
            Button btn = new Button(emoji);
            btn.setStyle("-fx-font-size: 18;");
            btn.setPrefSize(43, 43);
            btn.setOnAction(e -> {
                tf_message.insertText(tf_message.getCaretPosition(), emoji);
                emojiPopup.hide();
            });
            emojiGrid.add(btn, i % cols, i / cols);
        }

        emojiPopup.getContent().add(emojiGrid); //
        emojiPopup.setAutoHide(true);
        // Show popup near the emoji icon
        emojiPopup.show(emojiIcon.getScene().getWindow(),
                emojiIcon.localToScreen(0, 0).getX(),
                emojiIcon.localToScreen(0, 0).getY() - 200);
    }


    // Search history work(Prapti)
    @FXML
    private ImageView searchIcon;
    @FXML
    private TextField searchHistory;
    //using the search icon
    @FXML
    private void SeachBarToggle(MouseEvent e) {
        boolean visible = !searchHistory.isVisible();
        SearchService.toggleSearchBarVisibility(visible, searchHistory, tf_message);
    }
    // using the search bar
    @FXML
    public void searchMessage(ActionEvent e) {
        String m = searchHistory.getText().toLowerCase();
        Platform.runLater(() -> SearchService.searchMessages(m, vbox_messages, scrollpane_main));
    }

    // listview deco manually here
    private void setupUserListCellFactory() {
        userList.setCellFactory(lv -> new ListCell<>() {
            private final Circle statusCircle = new Circle(6); // indicator circle radius 6 px

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: #021429;");
                } else {
                    setText(item);

                    // base text color
                    String baseStyle = "-fx-background-color: #021429;";
                    if (item.equals("Room")) {
                        baseStyle += "-fx-text-fill: #c9fffe; -fx-font-weight: bold;";
                        // For Room
                        setGraphic(null);
                    } else {
                        baseStyle += "-fx-text-fill: white;";
                        //online status and set circle color
                        boolean online = OnlineStatusService.isOnline(item);
                        if (online) {
                            statusCircle.setFill(Color.LIMEGREEN);  // online = green
                        } else {
                            statusCircle.setFill(Color.GRAY);  // offline = gray
                        }
                        setGraphic(statusCircle);
                    }

                    if (isSelected()) {
                        baseStyle = "-fx-background-color: #2e3b58; -fx-text-fill: white;";
                        if (item.equals("Room")) baseStyle += " -fx-font-weight: bold;";
                    }

                    setStyle(baseStyle);
                }
            }
        });
    }

}



