package org.Client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

public class Register extends Application {

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

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> handleRegister(usernameField.getText(), passwordField.getText()));

        // Layout
        VBox layout = new VBox(10, usernameLabel, usernameField, passwordLabel, passwordField, registerButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Setare scena
        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setTitle("Register");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleRegister(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill in both fields.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            // Verificăm dacă utilizatorul există deja
            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(checkQuery)) {
                stmt.setString(1, username);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.getInt(1) > 0) {
                    showAlert("Error", "Username already exists.");
                    return;
                }
            }

            // Inserăm utilizatorul în baza de date
            String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.executeUpdate();
                showAlert("Success", "Registration successful.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
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
