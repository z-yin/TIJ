package TIJ.concurrency;//: concurrency/TestBlockingQueues.java
// {RunByHand}

import java.util.concurrent.*;
import java.io.*;

class LiftOffRunner implements Runnable {
    private BlockingQueue<LiftOff> rockets;

    public LiftOffRunner(BlockingQueue<LiftOff> queue) {
        rockets = queue;
    }

    public void add(LiftOff lo) {
        try {
            rockets.put(lo);
        } catch (InterruptedException e) {
            System.out.println("Interrupted during put()");
        }
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                LiftOff rocket = rockets.take();
                rocket.run(); // Use this thread. Not able to stop if interrupted.
//                new Thread(rocket).start();
                System.out.println(Thread.interrupted());
            }
        } catch (InterruptedException e) {
            System.out.println("Waking from take()");
        }
        System.out.println("Exiting LiftOffRunner");
    }
}

class AddLiftOff implements Runnable {
    private BlockingQueue<LiftOff> rockets;

    public AddLiftOff(BlockingQueue<LiftOff> queue) {
        rockets = queue;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                rockets.put(new LiftOff(2));
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted during put()");
        }
    }
}

public class TestBlockingQueues {
    static void getkey() {
        try {
            // Compensate for Windows/Linux difference in the
            // length of the result produced by the Enter key:
            new BufferedReader(
                    new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void getkey(String message) {
        System.out.println(message);
        getkey();
    }

    static void
    test(String msg, BlockingQueue<LiftOff> queue) {
        System.out.println(msg);
        ExecutorService exec = Executors.newCachedThreadPool();
//        for (int i = 0; i < 5; i++)
//            runner.add(new LiftOff(5));
        exec.execute(new Thread(new LiftOffRunner(queue)));
        exec.execute(new Thread(new AddLiftOff(queue)));
        getkey("Press 'Enter' (" + msg + ")");
        exec.shutdownNow();
        System.out.println("Finished " + msg + " test");
    }

    public static void main(String[] args) {
        test("LinkedBlockingQueue", // Unlimited size
                new LinkedBlockingQueue<LiftOff>());
        test("ArrayBlockingQueue", // Fixed size
                new ArrayBlockingQueue<LiftOff>(3));
        test("SynchronousQueue", // Size of 1
                new SynchronousQueue<LiftOff>());
    }
} ///:~
