package com.swiftcart;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LabellingStation class simulates a labelling station that processes orders from a packing queue.
 * It implements Runnable to allow it to run in a separate thread.
 * The labelling station takes orders from a packing queue and puts them into a labelling queue.
 * It can reject orders with a 5% probability and keeps track of the number of rejected orders.
 * It will print messages to indicate its status and actions taken.
 */
public class LabellingStation implements Runnable {
    private final BlockingQueue<Order> packingQueue;
    private final BlockingQueue<Order> labellingQueue;
    private final AtomicInteger ordersRejected;
    private final Random random = new Random();

    public LabellingStation(BlockingQueue<Order> packingQueue, BlockingQueue<Order> labellingQueue, AtomicInteger ordersRejected) {
        this.packingQueue = packingQueue;
        this.labellingQueue = labellingQueue;
        this.ordersRejected = ordersRejected;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Order order = packingQueue.take();
                if (order == Order.POISON_PILL) {
                    labellingQueue.put(Order.POISON_PILL);
                    break;
                }
                System.out.println("LabellingStation: Labelling Order #" + order.getId() + " (Thread: " + Thread.currentThread().getName() + ")");
                if (random.nextDouble() < 0.05) {
                    ordersRejected.incrementAndGet();
                    System.out.println("LabellingStation: Order #" + order.getId() + " rejected. (Thread: " + Thread.currentThread().getName() + ")");
                } else {
                    labellingQueue.put(order);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}