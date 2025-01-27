package org.Client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ClientJavaFX extends Application {
    private TextArea messagesArea;
    private TextField commandField;
    private PrintWriter out;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Configurare UI
        primaryStage.setTitle("Client Platforma de Vânzări");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label nameLabel = new Label("Introdu numele tău:");
        TextField nameField = new TextField();

        Button connectButton = new Button("Conectează-te");
        messagesArea = new TextArea();
        messagesArea.setEditable(false);

        commandField = new TextField();
        commandField.setPromptText("Scrie o comandă...");

        Button sendButton = new Button("Trimite");
        Button viewCartButton = new Button("Vezi coșul");
        Button finalizeOrderButton = new Button("Finalizează comanda");

        HBox commandBox = new HBox(10, commandField, sendButton, viewCartButton, finalizeOrderButton);

        root.getChildren().addAll(nameLabel, nameField, connectButton, messagesArea, commandBox);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Eveniment pentru conectare
        connectButton.setOnAction(event -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                connectToServer(name);
                connectButton.setDisable(true);
                nameField.setDisable(true);
            } else {
                messagesArea.appendText("Te rog să introduci un nume valid.\n");
            }
        });

        // Eveniment pentru trimiterea comenzilor
        sendButton.setOnAction(event -> sendCommand());
        viewCartButton.setOnAction(event -> sendViewCartCommand());
        finalizeOrderButton.setOnAction(event -> sendFinalizeOrderCommand());
    }

    private void connectToServer(String name) {
        try {
            Socket socket = new Socket("192.168.0.102", 12345); // IP-ul serverului tău
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Trimite numele clientului
            out.println(name);

            // Thread pentru primirea mesajelor
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        final String receivedMessage = message;
                        Platform.runLater(() -> messagesArea.appendText(receivedMessage + "\n"));

                        // Verificăm dacă mesajul este o actualizare de stoc
                        if (receivedMessage.startsWith("STOCK_UPDATE")) {
                            Platform.runLater(() -> updateStock(receivedMessage));
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


    private void updateStock(String message) {
        // Extragem informațiile din mesaj și actualizăm zona de text
        String[] parts = message.split(":");
        if (parts.length > 1) {
            messagesArea.appendText("Actualizare stoc:\n" + parts[1] + "\n");
        }
    }
}
