package TIJ.concurrency;

import org.omg.CORBA.INITIALIZE;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class WaitClass implements Runnable {
    public synchronized void letsWait() throws InterruptedException{
        wait();
    }

    @Override
    public void run() {
        try {
            letsWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Finished waiting");
    }
}

class NotifyClass implements Runnable {
    private final WaitClass wc;

    public NotifyClass(WaitClass wc) {
        this.wc = wc;
    }

    public void letsNotify() {
        synchronized (wc) {
            wc.notifyAll();
        }
    }

    @Override
    public void run() {
        letsNotify();
    }
}

public class TwoClassNotify {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();
        WaitClass wc = new WaitClass();
        exec.execute(wc);
        TimeUnit.MILLISECONDS.sleep(2000);
        exec.execute(new NotifyClass(wc));
        exec.shutdown();
    }
}
