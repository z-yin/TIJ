package TIJ.concurrency;//: concurrency/Philosopher.java
// A dining philosopher

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Philosopher2 implements Runnable {
    private final BlockingQueue<Chopstick> chopsticks;
    private final int id;
    private final int ponderFactor;
    private Random rand = new Random(47);

    private void pause() throws InterruptedException {
        if (ponderFactor == 0) return;
        TimeUnit.MILLISECONDS.sleep(
                rand.nextInt(ponderFactor * 250));
    }

    public Philosopher2(BlockingQueue<Chopstick> chopsticks, int ident, int ponder) {
        this.chopsticks = chopsticks;
        id = ident;
        ponderFactor = ponder;
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                System.out.println(this + " " + "thinking");
                pause();
                // Philosopher becomes hungry
                System.out.println(this + " " + "grabbing first");
                Chopstick first = chopsticks.take();
                System.out.println(this + " " + "grabbing second");
                Chopstick second = chopsticks.take();
                System.out.println(this + " " + "eating");
                pause();
                chopsticks.put(first);
                chopsticks.put(second);
            }
        } catch (InterruptedException e) {
            System.out.println(this + " " + "exiting via interrupt");
        }
    }

    public String toString() {
        return "Philosopher " + id;
    }
} ///:~
