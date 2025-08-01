package com.swiftcart;

/**
 * Order class represents an order in the SwiftCart system.
 * It contains an ID to uniquely identify each order.
 * A static POISON_PILL constant is used to signal the end of processing.
 */
public class Order {
    public static final Order POISON_PILL = new Order(-1);
    private final int id;

    public Order(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}