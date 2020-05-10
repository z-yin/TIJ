package TIJ.concurrency;//: concurrency/PipedIO.java
// Using pipes for inter-task I/O

import java.util.concurrent.*;
import java.io.*;
import java.util.*;

class Sender implements Runnable {
    private Random rand = new Random(47);
    //    private PipedWriter out = new PipedWriter();
    private BlockingQueue<Character> queue;

//    public PipedWriter getPipedWriter() {
//        return out;
//    }

    public Sender(BlockingQueue<Character> queue) {
        this.queue = queue;
    }

    public void run() {
        try {
            while (true)
                for (char c = 'A'; c <= 'z'; c++) {
//                    out.write(c);
                    queue.put((Character)c);
                    TimeUnit.MILLISECONDS.sleep(rand.nextInt(100));
                }
//        } catch (IOException e) {
//            System.out.println(e + " Sender write exception");
        } catch (InterruptedException e) {
            System.out.println(e + " Sender sleep interrupted");
        }
    }
}

class Receiver implements Runnable {
    //    private PipedReader in;
    private BlockingQueue<Character> queue;

//    public Receiver(Sender sender) throws IOException {
//        in = new PipedReader(sender.getPipedWriter());
//    }

    public Receiver(BlockingQueue<Character> queue) {
        this.queue = queue;
    }

    public void run() {
        try {
            while (true) {
                // Blocks until characters are there:
//                System.out.println("Read: " + (char) in.read() + ", ");
                System.out.println("Read: " + (char) queue.take() + ", ");
            }
//        } catch (IOException e) {
//            System.out.println(e + " Receiver read exception");
//        }
        } catch (InterruptedException e) {
            System.out.println("Take exception");
        }
    }
}

public class PipedIO {
    public static void main(String[] args) throws Exception {
//        Sender sender = new Sender();
//        Receiver receiver = new Receiver(sender);
        BlockingQueue<Character> queue = new LinkedBlockingQueue<>();
        ExecutorService exec = Executors.newCachedThreadPool();
        exec.execute(new Sender(queue));
        exec.execute(new Receiver(queue));
        TimeUnit.SECONDS.sleep(4);
        exec.shutdownNow();
    }
} /* Output: (65% match)
Read: A, Read: B, Read: C, Read: D, Read: E, Read: F, Read: G, Read: H, Read: I, Read: J, Read: K, Read: L, Read: M, java.lang.InterruptedException: sleep interrupted Sender sleep interrupted
java.io.InterruptedIOException Receiver read exception
*///:~
