package com.swiftcart;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

// PickingStation class simulates a picking station that processes orders
// It implements Runnable to allow it to run in a separate thread.
// The picking station takes orders from an intake queue and puts them into a picking queue.
// It uses a semaphore to control access to the picking process.
// The picking station can reject orders with a 5% probability and keeps track of the number of rejected orders.
// It also uses an AtomicInteger to track the number of pickers that have finished processing orders.
// The picking station will print messages to indicate its status and actions taken.
public class PickingStation implements Runnable {
    private final BlockingQueue<Order> intakeQueue;
    private final BlockingQueue<Order> pickingQueue;
    private final Semaphore pickingSemaphore;
    private final AtomicInteger ordersRejected;
    private final Random random = new Random();
    private static final AtomicInteger pickersFinished = new AtomicInteger(0);
    private final int totalPickers;

    public PickingStation(BlockingQueue<Order> intakeQueue, BlockingQueue<Order> pickingQueue, Semaphore pickingSemaphore, AtomicInteger ordersRejected, int totalPickers) {
        this.intakeQueue = intakeQueue;
        this.pickingQueue = pickingQueue;
        this.pickingSemaphore = pickingSemaphore;
        this.ordersRejected = ordersRejected;
        this.totalPickers = totalPickers;
    }

    // The run method is where the picking process takes place.
    // It continuously checks the intake queue for orders until it receives a POISON_PILL to stop processing.
    // It uses the semaphore to control access to the picking process, ensuring that only a limited number of pickers can process orders at the same time.
    // If an order is rejected, it increments the ordersRejected counter and prints a message.
    @Override
    public void run() {
        try {
            while (true) {
                pickingSemaphore.acquire();
                try {
                    Order order = intakeQueue.take();
                    if (order == Order.POISON_PILL) {
                        if (pickersFinished.incrementAndGet() == totalPickers) {
                            pickingQueue.put(Order.POISON_PILL); // Last one poisons the next queue
                        }
                        break; // Exit loop
                    }
                    System.out.println("PickingStation: Picking Order #" + order.getId() + " (Thread: " + Thread.currentThread().getName() + ")");
                    if (random.nextDouble() < 0.05) {
                        ordersRejected.incrementAndGet();
                        System.out.println("PickingStation: Order #" + order.getId() + " rejected. (Thread: " + Thread.currentThread().getName() + ")");
                    } else {
                        pickingQueue.put(order);
                    }
                } finally {
                    pickingSemaphore.release();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}