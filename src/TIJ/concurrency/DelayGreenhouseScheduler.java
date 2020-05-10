package TIJ.concurrency;//: concurrency/GreenhouseScheduler.java
// Rewriting innerclasses/GreenhouseController.java
// to use a ScheduledThreadPoolExecutor.
// {Args: 5000}

import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

abstract class DelayedGreenhouseTask implements Runnable, Delayed {
    private static int counter = 0;
    private final int id = counter++;
    private final int delta;
    private final long trigger;

    DelayedGreenhouseTask() {
        this(0);
    }

    DelayedGreenhouseTask(int d) {
        delta = d;
        trigger = System.nanoTime() +
                NANOSECONDS.convert(delta, MILLISECONDS);
    }

    public int compareTo(Delayed arg) {
        DelayedGreenhouseTask that = (DelayedGreenhouseTask) arg;
        if (trigger < that.trigger) return -1;
        if (trigger > that.trigger) return 1;
        return 0;
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(
                trigger - System.nanoTime(), NANOSECONDS);
    }

    abstract public void run();

    abstract public DelayedGreenhouseTask create(int d);
}

class DelayGreenhouseController {
    private volatile boolean light = false;
    private volatile boolean water = false;
    private String thermostat = "Day";
    private ExecutorService exec;
    private DelayQueue<DelayedGreenhouseTask> queue;

    DelayGreenhouseController(ExecutorService e, DelayQueue<DelayedGreenhouseTask> q) {
        exec = e;
        queue = q;
    }

    public synchronized String getThermostat() {
        return thermostat;
    }

    public synchronized void setThermostat(String value) {
        thermostat = value;
    }

    class LightOn extends DelayedGreenhouseTask {

        LightOn() {
            super();
        }

        LightOn(int d) {
            super(d);
        }

        public LightOn create(int d) {
            return new LightOn(d);
        }

        public void run() {
            // Put hardware control code here to
            // physically turn on the light.
            System.out.println("Turning on lights");
            light = true;
        }
    }

    class LightOff extends DelayedGreenhouseTask {

        LightOff() {
            super();
        }

        LightOff(int d) {
            super(d);
        }

        public LightOff create(int d) {
            return new LightOff(d);
        }

        public void run() {
            // Put hardware control code here to
            // physically turn off the light.
            System.out.println("Turning off lights");
            light = false;
        }
    }

    class WaterOn extends DelayedGreenhouseTask {

        WaterOn() {
            super();
        }

        WaterOn(int d) {
            super(d);
        }

        public WaterOn create(int d) {
            return new WaterOn(d);
        }

        public void run() {
            // Put hardware control code here.
            System.out.println("Turning greenhouse water on");
            water = true;
        }
    }

    class WaterOff extends DelayedGreenhouseTask {

        WaterOff() {
            super();
        }

        WaterOff(int d) {
            super(d);
        }

        public WaterOff create(int d) {
            return new WaterOff(d);
        }

        public void run() {
            // Put hardware control code here.
            System.out.println("Turning greenhouse water off");
            water = false;
        }
    }

    class ThermostatNight extends DelayedGreenhouseTask {

        ThermostatNight() {
            super();
        }

        ThermostatNight(int d) {
            super(d);
        }

        public ThermostatNight create(int d) {
            return new ThermostatNight(d);
        }

        public void run() {
            // Put hardware control code here.
            System.out.println("Thermostat to night setting");
            setThermostat("Night");
        }
    }

    class ThermostatDay extends DelayedGreenhouseTask {

        ThermostatDay() {
            super();
        }

        ThermostatDay(int d) {
            super(d);
        }

        public ThermostatDay create(int d) {
            return new ThermostatDay(d);
        }

        public void run() {
            // Put hardware control code here.
            System.out.println("Thermostat to day setting");
            setThermostat("Day");
        }
    }

    class Bell extends DelayedGreenhouseTask {

        Bell() {
            super();
        }

        Bell(int d) {
            super(d);
        }

        public Bell create(int d) {
            return new Bell(d);
        }

        public void run() {
            System.out.println("Bing!");
        }
    }

    class Terminate extends DelayedGreenhouseTask {

        Terminate() {
            super();
        }

        Terminate(int d) {
            super(d);
        }

        // will never use
        public Terminate create(int d) {
            return new Terminate(d);
        }

        public void run() {
            System.out.println("Terminating");
            exec.shutdownNow();
            // Must start a separate task to do this job,
            // since the scheduler has been shut down:
            new Thread() {
                public void run() {
                    for (DataPoint d : data)
                        System.out.println(d);
                }
            }.start();
        }
    }

    // New feature: data collection
    static class DataPoint {
        final Calendar time;
        final float temperature;
        final float humidity;

        public DataPoint(Calendar d, float temp, float hum) {
            time = d;
            temperature = temp;
            humidity = hum;
        }

        public String toString() {
            return time.getTime() +
                    String.format(
                            " temperature: %1$.1f humidity: %2$.2f",
                            temperature, humidity);
        }
    }

    private Calendar lastTime = Calendar.getInstance();

    { // Adjust date to the half hour
        lastTime.set(Calendar.MINUTE, 30);
        lastTime.set(Calendar.SECOND, 00);
    }

    private float lastTemp = 65.0f;
    private int tempDirection = +1;
    private float lastHumidity = 50.0f;
    private int humidityDirection = +1;
    private Random rand = new Random(47);
    List<DataPoint> data = Collections.synchronizedList(new ArrayList<>());

    class CollectData extends DelayedGreenhouseTask {

        CollectData() {
            super();
        }

        CollectData(int d) {
            super(d);
        }

        public CollectData create(int d) {
            return new CollectData(d);
        }

        public void run() {
            System.out.println("Collecting data");
            synchronized (DelayGreenhouseController.this) {
                // Pretend the interval is longer than it is:
                lastTime.set(Calendar.MINUTE,
                        lastTime.get(Calendar.MINUTE) + 30);
                // One in 5 chances of reversing the direction:
                if (rand.nextInt(5) == 4)
                    tempDirection = -tempDirection;
                // Store previous value:
                lastTemp = lastTemp +
                        tempDirection * (1.0f + rand.nextFloat());
                if (rand.nextInt(5) == 4)
                    humidityDirection = -humidityDirection;
                lastHumidity = lastHumidity +
                        humidityDirection * rand.nextFloat();
                // Calendar must be cloned, otherwise all
                // DataPoints hold references to the same lastTime.
                // For a basic object like Calendar, clone() is OK.
                data.add(new DataPoint((Calendar) lastTime.clone(),
                        lastTemp, lastHumidity));
            }
        }
    }

    class DoTasks implements Runnable {
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    exec.execute(queue.take());
                }
            } catch (InterruptedException e) {
                System.out.println("Creating threads failed.");
            } catch (RejectedExecutionException e) {
                System.out.println("New Tasks are rejected");
            }
        }
    }

    public void schedule(DelayedGreenhouseTask task, int delay) {
        queue.put(task.create(delay));
    }

    public void repeat(DelayedGreenhouseTask task, int interval, long duration) {
        if (interval <= duration) {
            for (int i = 0; i < duration / interval; i++) {
                DelayedGreenhouseTask t = task.create(interval * i);
                queue.put(t);
            }
        }
    }
}

public class DelayGreenhouseScheduler {
    public static void main(String[] args) {
        DelayQueue<DelayedGreenhouseTask> queue = new DelayQueue<>();
        ExecutorService exec = Executors.newCachedThreadPool();
        DelayGreenhouseController gh = new DelayGreenhouseController(exec, queue);
        exec.execute(gh.new DoTasks());
        gh.schedule(gh.new Terminate(), 5000);
        // Former "Restart" class not necessary:
        gh.repeat(gh.new Bell(), 1000,  10000);
        gh.repeat(gh.new ThermostatNight(), 2000, 10000);
        gh.repeat(gh.new LightOn(), 200, 10000);
        gh.repeat(gh.new LightOff(), 400, 10000);
        gh.repeat(gh.new WaterOn(), 600, 10000);
        gh.repeat(gh.new WaterOff(), 800, 10000);
        gh.repeat(gh.new ThermostatDay(), 1400, 10000);
        gh.repeat(gh.new CollectData(), 500, 10000);
    }
} /* (Execute to see output) *///:~
