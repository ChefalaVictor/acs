package com.Product;

public class Product {
    private String name;
    private String model;
    private double price;
    private int quantity;

    public Product(String name, String model, double price, int quantity) {
        if (price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Prețul și cantitatea trebuie să fie pozitive.");
        }
        this.name = name;
        this.model = model;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Prețul nu poate fi negativ.");
        }
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Cantitatea nu poate fi negativă.");
        }
        this.quantity = quantity;
    }

    public void reduceQuantity(int quantity) {
        if (quantity > this.quantity) {
            throw new IllegalArgumentException("Cantitate insuficientă în stoc.");
        }
        this.quantity -= quantity;
    }

    @Override
    public String toString() {
        return name + " (" + model + ") - " + price + " lei - " + quantity + " buc.";
    }
}
