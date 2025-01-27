package org.Client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.sql.*;

public class ClientLoginApp extends Application {

    private static final String DB_URL = "jdbc:sqlite:users"; // Calea la baza de date SQLite
    private String userName;
    private TextArea messagesArea;
    private TextField commandField;
    private PrintWriter out;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Creare elemente UI pentru login
        Label titleLabel = new Label("Login/Register");
        titleLabel.setFont(Font.font("Arial", 24));
        titleLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        titleLabel.setPadding(new Insets(0, 0, 20, 0)); // Spacing below title

        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-text-fill: #333;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");


        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-text-fill: #333;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");


        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        registerButton.setOnAction(e -> handleRegister(usernameField.getText(), passwordField.getText()));

        // Layout pentru login
        VBox layout = new VBox(10, titleLabel, usernameLabel, usernameField, passwordLabel, passwordField, loginButton, registerButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Setare scena pentru login
        Scene scene = new Scene(layout, 350, 300);
        primaryStage.setTitle("Login/Register");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Te rog completeaza ambele câmpuri.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            String loginQuery = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = connection.prepareStatement(loginQuery)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.getInt(1) > 0) {
                    showAlert("Success", "Logare cu succes.");

                    // Conectare la server și deschidere interfață client cu username-ul
                    this.userName = username;  // Salvează username-ul
                    connectToServer();         // Conectează la server
                    openClientInterface();     // Deschide interfața clientului
                } else {
                    showAlert("Error", "Invalid username sau password.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private void handleRegister(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Te rog completeaza ambele câmpuri.");
            return;
        }

        // Password validation
        if (!isValidPassword(password)) {
            showAlert("Error", "Parola trebuie să conțină minim 8 caractere, o literă mare și o cifră.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            // Verificăm dacă utilizatorul există deja
            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(checkQuery)) {
                stmt.setString(1, username);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.getInt(1) > 0) {
                    showAlert("Error", "Username deja exista.");
                    return;
                }
            }

            // Inserăm utilizatorul în baza de date
            String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.executeUpdate();
                showAlert("Success", "Inregistrare cu succes.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    // Password validation method
    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.matches(".*[A-Z].*") && password.matches(".*\\d.*");
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("192.168.0.102", 12345);  // IP-ul serverului
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Trimite numele utilizatorului la server
            out.println(userName);

            // Thread pentru primirea mesajelor
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        final String receivedMessage = message;
                        Platform.runLater(() -> messagesArea.appendText(receivedMessage + "\n"));

                        // Actualizează lista de produse sau coș
                        if (receivedMessage.startsWith("PRODUCT_LIST")) {
                            updateProductList(receivedMessage);
                        } else if (receivedMessage.startsWith("CART_LIST")) {
                            updateCartList(receivedMessage);
                        }
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> messagesArea.appendText("Conexiunea cu serverul a fost întreruptă.\n"));
                }
            }).start();

        } catch (IOException e) {
            messagesArea.appendText("Eroare la conectarea cu serverul.\n");
        }
    }

    private void openClientInterface() {
        // Deschide fereastra pentru interfața clientului
        Stage clientStage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        messagesArea = new TextArea();
        messagesArea.setEditable(false);
        messagesArea.setWrapText(true); // Allow the text to wrap for better readability
        messagesArea.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

        commandField = new TextField();
        commandField.setPromptText("Scrie o comandă...");


        Button sendButton = new Button("Trimite");
        sendButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        Button viewCartButton = new Button("Vezi coșul");
        viewCartButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        Button finalizeOrderButton = new Button("Finalizează comanda");
        finalizeOrderButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white;");

        HBox commandBox = new HBox(10, commandField, sendButton, viewCartButton, finalizeOrderButton);
        root.getChildren().addAll(messagesArea, commandBox);

        Scene scene = new Scene(root, 600, 400);
        clientStage.setScene(scene);
        clientStage.show();

        // Evenimente pentru butoane
        sendButton.setOnAction(event -> sendCommand());
        viewCartButton.setOnAction(event -> sendViewCartCommand());
        finalizeOrderButton.setOnAction(event -> sendFinalizeOrderCommand());
    }

    private void sendCommand() {
        String command = commandField.getText().trim();
        if (!command.isEmpty() && out != null) {
            out.println(command);
            commandField.clear();
        }
    }

    private void sendViewCartCommand() {
        if (out != null) {
            out.println("cos");
        }
    }

    private void sendFinalizeOrderCommand() {
        if (out != null) {
            out.println("finalizeaza");
        }
    }

    private void updateProductList(String message) {
        String[] products = message.split("\n");
        for (String product : products) {
            if (!product.isEmpty() && !product.startsWith("PRODUCT_LIST")) {
                messagesArea.appendText("Produs: " + product + "\n");
            }
        }
    }

    private void updateCartList(String message) {
        String[] cartItems = message.split("\n");
        for (String item : cartItems) {
            if (!item.isEmpty() && !item.startsWith("CART_LIST")) {
                messagesArea.appendText("Coș: " + item + "\n");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
