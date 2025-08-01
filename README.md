# SwiftCart Warehouse Simulation

This project is a Java-based simulation of an automated warehouse order fulfillment system called SwiftCart. It models the entire process from order intake to truck dispatch, using concurrent programming techniques to simulate a high-throughput environment.

The whole project was made for and assignmen created by **DR. KUAN YIK JUNN** my lecturer at APU for Concurent Programing module

## Simulation Overview

The simulation follows an order through a series of processing stages, each represented by a different class. These stages operate concurrently, passing orders along a pipeline using shared data structures. The primary goal is to process 600 orders, pack them into containers, load them onto trucks, and dispatch the trucks efficiently.

The simulation pipeline consists of the following stages:

1.  **Order Intake**: Generates a steady stream of 600 orders.
2.  **Picking Station**: Retrieves orders from the intake queue. Four concurrent pickers process orders.
3.  **Packing Station**: Packs the items for each order.
4.  **Labelling Station**: Applies a shipping label to each packed order.
5.  **Sorting Area**: Groups 30 packed orders (boxes) into a single container.
6.  **Loading**: Three concurrent loaders move containers from the sorting area to one of two available loading bays.
7.  **Truck Dispatch**: Trucks wait at the loading bays to be filled with 18 containers before being dispatched.

## Key Components

-   `SwiftCartSimulation`: The main class that initializes and runs the simulation. It sets up the shared queues, semaphores, and thread pools, and prints the final statistics.
-   `Order`: Represents a single customer order with a unique ID.
-   `Container`: Represents a shipping container that holds a batch of 30 orders.
-   `OrderIntake`: A `Runnable` that creates 600 orders and places them into the initial queue.
-   `PickingStation`: Simulates one of four stations where orders are picked. It can reject an order with a 5% probability.
-   `PackingStation`: A `Runnable` that simulates the packing of an order. It can reject an order with a 5% probability.
-   `LabellingStation`: A `Runnable` that simulates the labelling of a packed order. It can reject an order with a 5% probability.
-   `SortingArea`: A `Runnable` that collects 30 processed orders and groups them into a `Container`.
-   `Loader`: A `Runnable` representing one of three loaders that move containers to the loading bay. It simulates potential breakdowns.
-   `Truck`: A `Runnable` that simulates a truck arriving, waiting to be loaded with 18 containers, and then departing.

## Concurrency and Synchronization

The simulation makes extensive use of Java's `java.util.concurrent` package to manage the complex interactions between different stages:

-   **`ExecutorService`**: A fixed-size thread pool (`newFixedThreadPool(15)`) is used to manage all the worker threads for the different stations.
-   **`BlockingQueue`**: Each stage is connected to the next via a `BlockingQueue`. This ensures thread-safe communication and provides back-pressure, where a stage will wait if the next stage's queue is full.
-   **`Semaphore`**: Semaphores are used to control access to limited resources:
    -   `pickingSemaphore`: Limits the number of concurrent pickers to 4.
    -   `loadingBaySemaphore`: Limits access to the 2 available loading bays.
    -   `loaderSemaphore`: Limits the number of concurrent loaders to 3.
-   **`AtomicInteger`**: Used for thread-safe counters to track statistics like orders processed, rejected, and trucks dispatched.
-   **`CountDownLatch`**: A `CountDownLatch` is used to ensure the main thread waits until all trucks have completed their loading/dispatch cycle before printing the final report.
-   **Poison Pill**: The simulation uses the "poison pill" shutdown pattern. A special `POISON_PILL` object is placed in the queue to signal to consumer threads that no more items will be added, allowing them to terminate gracefully.

## How to Run

1.  **Compile the code**:
    ```bash
    mvn compile
    ```
2.  **Run the simulation**:
    ```bash
    mvn exec:java -Dexec.mainClass="com.swiftcart.SwiftCartSimulation"
    ```

## Simulation Output

The simulation prints real-time status messages to the console, indicating which thread is processing which order or container.

At the end of the simulation, a final report is displayed with the following statistics:
-   **Simulation Duration**: Total time taken for the simulation to run.
-   **Total Orders Processed**: The sum of successfully packed boxes and rejected orders.
-   **Orders Rejected**: The number and percentage of orders rejected at various stages.
-   **Boxes Packed**: The total number of orders successfully packed.
-   **Containers Created**: The number of containers filled and sent to the loading area.
-   **Trucks Dispatched**: The number of trucks that were filled and departed.
-   **Truck Wait Times**: The maximum, minimum, and average time trucks spent waiting for their first container.
-   **Order Processing Rate**: The number of orders processed per minute.
-   **Final System Status**: The number of items remaining in each queue at the end of the simulation.
