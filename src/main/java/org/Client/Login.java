package org.Client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

public class Login extends Application {

    private static final String DB_URL = "jdbc:sqlite:users.db"; // Calea la baza de date SQLite

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Creare elemente UI
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> openRegisterForm());

        // Layout
        VBox layout = new VBox(10, usernameLabel, usernameField, passwordLabel, passwordField, loginButton, registerButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Setare scena
        Scene scene = new Scene(layout, 300, 250);
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in both fields.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            // Verificăm dacă utilizatorul există și dacă parola corespunde
            String loginQuery = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = connection.prepareStatement(loginQuery)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.getInt(1) > 0) {
                    showAlert("Success", "Login successful.");
                } else {
                    showAlert("Error", "Invalid username or password.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private void openRegisterForm() {
        Register register = new Register();
        Stage registerStage = new Stage();
        try {
            register.start(registerStage);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open registration form.");
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
