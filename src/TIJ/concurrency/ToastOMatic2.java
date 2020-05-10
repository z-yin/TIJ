package TIJ.concurrency;//: concurrency/ToastOMatic.java
// A toaster that uses queues.

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Apply butter to toast:
class Butterer2 implements Runnable {
    private ToastQueue dryQueue, finishedQueue;

    public Butterer2(ToastQueue dry, ToastQueue finished) {
        dryQueue = dry;
        finishedQueue = finished;
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                // Blocks until next piece of toast is available:
                Toast t = dryQueue.take();
                t.butter();
                System.out.println(t);
                finishedQueue.put(t);
            }
        } catch (InterruptedException e) {
            System.out.println("Butterer interrupted");
        }
        System.out.println("Butterer off");
    }
}

// Apply jam to buttered toast:
class Jammer2 implements Runnable {
    private ToastQueue dryQueue, finishedQueue;

    public Jammer2(ToastQueue dry, ToastQueue finished) {
        dryQueue = dry;
        finishedQueue = finished;
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                // Blocks until next piece of toast is available:
                Toast t = dryQueue.take();
                t.jam();
                System.out.println(t);
                finishedQueue.put(t);
            }
        } catch (InterruptedException e) {
            System.out.println("Jammer interrupted");
        }
        System.out.println("Jammer off");
    }
}

// Consume the toast:
class Eater2 implements Runnable {
    private ToastQueue finishedQueue;
    private int counter = 0;

    public Eater2(ToastQueue finished) {
        finishedQueue = finished;
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                // Blocks until next piece of toast is available:
                Toast t = finishedQueue.take();
                // Verify that the toast is coming in order,
                // and that all pieces are getting jammed:
                if ((t.getStatus() != Toast.Status.BUTTERED)
                        && (t.getStatus() != Toast.Status.JAMMED)) {
                    System.out.println(">>>> Error: " + t);
                    System.exit(1);
                } else
                    System.out.println("Chomp! " + t);
            }
        } catch (InterruptedException e) {
            System.out.println("Eater interrupted");
        }
        System.out.println("Eater off");
    }
}

public class ToastOMatic2 {
    public static void main(String[] args) throws Exception {
        ToastQueue dryQueue = new ToastQueue(),
                finishedQueue = new ToastQueue();
        ExecutorService exec = Executors.newCachedThreadPool();
        exec.execute(new Toaster(dryQueue));
        exec.execute(new Butterer2(dryQueue, finishedQueue));
        exec.execute(new Jammer2(dryQueue, finishedQueue));
        exec.execute(new Eater2(finishedQueue));
        TimeUnit.SECONDS.sleep(5);
        exec.shutdownNow();
    }
} /* (Execute to see output) *///:~
