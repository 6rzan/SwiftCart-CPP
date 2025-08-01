package com.swiftcart;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Loader class simulates a loader that moves containers from a sorting queue to a loading queue.
 * It implements Runnable to allow it to run in a separate thread.
 * The loader can handle multiple threads and uses semaphores to control access to the loading process.
 * It also simulates potential breakdowns with a 10% chance, causing the loader to sleep for 5 seconds.
 * The loader will print messages to indicate its status and actions taken.
 */
public class Loader implements Runnable {
    private final BlockingQueue<Container> sortingQueue;
    private final BlockingQueue<Container> loadingQueue;
    private final Semaphore loaderSemaphore;
    private final Semaphore loadingBaySemaphore;
    private final Random random = new Random();
    private static final AtomicInteger loadersFinished = new AtomicInteger(0);
    private final int totalLoaders;
    private final int totalTrucks;

    public Loader(BlockingQueue<Container> sortingQueue, BlockingQueue<Container> loadingQueue, Semaphore loaderSemaphore, Semaphore loadingBaySemaphore, int totalLoaders, int totalTrucks) {
        this.sortingQueue = sortingQueue;
        this.loadingQueue = loadingQueue;
        this.loaderSemaphore = loaderSemaphore;
        this.loadingBaySemaphore = loadingBaySemaphore;
        this.totalLoaders = totalLoaders;
        this.totalTrucks = totalTrucks;
    }

    @Override
    public void run() {
        try {
            while (true) {
                loaderSemaphore.acquire();
                try {
                    Container container = sortingQueue.take();
                    if (container == Container.POISON_PILL) {
                        if (loadersFinished.incrementAndGet() == totalLoaders) {
                            for (int i = 0; i < totalTrucks; i++) {
                                loadingQueue.put(Container.POISON_PILL);
                            }
                        }
                        break;
                    }

                    System.out.println(Thread.currentThread().getName() + ": Moving Container #" + container.getId() + " to Loading Bay (Thread: " + Thread.currentThread().getName() + ")");

                    if (random.nextDouble() < 0.1) {
                        System.out.println(Thread.currentThread().getName() + ": Loader broke down! Sleeping for 5 seconds. (Thread: " + Thread.currentThread().getName() + ")");
                        Thread.sleep(5000);
                    }

                    loadingBaySemaphore.acquire();
                    try {
                        System.out.println(Thread.currentThread().getName() + ": Loading Container #" + container.getId() + " onto a truck. (Thread: " + Thread.currentThread().getName() + ")");
                        loadingQueue.put(container);
                    } finally {
                        loadingBaySemaphore.release();
                    }
                } finally {
                    loaderSemaphore.release();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}