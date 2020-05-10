package TIJ.concurrency;

import java.util.concurrent.*;

class SynchronizedAddTwo {
    private int a = 0;
    private int b = 0;
    private volatile boolean canceled = false;

    public synchronized void addAll() {
        ++a; // Danger point here!
        Thread.yield();
        ++b;
    }

    public synchronized int getA() {
        return a;
    }

    public synchronized int getB() {
        return b;
    }

    public synchronized boolean isEqual() {
        return a == b;
    }

    // Allow this to be canceled:
    public void cancel() {
        canceled = true;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public static void main(String[] args) {
        EqualChecker.test(new SynchronizedAddTwo());
    }
}

public class EqualChecker implements Runnable {
    private SynchronizedAddTwo generator;
    private final int id;

    public EqualChecker(SynchronizedAddTwo g, int ident) {
        generator = g;
        id = ident;
    }

    public void run() {
        while (!generator.isCanceled()) {
            generator.addAll();
            if (!generator.isEqual()) {
                System.out.println("Not equal!");
                generator.cancel(); // Cancels all EvenCheckers
            }
        }
    }

    // Test any type of IntGenerator:
    public static void test(SynchronizedAddTwo gp, int count) {
        System.out.println("Press Control-C to exit");
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < count; i++)
            exec.execute(new EqualChecker(gp, i));
        exec.shutdown();
    }

    // Default value for count:
    public static void test(SynchronizedAddTwo gp) {
        test(gp, 10);
    }
} ///:~
