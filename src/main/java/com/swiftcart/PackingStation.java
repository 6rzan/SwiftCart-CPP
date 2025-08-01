package com.swiftcart;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PackingStation class simulates a packing station that processes orders
 * It implements Runnable to allow it to run in a separate thread.
 * The packing station takes orders from a picking queue and puts them into a packing queue.
 * It uses a semaphore to control access to the packing process.
 * The packing station can reject orders with a 5% probability and keeps track of the number of rejected orders.
 * It will print messages to indicate its status and actions taken.
 */
public class PackingStation implements Runnable {
    private final BlockingQueue<Order> pickingQueue;
    private final BlockingQueue<Order> packingQueue;
    private final AtomicInteger ordersRejected;
    private final Random random = new Random();

    public PackingStation(BlockingQueue<Order> pickingQueue, BlockingQueue<Order> packingQueue, AtomicInteger ordersRejected) {
        this.pickingQueue = pickingQueue;
        this.packingQueue = packingQueue;
        this.ordersRejected = ordersRejected;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Order order = pickingQueue.take();
                if (order == Order.POISON_PILL) {
                    packingQueue.put(Order.POISON_PILL);
                    break;
                }
                System.out.println("PackingStation: Packing Order #" + order.getId() + " (Thread: " + Thread.currentThread().getName() + ")");
                if (random.nextDouble() < 0.05) {
                    ordersRejected.incrementAndGet();
                    System.out.println("PackingStation: Order #" + order.getId() + " rejected. (Thread: " + Thread.currentThread().getName() + ")");
                } else {
                    packingQueue.put(order);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}