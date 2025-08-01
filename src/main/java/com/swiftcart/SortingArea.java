
package com.swiftcart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        Map<String, List<Order>> regionalBatches = new HashMap<>();
        List<List<Order>> readyBatches = new ArrayList<>();

        try {
            while (true) {
                Order order = labellingQueue.take();

                if (order == Order.POISON_PILL) {
                    // Process remaining orders
                    for (List<Order> batch : regionalBatches.values()) {
                        if (!batch.isEmpty()) {
                            readyBatches.add(new ArrayList<>(batch));
                        }
                    }
                    if (!readyBatches.isEmpty()) {
                        List<Order> finalBoxes = new ArrayList<>();
                        for (List<Order> batch : readyBatches) {
                            finalBoxes.addAll(batch);
                        }
                        if (!finalBoxes.isEmpty()) {
                            Container container = new Container(finalBoxes);
                            System.out.println("SortingArea: Created final Container #" + container.getId() + " with " + finalBoxes.size() + " boxes. (Thread: " + Thread.currentThread().getName() + ")");
                            sortingQueue.put(container);
                            containersShipped.incrementAndGet();
                        }
                    }
                    for (int i = 0; i < 3; i++) {
                        sortingQueue.put(Container.POISON_PILL);
                    }
                    break;
                }

                boxesPacked.incrementAndGet();
                regionalBatches.computeIfAbsent(order.getRegionalZone(), k -> new ArrayList<>()).add(order);

                List<Order> batch = regionalBatches.get(order.getRegionalZone());
                if (batch.size() == 6) {
                    readyBatches.add(new ArrayList<>(batch));
                    System.out.println("SortingArea: Batch of 6 for zone " + order.getRegionalZone() + " is ready. (Thread: " + Thread.currentThread().getName() + ")");
                    batch.clear();

                    if (readyBatches.size() == 5) {
                        List<Order> containerBoxes = new ArrayList<>();
                        for (List<Order> readyBatch : readyBatches) {
                            containerBoxes.addAll(readyBatch);
                        }
                        Container container = new Container(containerBoxes);
                        System.out.println("SortingArea: Created Container #" + container.getId() + " with 30 boxes from 5 batches. (Thread: " + Thread.currentThread().getName() + ")");
                        sortingQueue.put(container);
                        containersShipped.incrementAndGet();
                        readyBatches.clear();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
