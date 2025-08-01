package com.swiftcart;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SwiftCartSimulation class is the entry point for the SwiftCart simulation.
 * It sets up the necessary queues, semaphores, and threads to simulate the order processing system.
 * The simulation runs for a specified duration and prints out statistics at the end.
 */
public class SwiftCartSimulation {

    public static void main(String[] args) throws InterruptedException {
        // Shared resources
        BlockingQueue<Order> intakeQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Order> pickingQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Order> packingQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Order> labellingQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Container> sortingQueue = new ArrayBlockingQueue<>(10);
        BlockingQueue<Container> loadingQueue = new LinkedBlockingQueue<>();

        Semaphore pickingSemaphore = new Semaphore(4);
        Semaphore loadingBaySemaphore = new Semaphore(2);
        Semaphore loaderSemaphore = new Semaphore(3);

        AtomicInteger ordersProcessed = new AtomicInteger(0);
        AtomicInteger ordersRejected = new AtomicInteger(0);
        AtomicInteger boxesPacked = new AtomicInteger(0);
        AtomicInteger containersShipped = new AtomicInteger(0);
        AtomicInteger trucksDispatched = new AtomicInteger(0);

        // Executor service
        ExecutorService executor = Executors.newFixedThreadPool(15);

        // Start simulation
        long startTime = System.currentTimeMillis();
        System.out.println("--- SwiftCart Simulation Started ---");

        // Create and start threads
        Future<?> intakeFuture = executor.submit(new OrderIntake(intakeQueue));

        int totalPickers = 4;
        for (int i = 0; i < totalPickers; i++) {
            executor.submit(new PickingStation(intakeQueue, pickingQueue, pickingSemaphore, ordersRejected, totalPickers));
        }

        executor.submit(new PackingStation(pickingQueue, packingQueue, ordersRejected));
        executor.submit(new LabellingStation(packingQueue, labellingQueue, ordersRejected));
        executor.submit(new SortingArea(labellingQueue, sortingQueue, containersShipped, boxesPacked));

        int totalOrders = 600;
        int ordersPerContainer = 30;
        int containersPerTruck = 18;
        int totalContainers = totalOrders / ordersPerContainer;
        int totalTrucks = (int) Math.ceil((double) totalContainers / containersPerTruck);

        int totalLoaders = 3;
        for (int i = 0; i < totalLoaders; i++) {
            executor.submit(new Loader(sortingQueue, loadingQueue, loaderSemaphore, loadingBaySemaphore, totalLoaders, totalTrucks));
        }

        CountDownLatch truckLatch = new CountDownLatch(totalTrucks);
        java.util.List<Truck> trucks = new java.util.ArrayList<>();
        for (int i = 0; i < totalTrucks; i++) {
            Truck truck = new Truck(loadingQueue, truckLatch, trucksDispatched);
            trucks.add(truck);
            executor.submit(truck);
        }

        // Shutdown executor gracefully
        executor.shutdown();
        try {
            // Wait for all threads to finish their work.
            if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                System.err.println("Executor did not terminate in the specified time.");
                executor.shutdownNow(); // Force shutdown if graceful period expires
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Wait for the trucks to finish their final processing
        truckLatch.await();

        // End simulation
        long endTime = System.currentTimeMillis();
        long totalTimeSeconds = (endTime - startTime) / 1000;

        // Print final statistics
        System.out.println("\n" + "=".repeat(60));
        System.out.println("         SWIFTCART SIMULATION RESULTS");
        System.out.println("=".repeat(60));
        System.out.printf("Simulation Duration: %.2f minutes%n", totalTimeSeconds / 60.0);
        System.out.printf("Total Orders Processed: %d%n", boxesPacked.get() + ordersRejected.get());
        System.out.printf("Orders Rejected: %d (%.1f%%)%n",
            ordersRejected.get(), (ordersRejected.get() * 100.0) / totalOrders);
        System.out.printf("Boxes Packed: %d%n", boxesPacked.get());
        System.out.printf("Containers Created: %d%n", containersShipped.get());
        System.out.printf("Trucks Dispatched: %d%n", trucksDispatched.get());

        if (trucksDispatched.get() > 0) {
            long maxWaitTime = 0;
            long minWaitTime = Long.MAX_VALUE;
            long totalWaitTime = 0;
            int trucksWithWaitTime = 0;
            for (Truck truck : trucks) {
                if (truck.getWaitTime() > 0) {
                    long waitTime = truck.getWaitTime();
                    if (waitTime > maxWaitTime) {
                        maxWaitTime = waitTime;
                    }
                    if (waitTime < minWaitTime) {
                        minWaitTime = waitTime;
                    }
                    totalWaitTime += waitTime;
                    trucksWithWaitTime++;
                }
            }
            if (trucksWithWaitTime > 0) {
                System.out.printf("Truck Wait Times - Max: %.2f seconds, Min: %.2f seconds, Average: %.2f seconds%n",
                    maxWaitTime / 1000.0, minWaitTime / 1000.0, (totalWaitTime / (double)trucksWithWaitTime) / 1000.0);
            }
        }

        if (totalTimeSeconds > 0) {
            double ordersPerMinute = (boxesPacked.get() * 60.0) / totalTimeSeconds;
            System.out.printf("Order Processing Rate: %.1f orders/minute%n", ordersPerMinute);
        }

        System.out.println("\n--- Final System Status ---");
        System.out.printf("Orders in queues: Intake: %d, Picking: %d, Packing: %d, Labelling: %d%n",
            intakeQueue.size(), pickingQueue.size(), packingQueue.size(), labellingQueue.size());
        System.out.printf("Containers awaiting dispatch: %d%n", sortingQueue.size());
        System.out.println("=".repeat(60));
    }
}