package TIJ.concurrency;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class SetTrue implements Runnable {
    private volatile boolean flag = false;

    public synchronized void setFlag(boolean b) {
        flag = b;
    }

    public synchronized boolean getFlag() {
        return flag;
    }

    @Override
    public void run() {
        try {
            TimeUnit.MILLISECONDS.sleep(3000);
            setFlag(true);
            synchronized (this) {
                notifyAll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class CheckFlag implements Runnable {
    private final SetTrue st;

    public CheckFlag(SetTrue st) {
        this.st = st;
    }

    @Override
    public void run() {
        while (!st.getFlag()) {
            try {
                synchronized (st) {
                    st.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        st.setFlag(false);
        System.out.println("Set to false again!");
    }
}

public class BusyWait {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        SetTrue st = new SetTrue();
        exec.execute(st);
        exec.execute(new CheckFlag(st));
        exec.shutdown();
    }
}
