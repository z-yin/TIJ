package TIJ.concurrency;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SleepingExercise implements Runnable {
    private static int count = 0;
    private int id = count++;

    @Override
    public void run() {
        Random random = new Random();
        int sleepTime = random.nextInt(10) + 1;
        try {
            TimeUnit.SECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("#" + id + " sleep " + sleepTime + " seconds");
        }
    }

    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; ++i)
            exec.execute(new SleepingExercise());
        exec.shutdown();
    }
}
