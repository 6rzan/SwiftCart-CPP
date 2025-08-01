
package com.swiftcart;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SortingArea class simulates a sorting area that processes orders from a labelling queue.
 * It implements Runnable to allow it to run in a separate thread.
 * The sorting area takes orders from a labelling queue and puts them into a sorting queue.
 */
public class SortingArea implements Runnable {
    private final BlockingQueue<Order> labellingQueue;
    private final BlockingQueue<Container> sortingQueue;
    private final AtomicInteger containersShipped;
    private final AtomicInteger boxesPacked;

    public SortingArea(BlockingQueue<Order> labellingQueue, BlockingQueue<Container> sortingQueue, AtomicInteger containersShipped, AtomicInteger boxesPacked) {
        this.labellingQueue = labellingQueue;
        this.sortingQueue = sortingQueue;
        this.containersShipped = containersShipped;
        this.boxesPacked = boxesPacked;
    }

    @Override
    public void run() {
        List<Order> batch = new ArrayList<>();
        try {
            while (true) {
                Order order = labellingQueue.take();
                if (order == Order.POISON_PILL) {
                    // Process any remaining orders in the batch before stopping
                    if (!batch.isEmpty()) {
                        Container container = new Container(new ArrayList<>(batch));
                        System.out.println("SortingArea: Created final Container #" + container.getId() + " with " + batch.size() + " boxes. (Thread: " + Thread.currentThread().getName() + ")");
                        sortingQueue.put(container);
                        containersShipped.incrementAndGet();
                    }
                    // Poison the next queue for each Loader thread
                    for (int i = 0; i < 3; i++) {
                        sortingQueue.put(Container.POISON_PILL);
                    }
                    break;
                }
                batch.add(order);
                boxesPacked.incrementAndGet();
                if (batch.size() == 30) {
                    Container container = new Container(new ArrayList<>(batch));
                    System.out.println("SortingArea: Created Container #" + container.getId() + " with 30 boxes. (Thread: " + Thread.currentThread().getName() + ")");
                    sortingQueue.put(container);
                    containersShipped.incrementAndGet();
                    batch.clear();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
