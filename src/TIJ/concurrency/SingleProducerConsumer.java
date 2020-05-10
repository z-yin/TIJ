package TIJ.concurrency;

import org.omg.PortableServer.THREAD_POLICY_ID;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Product {
    private final int id;

    public Product(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Product " + id;
    }
}

class Producer implements Runnable {
    private SingleProducerConsumer spc;
    private int count = 0;

    public Producer (SingleProducerConsumer spc) {
        this.spc = spc;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                synchronized (this) {
                    while (spc.products.size() >= 10) {
                        wait();
                    }
                }
                synchronized (spc.consumer) {
                    Product product = new Product(++count);
                    spc.products.offer(product);
                    System.out.println("Order up! " + product);
                    System.out.println("Total products: " + spc.products.size());
                    spc.consumer.notifyAll();
                }
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }
}

class Consumer implements Runnable {
    private SingleProducerConsumer spc;

    public Consumer (SingleProducerConsumer spc) {
        this.spc = spc;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                synchronized (this) {
                    while (spc.products.size() == 0) {
                        wait();
                    }
                }
                synchronized (spc.producer) {
                    Product product = spc.products.poll();
                    System.out.println("Waitperson got " + product);
                    System.out.println("Total products: " + spc.products.size());
                    spc.producer.notifyAll();
                }
                synchronized (spc.busBoy) {
                    spc.busBoy.notifyAll();
                }
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }
}

class BusBoy implements Runnable {
    private SingleProducerConsumer spc;

    public BusBoy(SingleProducerConsumer spc) {
        this.spc = spc;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                synchronized (this) {
                    wait();
                }
                System.out.println("Let's clean!");
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }
}

public class SingleProducerConsumer {
    Queue<Product> products = new LinkedList<>();

    ExecutorService exec = Executors.newCachedThreadPool();
    Producer producer = new Producer(this);
    Consumer consumer = new Consumer(this);
    BusBoy busBoy = new BusBoy(this);

    public SingleProducerConsumer() throws InterruptedException {
        exec.execute(producer);
        exec.execute(consumer);
        exec.execute(busBoy);
        TimeUnit.MILLISECONDS.sleep(2000);
        exec.shutdownNow();
    }

    public static void main(String[] args) throws InterruptedException {
        new SingleProducerConsumer();
    }
}
