package TIJ.concurrency;//: concurrency/FixedDiningPhilosophers.java
// Dining philosophers without deadlock.
// {Args: 5 5 timeout}

import java.util.concurrent.*;

public class BinPhilosophers {
    public static void main(String[] args) throws Exception {
        int ponder = 5;
        if (args.length > 0)
            ponder = Integer.parseInt(args[0]);
        int size = 5;
        if (args.length > 1)
            size = Integer.parseInt(args[1]);
        ExecutorService exec = Executors.newCachedThreadPool();
        BlockingQueue<Chopstick> chopsticks = new LinkedBlockingQueue<>();
        for (int i = 0; i < size; i++)
            chopsticks.put(new Chopstick());
        for (int i = 0; i < size; i++)
            exec.execute(new Philosopher2(chopsticks, i, ponder));
        if (args.length == 3 && args[2].equals("timeout"))
            TimeUnit.SECONDS.sleep(5);
        else {
            System.out.println("Press 'Enter' to quit");
            System.in.read();
        }
        exec.shutdownNow();
    }
} /* (Execute to see output) *///:~
