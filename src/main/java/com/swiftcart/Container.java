package com.swiftcart;

import java.util.List;

/**
 * Container class represents a container that holds a batch of orders.
 * It contains an ID to uniquely identify each container and a list of orders it holds.
 * A static POISON_PILL constant is used to signal the end of processing.
 */
public class Container {
    public static final Container POISON_PILL = new Container(null);
    private static int idCounter = 0;
    private final int id;
    private final List<Order> orders;

    public Container(List<Order> orders) {
        this.id = ++idCounter;
        this.orders = orders;
    }

    public int getId() {
        return id;
    }

    public List<Order> getOrders() {
        return orders;
    }
}