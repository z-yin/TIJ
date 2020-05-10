package TIJ.concurrency;//: concurrency/CallableDemo.java

import java.util.concurrent.*;
import java.util.*;

class TaskWithResult implements Callable<String> {
    private int id;

    public TaskWithResult(int id) {
        this.id = id;
    }

    public String call() {
        return "result of TaskWithResult " + id;
    }
}

class Fibonacci implements Callable<Integer> {
    private int n = 0;

    public Fibonacci(int n) { this.n = n; }

    private int fib(int n) {
        if (n < 2) return 1;
        return fib(n - 2) + fib(n - 1);
    }

    @Override
    public Integer call() {
        return fib(n);
    }
}

public class CallableDemo {
    public static void main(String[] args) {
//        ExecutorService exec = Executors.newCachedThreadPool();
//        ArrayList<Future<String>> results =
//                new ArrayList<Future<String>>();
//        for (int i = 0; i < 10; i++)
//            results.add(exec.submit(new TaskWithResult(i)));
//        for (Future<String> fs : results)
//            try {
//                // get() blocks until completion:
//                System.out.println(fs.get());
//            } catch (InterruptedException e) {
//                System.out.println(e);
//                return;
//            } catch (ExecutionException e) {
//                System.out.println(e);
//            } finally {
//                exec.shutdown();
//            }
        ExecutorService exec = Executors.newCachedThreadPool();
        ArrayList<Future<Integer>> results = new ArrayList<>();
        for (int i = 0; i < 10; ++i)
            results.add(exec.submit(new Fibonacci(i)));

        for (Future<Integer> fi : results) {
            try {
                System.out.println(fi.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (ExecutionException e) {
                e.printStackTrace();
            } finally {
                exec.shutdown();
            }
        }
    }
} /* Output:
result of TaskWithResult 0
result of TaskWithResult 1
result of TaskWithResult 2
result of TaskWithResult 3
result of TaskWithResult 4
result of TaskWithResult 5
result of TaskWithResult 6
result of TaskWithResult 7
result of TaskWithResult 8
result of TaskWithResult 9
*///:~
