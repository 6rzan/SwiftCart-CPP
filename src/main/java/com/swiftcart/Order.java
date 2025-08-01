package com.swiftcart;

/**
 * Order class represents an order in the SwiftCart system.
 * It contains an ID to uniquely identify each order and a regional zone for sorting.
 * A static POISON_PILL constant is used to signal the end of processing.
 */
public class Order {
    public static final Order POISON_PILL = new Order(-1, "POISON");
    private final int id;
    private final String regionalZone;

    public Order(int id, String regionalZone) {
        this.id = id;
        this.regionalZone = regionalZone;
    }

    public int getId() {
        return id;
    }

    public String getRegionalZone() {
        return regionalZone;
    }
}