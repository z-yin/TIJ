package TIJ.concurrency;//: concurrency/FastSimulation.java

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static TIJ.net.util.Print.*;

public class FastSimulation {
    static final int N_ELEMENTS = 100000;
    static final int N_GENES = 30;
    static final int N_EVOLVERS = 50;
    static final AtomicInteger[][] GRID =
            new AtomicInteger[N_ELEMENTS][N_GENES];
    static final int[][] grid = new int[N_ELEMENTS][N_GENES];
    static Random rand = new Random(47);
    static final ReentrantLock[] locks = new ReentrantLock[N_ELEMENTS];
    static AtomicInteger counter1 = new AtomicInteger();
    static AtomicInteger counter2 = new AtomicInteger();

    static class Evolver1 implements Runnable {
        public void run() {
            while (!Thread.interrupted()) {
                // Randomly select an element to work on:
                int element = rand.nextInt(N_ELEMENTS);
                for (int i = 0; i < N_GENES; i++) {
                    int previous = element - 1;
                    if (previous < 0) previous = N_ELEMENTS - 1;
                    int next = element + 1;
                    if (next >= N_ELEMENTS) next = 0;
                    int oldvalue = GRID[element][i].get();
                    // Perform some kind of modeling calculation:
                    int newvalue = oldvalue +
                            GRID[previous][i].get() + GRID[next][i].get();
                    newvalue /= 3; // Average the three values
                    if (!GRID[element][i]
                            .compareAndSet(oldvalue, newvalue)) {
                        // Policy here to deal with failure. Here, we
                        // just report it and ignore it; our model
                        // will eventually deal with it.
                        print("Old value changed from " + oldvalue);
                    }
                }
                counter1.incrementAndGet();
            }
        }
    }

    static class Evolver2 implements Runnable {
        public void run() {
            while (!Thread.interrupted()) {
                // Randomly select an element to work on:
                int element = rand.nextInt(N_ELEMENTS);
                locks[element].lock();
                try {
                    for (int i = 0; i < N_GENES; i++) {
                        int previous = element - 1;
                        if (previous < 0) previous = N_ELEMENTS - 1;
                        int next = element + 1;
                        if (next >= N_ELEMENTS) next = 0;
                        grid[element][i] =
                                (grid[element][i] + grid[previous][i] + grid[next][i]) / 3;
                    }
                } finally {
                    locks[element].unlock();
                }
                counter2.incrementAndGet();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < N_ELEMENTS; i++)
            for (int j = 0; j < N_GENES; j++)
                GRID[i][j] = new AtomicInteger(rand.nextInt(1000));
        for (int i = 0; i < N_ELEMENTS; i++)
            for (int j = 0; j < N_GENES; j++)
                grid[i][j] = rand.nextInt(1000);
        for (int i = 0; i < N_ELEMENTS; i++)
            locks[i] = new ReentrantLock();
        for (int i = 0; i < N_EVOLVERS; i++) {
            exec.execute(new Evolver1());
            exec.execute(new Evolver2());
        }
        TimeUnit.SECONDS.sleep(5);
        exec.shutdownNow();
        print("Variant 1: " + counter1.get());
        print("Variant 2: " + counter2.get());
    }
} /* (Execute to see output) *///:~
