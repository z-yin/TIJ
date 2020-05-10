package TIJ.concurrency;//: concurrency/Restaurant.java
// The producer-consumer approach to task cooperation.

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Meal {
    private final int orderNum;

    public Meal(int orderNum) {
        this.orderNum = orderNum;
    }

    public String toString() {
        return "Meal " + orderNum;
    }
}

class WaitPerson implements Runnable {
    private Restaurant restaurant;
    protected ReentrantLock lock = new ReentrantLock();
    protected Condition condition = lock.newCondition();

    public WaitPerson(Restaurant r) {
        restaurant = r;
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
//                synchronized (this) {
//                    while (restaurant.meal == null)
//                        wait(); // ... for the chef to produce a meal
//                }
                lock.lock();
                try {
                    while (restaurant.meal == null)
                        condition.await();
                } finally {
                    lock.unlock();
                }

                System.out.println("Waitperson got " + restaurant.meal);
//                synchronized (restaurant.chef) {
//                    restaurant.meal = null;
//                    restaurant.chef.notifyAll(); // Ready for another
//                }

                restaurant.chef.lock.lock();
                try {
                    restaurant.meal = null;
                    restaurant.chef.condition.signalAll();
                } finally {
                    restaurant.chef.lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("WaitPerson interrupted");
        }
    }
}

class Chef implements Runnable {
    private Restaurant restaurant;
    private int count = 0;
    protected ReentrantLock lock = new ReentrantLock();
    protected Condition condition = lock.newCondition();

    public Chef(Restaurant r) {
        restaurant = r;
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
//                synchronized (this) {
//                    while (restaurant.meal != null)
//                        wait(); // ... for the meal to be taken
//                }
                lock.lock();
                try {
                    while (restaurant.meal != null)
                        condition.await(); // ... for the meal to be taken
                } finally {
                    lock.unlock();
                }

                if (++count == 10) {
                    System.out.println("Out of food, closing");
                    restaurant.exec.shutdownNow();
                }
                System.out.println("Order up! ");
//                synchronized (restaurant.waitPerson) {
//                    restaurant.meal = new Meal(count);
//                    restaurant.waitPerson.notifyAll();
//                }

                restaurant.waitPerson.lock.lock();
                try {
                    restaurant.meal = new Meal(count);
                    restaurant.waitPerson.condition.signalAll();
                } finally {
                    restaurant.waitPerson.lock.unlock();
                }

                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (InterruptedException e) {
            System.out.println("Chef interrupted");
        }
    }
}

public class Restaurant {
    Meal meal;
    ExecutorService exec = Executors.newCachedThreadPool();
    WaitPerson waitPerson = new WaitPerson(this);
    Chef chef = new Chef(this);

    public Restaurant() {
        exec.execute(chef);
        exec.execute(waitPerson);
    }

    public static void main(String[] args) {
        new Restaurant();
    }
} /* Output:
Order up! Waitperson got Meal 1
Order up! Waitperson got Meal 2
Order up! Waitperson got Meal 3
Order up! Waitperson got Meal 4
Order up! Waitperson got Meal 5
Order up! Waitperson got Meal 6
Order up! Waitperson got Meal 7
Order up! Waitperson got Meal 8
Order up! Waitperson got Meal 9
Out of food, closing
WaitPerson interrupted
Order up! Chef interrupted
*///:~
