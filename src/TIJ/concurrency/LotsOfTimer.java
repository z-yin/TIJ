package TIJ.concurrency;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LotsOfTimer extends Thread {
    private int i = 50;

    @Override
    public void run() {
        while (i-- > 0) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println(getName() + " timer.");
                }
            }, 1000);
        }
    }

    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; ++i)
            exec.execute(new LotsOfTimer());
        exec.shutdown();
    }
}
