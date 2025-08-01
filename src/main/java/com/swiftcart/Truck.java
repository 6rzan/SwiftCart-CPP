package com.swiftcart;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Truck class simulates a truck that loads containers from a loading queue
 * and dispatches them once full or when the process is complete.
 * It implements Runnable to allow it to run in a separate thread.
 * The truck can hold up to 18 containers and will wait for containers to be available.
 * It also tracks the time taken to load the first container and the total wait time.
 * The truck will print messages to indicate its status and actions taken.
 * The truck uses a CountDownLatch to signal when it has finished loading containers,
 * allowing the main program to wait for all trucks to finish before proceeding.
 */


public class Truck implements Runnable {
    private static int idCounter = 0;
    private final int id;
    private final BlockingQueue<Container> loadingQueue;
    private final CountDownLatch latch;
    private final AtomicInteger trucksDispatched;
    private final List<Container> containers = new ArrayList<>();
    private long startTime;
    private long firstContainerTime = -1;

    public Truck(BlockingQueue<Container> loadingQueue, CountDownLatch latch, AtomicInteger trucksDispatched) {
        this.id = ++idCounter;
        this.loadingQueue = loadingQueue;
        this.latch = latch;
        this.trucksDispatched = trucksDispatched;
        this.startTime = System.currentTimeMillis();
    }
    // The run method is where the truck's loading process takes place.
    // It continuously checks the loading queue for containers until it has loaded 18 containers or the queue is empty.
    // It also handles the case where it receives a POISON_PILL to stop processing.
    @Override
    public void run() {
        System.out.println("Truck-" + id + ": Arrived at loading bay. (Thread: " + Thread.currentThread().getName() + ")");
        try {
            while (containers.size() < 18) {
                System.out.println("Truck-" + id + ": Waiting for a container. (Thread: " + Thread.currentThread().getName() + ")");
                Container container = loadingQueue.take();
                if (container == Container.POISON_PILL) {
                    break;
                }
                if (firstContainerTime == -1) {
                    firstContainerTime = System.currentTimeMillis();
                }
                containers.add(container);
                System.out.println("Truck-" + id + ": Loaded Container #" + container.getId() + ". Total: " + containers.size() + "/18 (Thread: " + Thread.currentThread().getName() + ")");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
             if (containers.size() == 18) {
                System.out.println("Truck-" + id + ": Full. Departing. (Thread: " + Thread.currentThread().getName() + ")");
                trucksDispatched.incrementAndGet();
            } else if (containers.size() > 0) {
                 System.out.println("Truck-" + id + ": Force dispatched with " + containers.size() + " containers. (Thread: " + Thread.currentThread().getName() + ")");
                 trucksDispatched.incrementAndGet();
            } else {
                System.out.println("Truck-" + id + ": Shutting down empty. (Thread: " + Thread.currentThread().getName() + ")");
            }
            latch.countDown();
        }
    }
    // Getters for truck ID and the number of containers loaded
    // These methods allow other parts of the program to access the truck's ID and the number of containers it has loaded.
    public long getWaitTime() {
        if (firstContainerTime == -1) {
            return 0;
        }
        return firstContainerTime - startTime;
    }
}