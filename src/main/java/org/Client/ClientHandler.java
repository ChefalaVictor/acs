package org.Client;

import com.Product.Product;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final List<Product> products;
    private final Map<String, Integer> cart = new HashMap<>();
    private String clientName;

    public ClientHandler(Socket socket, List<Product> products) throws IOException {
        this.socket = socket;
        this.products = products;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            // Preia numele clientului
            clientName = in.readLine();
            System.out.println(clientName + " s-a conectat la server.");
            out.println("Bun venit la platforma de vânzări, " + clientName + "!");
            sendProductList();

            // Așteaptă comenzi
            while (true) {
                String input = in.readLine();
                if (input == null || input.equalsIgnoreCase("exit")) {
                    break;
                }

                System.out.println("Comandă primită de la client: " + input);
                processCommand(input);
            }
        } catch (IOException e) {
            System.err.println("Eroare la clientul " + clientName + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println(clientName + " s-a deconectat.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processCommand(String input) {
        String[] command = input.split(" ");
        if (command.length == 0) {
            out.println("Comandă invalidă. Comenzi disponibile: 'cumpar', 'cos', 'finalizeaza'.");
            return;
        }

        switch (command[0].toLowerCase()) {
            case "cumpar":
                handleBuyCommand(command);
                break;
            case "cos":
                viewCart();
                break;
            case "finalizeaza":
                finalizeOrder();
                break;
            default:
                out.println("Comandă invalidă. Comenzi disponibile: 'cumpar', 'cos', 'finalizeaza'.");
        }
    }

    private void handleBuyCommand(String[] command) {
        if (command.length < 3) {
            out.println("Format invalid. Folosește: 'cumpar <model> <cantitate>'");
            return;
        }

        // Obținem modelul și cantitatea
        String model = String.join(" ", Arrays.copyOfRange(command, 1, command.length - 1));
        try {
            int quantity = Integer.parseInt(command[command.length - 1]);
            Product product = findProductByModel(model.trim());

            if (product != null && product.getQuantity() >= quantity && quantity > 0) {
                cart.put(product.getModel(), cart.getOrDefault(product.getModel(), 0) + quantity);
                out.println(quantity + " x " + product.getName() + " (" + model + ") adăugate în coș.");
            } else {
                out.println("Produs indisponibil sau cantitate insuficientă.");
            }
        } catch (NumberFormatException e) {
            out.println("Cantitatea introdusă nu este validă.");
        }
    }

    private Product findProductByModel(String model) {
        return products.stream()
                .filter(product -> product.getModel().equalsIgnoreCase(model.trim()))
                .findFirst()
                .orElse(null);
    }

    private void viewCart() {
        if (cart.isEmpty()) {
            out.println("Coșul tău de cumpărături este gol.");
            return;
        }

        StringBuilder cartInfo = new StringBuilder("Produse în coș:\n");
        double total = 0.0;

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Product product = findProductByModel(entry.getKey());
            if (product != null) {
                double subtotal = entry.getValue() * product.getPrice();
                total += subtotal;
                cartInfo.append(entry.getValue()).append(" x ").append(product.getName())
                        .append(" (").append(product.getModel()).append(") - ").append(subtotal).append(" lei\n");
            }
        }
        cartInfo.append("Suma totală: ").append(total).append(" lei.");
        out.println(cartInfo.toString());
    }

    private void finalizeOrder() {
        if (cart.isEmpty()) {
            out.println("Coșul este gol. Adaugă produse înainte de a finaliza comanda.");
            return;
        }

        StringBuilder orderSummary = new StringBuilder("Rezumatul comenzii:\n");
        double totalValue = 0.0;

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Product product = findProductByModel(entry.getKey());
            if (product != null) {
                int quantity = entry.getValue();
                double value = quantity * product.getPrice();
                totalValue += value;

                // Actualizăm stocul
                product.reduceQuantity(quantity);

                // Adăugăm la rezumat
                orderSummary.append(quantity).append(" x ").append(product.getName())
                        .append(" (").append(product.getModel()).append(") - Total: ")
                        .append(value).append(" lei\n");
            }
        }

        // Curățăm coșul după plasarea comenzii
        cart.clear();

        // Trimitem mesaj clientului
        orderSummary.append("Suma totală: ").append(totalValue).append(" lei.");
        out.println(orderSummary.toString());

        // Afișăm în consolă pe server ce a fost cumpărat
        System.out.println("Clientul " + clientName + " a plasat o comandă.");
        System.out.println(orderSummary);

        // Trimitem lista de produse actualizată
        sendProductList();
    }


    public void sendStockUpdate(String stockInfo) {
        out.println(stockInfo);
    }

    private void sendProductList() {
        StringBuilder stockInfo = new StringBuilder("Produse disponibile:\n");

        for (Product product : products) {
            stockInfo.append(product.getName()).append(" (")
                    .append(product.getModel()).append(") - ")
                    .append(product.getPrice()).append(" lei, Stoc: ")
                    .append(product.getQuantity()).append("\n");
        }
        out.println(stockInfo.toString());
    }
}
