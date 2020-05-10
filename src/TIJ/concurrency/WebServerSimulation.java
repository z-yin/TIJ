package TIJ.concurrency;//: concurrency/BankTellerSimulation.java
// Using queues and multithreading.
// {Args: 5}

import java.util.*;
import java.util.concurrent.*;

// Read-only objects don't require synchronization:
class Client {
    private final int serviceTime;

    public Client(int tm) {
        serviceTime = tm;
    }

    public int getServiceTime() {
        return serviceTime;
    }

    public String toString() {
        return "[" + serviceTime + "]";
    }
}

// Teach the customer line to display itself:
class ClientLine extends LinkedBlockingQueue<Client> {
    public ClientLine() {
        super();
    }

    public String toString() {
        if (this.size() == 0)
            return "[Empty]";
        StringBuilder result = new StringBuilder();
        for (Client client : this)
            result.append(client);
        return result.toString();
    }
}

// Randomly add customers to a queue:
class ClientGenerator implements Runnable {
    private ClientLine clients;
    private static Random rand = new Random(47);

    public ClientGenerator(ClientLine cq) {
        clients = cq;
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                TimeUnit.MILLISECONDS.sleep(rand.nextInt(20));
                clients.put(new Client(rand.nextInt(1000)));
            }
        } catch (InterruptedException e) {
            System.out.println("CustomerGenerator interrupted");
        }
        System.out.println("CustomerGenerator terminating");
    }
}

class Server implements Runnable {
    private static int counter = 0;
    private final int id = counter++;
    // Customers served during this shift:
    private int clientsServed = 0;
    private ClientLine clients;

    public Server(ClientLine cq) {
        clients = cq;
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                Client client = clients.take();
                TimeUnit.MILLISECONDS.sleep(client.getServiceTime());
                synchronized (this) {
                    clientsServed++;
                }
            }
        } catch (InterruptedException e) {
            System.out.println(this + "interrupted");
        }
        System.out.println(this + "terminating");
    }

    public String toString() {
        return "Server " + id + " ";
    }

    public String shortString() {
        return "S" + id;
    }
}

class ServerManager implements Runnable {
    private ExecutorService exec;
    private ClientLine clients;
    private static Random rand = new Random(47);
    private List<Server> servers = new LinkedList<>();
    private int adjustmentPeriod;

    public ServerManager(ExecutorService e, ClientLine clients, int ad, int numOFServers) {
        exec = e;
        this.clients = clients;
        this.adjustmentPeriod = ad;
        // Start with a single teller:
        for (int i = 0; i < numOFServers; ++i) {
            Server server = new Server(clients);
            exec.execute(server);
            servers.add(server);
        }
    }

    public void run() {
        try {
            while (!Thread.interrupted()) {
                TimeUnit.MILLISECONDS.sleep(adjustmentPeriod);
                System.out.print(clients.size() + " { ");
                for (Server server : servers)
                    System.out.print(server.shortString() + " ");
                System.out.println("}");
            }
        } catch (InterruptedException e) {
            System.out.println(this + "interrupted");
        }
        System.out.println(this + "terminating");
    }

    public String toString() {
        return "ServerManager ";
    }
}

public class WebServerSimulation {
    static final int NUM_OF_CLIENTS = 5;
    static final int ADJUSTMENT_PERIOD = 1000;

    public static void main(String[] args) throws Exception {
        ExecutorService exec = Executors.newCachedThreadPool();
        // If line is too long, customers will leave:
        ClientLine clients = new ClientLine();
        exec.execute(new ClientGenerator(clients));
        // Manager will add and remove tellers as necessary:
        exec.execute(new ServerManager(exec, clients, ADJUSTMENT_PERIOD, NUM_OF_CLIENTS));
        if (args.length > 0) // Optional argument
            TimeUnit.SECONDS.sleep(new Integer(args[0]));
        else {
            System.out.println("Press 'Enter' to quit");
            System.in.read();
        }
        exec.shutdownNow();
    }
} /* Output: (Sample)
[429][200][207] { T0 T1 }
[861][258][140][322] { T0 T1 }
[575][342][804][826][896][984] { T0 T1 T2 }
[984][810][141][12][689][992][976][368][395][354] { T0 T1 T2 T3 }
Teller 2 interrupted
Teller 2 terminating
Teller 1 interrupted
Teller 1 terminating
TellerManager interrupted
TellerManager terminating
Teller 3 interrupted
Teller 3 terminating
Teller 0 interrupted
Teller 0 terminating
CustomerGenerator interrupted
CustomerGenerator terminating
*///:~
