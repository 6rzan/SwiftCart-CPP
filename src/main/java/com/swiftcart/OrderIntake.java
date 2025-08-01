package com.swiftcart;

import java.util.concurrent.BlockingQueue;

/**
 * OrderIntake class simulates the intake of orders into the SwiftCart system.
 * It implements Runnable to allow it to run in a separate thread.
 * The OrderIntake generates 600 orders, each with a unique ID, and puts them into an intake queue.
 * After generating all orders, it sends POISON_PILL signals to stop processing.
 */
public class OrderIntake implements Runnable {
    private final BlockingQueue<Order> intakeQueue;

    public OrderIntake(BlockingQueue<Order> intakeQueue) {
        this.intakeQueue = intakeQueue;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 600; i++) {
                Order order = new Order(i);
                intakeQueue.put(order);
                System.out.println("OrderIntake: Created Order #" + order.getId() + " (Thread: " + Thread.currentThread().getName() + ")");
                Thread.sleep(500);
            }
            // After creating all orders, send poison pills to the pickers
            for (int i = 0; i < 4; i++) {
                intakeQueue.put(Order.POISON_PILL);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}