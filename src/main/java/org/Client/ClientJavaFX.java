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

        messagesArea = new TextArea();
        messagesArea.setEditable(false);
        messagesArea.setWrapText(true); // Allow the text to wrap for better readability

        commandField = new TextField();
        commandField.setPromptText("Scrie o comandă...");

        Button sendButton = new Button("Trimite");
        Button viewCartButton = new Button("Vezi coșul");
        Button finalizeOrderButton = new Button("Finalizează comanda");

        HBox commandBox = new HBox(10, commandField, sendButton, viewCartButton, finalizeOrderButton);

        root.getChildren().addAll(messagesArea, commandBox);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Conectare la server automat
        connectToServer();

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

    private void connectToServer() {
        try {
            String userName = System.getProperty("username"); // Obține numele utilizatorului conectat
            Socket socket = new Socket("192.168.0.102", 12345); // IP-ul serverului tău
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Trimite numele utilizatorului către server
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
}
