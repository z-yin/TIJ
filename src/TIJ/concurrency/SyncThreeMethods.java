package TIJ.concurrency;

import java.util.concurrent.locks.ReentrantLock;

class ThreeMethods {
    private Object syncObject1 = new Object();
    private Object syncObject2 = new Object();
    private ReentrantLock lock1 = new ReentrantLock();
    private ReentrantLock lock2 = new ReentrantLock();
    private ReentrantLock lock3 = new ReentrantLock();

    public void f() {
        lock1.lock();
        try {
            for (int i = 0; i < 5; i++) {
                System.out.println("f()");
                Thread.yield();
            }
        } finally {
            lock1.unlock();
        }
    }

    public void g() {
        lock2.lock();
        try {
            for (int i = 0; i < 5; i++) {
                System.out.println("g()");
                Thread.yield();
            }
        } finally {
            lock2.unlock();
        }
    }

    public void h() {
        lock3.lock();
        try {
            for (int i = 0; i < 5; i++) {
                System.out.println("h()");
                Thread.yield();
            }
        } finally {
            lock3.unlock();
        }
    }
}

public class SyncThreeMethods {
    public static void main(String[] args) {
        final ThreeMethods tm = new ThreeMethods();
        new Thread() {
            @Override
            public void run() {
                tm.f();
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                tm.g();
            }
        }.start();
        tm.h();
    }
}
