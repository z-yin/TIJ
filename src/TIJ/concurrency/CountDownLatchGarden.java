package TIJ.concurrency;//: concurrency/OrnamentalGarden.java

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Entrance2 implements Runnable {
    private static Count count = new Count();
    private static List<Entrance2> entrances =
            new ArrayList<Entrance2>();
    private int number = 0;
    // Doesn't need synchronization to read:
    private final int id;
    private static CountDownLatch stopLatch;
    private final CountDownLatch doneLatch;

    public Entrance2(int id, CountDownLatch stop, CountDownLatch done) {
        this.id = id;
        stopLatch = stop;
        this.doneLatch = done;
        // Keep this task in a list. Also prevents
        // garbage collection of dead tasks:
        entrances.add(this);
    }

    public void run() {
        while (stopLatch.getCount() != 0) {
            synchronized (this) {
                ++number;
            }
            System.out.println(this + " Total: " + count.increment());
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("sleep interrupted");
            }
        }
        System.out.println("Stopping " + this);
        doneLatch.countDown();
    }

    public synchronized int getValue() {
        return number;
    }

    public String toString() {
        return "Entrance " + id + ": " + getValue();
    }

    public static int getTotalCount() {
        return count.value();
    }

    public static int sumEntrances() {
        int sum = 0;
        for (Entrance2 entrance : entrances)
            sum += entrance.getValue();
        return sum;
    }
}

public class CountDownLatchGarden {
    public static void main(String[] args) throws Exception {
        CountDownLatch stopLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(5);
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++)
            exec.execute(new Entrance2(i, stopLatch, doneLatch));
        // Run for a while, then stop and collect the data:
        TimeUnit.SECONDS.sleep(3);
        stopLatch.countDown();
        exec.shutdown();
        doneLatch.await();
        System.out.println("Total: " + Entrance2.getTotalCount());
        System.out.println("Sum of Entrances: " + Entrance2.sumEntrances());
    }
} /* Output: (Sample)
Entrance 0: 1 Total: 1
Entrance 2: 1 Total: 3
Entrance 1: 1 Total: 2
Entrance 4: 1 Total: 5
Entrance 3: 1 Total: 4
Entrance 2: 2 Total: 6
Entrance 4: 2 Total: 7
Entrance 0: 2 Total: 8
...
Entrance 3: 29 Total: 143
Entrance 0: 29 Total: 144
Entrance 4: 29 Total: 145
Entrance 2: 30 Total: 147
Entrance 1: 30 Total: 146
Entrance 0: 30 Total: 149
Entrance 3: 30 Total: 148
Entrance 4: 30 Total: 150
Stopping Entrance 2: 30
Stopping Entrance 1: 30
Stopping Entrance 0: 30
Stopping Entrance 3: 30
Stopping Entrance 4: 30
Total: 150
Sum of Entrances: 150
*///:~
