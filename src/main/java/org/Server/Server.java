package org.Server;

import com.Product.Product.Product;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

public class Server {
    private static final List<Product> products = new ArrayList<>();
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final List<String> registeredUsers = new ArrayList<>();  // Păstrăm utilizatorii autentificați

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
            products.add(new Product("Căști", "Sony WH-1000XM4", 1200.0, 15));
            products.add(new Product("Căști", "Bose QuietComfort 35", 1100.0, 12));
            products.add(new Product("Smartwatch", "Apple Watch Series 8", 2500.0, 5));
            products.add(new Product("Smartwatch", "Samsung Galaxy Watch 5", 1800.0, 8));

            out.println("Serverul de vânzări a pornit...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                out.println("Un nou client s-a conectat.");
                ClientHandler clientHandler = new ClientHandler(clientSocket, products, registeredUsers);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Eroare la pornirea serverului: " + e.getMessage());
        }
    }




}
