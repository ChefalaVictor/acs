package org.Server;

import com.Product.Product;
import org.Client.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final List<Product> products = new ArrayList<>();
    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            // Adăugăm produse în listă
            products.add(new Product("Telefon", "Samsung Galaxy S23", 5000.0, 10));
            products.add(new Product("Telefon", "iPhone 14", 6000.0, 8));
            products.add(new Product("Telefon", "Xiaomi Redmi Note 12", 1500.0, 20));
            products.add(new Product("Laptop", "Dell Inspiron", 4000.0, 5));
            products.add(new Product("Laptop", "MacBook Air M2", 8000.0, 3));
            products.add(new Product("Tableta", "iPad Air", 3500.0, 7));
            products.add(new Product("Tableta", "Samsung Galaxy Tab S8", 3200.0, 10));

            System.out.println("Serverul de vânzări a pornit...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Un nou client s-a conectat.");
                ClientHandler clientHandler = new ClientHandler(clientSocket, products);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Eroare la pornirea serverului: " + e.getMessage());
        }
    }

    public static void sendStockUpdateToAllClients() {
        StringBuilder stockInfo = new StringBuilder("STOCK_UPDATE:\n");

        for (Product product : products) {
            stockInfo.append(product.getName())
                    .append(" - Preț: ").append(product.getPrice())
                    .append(", Stoc: ").append(product.getQuantity())
                    .append("\n");
        }

        for (ClientHandler clientHandler : clients) {
            clientHandler.sendStockUpdate(stockInfo.toString());
        }
    }
}
