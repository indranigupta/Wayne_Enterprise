import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Random;
import java.util.concurrent.*;

class Order {
    private static final Random random = new Random();

    private int cargoWeight;
    private String destination;

    public Order() {
        this.cargoWeight = random.nextInt(41) + 10;
        this.destination = random.nextBoolean() ? "Gotham City" : "Atlanta";
    }

    public int getCargoWeight() {
        return cargoWeight;
    }

    public String getDestination() {
        return destination;
    }
}

class DeliveryShip {
    private int currentCargo;

    public DeliveryShip() {
        this.currentCargo = 0;
    }

    public void loadCargo(Order order) {
        currentCargo += order.getCargoWeight();
    }


    public void deliverCargo(int earnings) {
//        System.out.println("Delivering cargo. Current cargo weight: " + currentCargo);
//        currentCargo = 0;

    }

    public void sendToMaintenance() {
        System.out.println("Ship sent to maintenance.");
    }

    public int getCurrentCargo() {
        return currentCargo;
    }
}

public class WayneEnterprise {
    private static final int orderCost = 1000;
    private static final int penaltyCost = 250;
    private static final int targetEarnings = 10000;

    private static int totalEarnings = 0;
    private static int totalDelivered = 0;
    private static int totalCancelled= 0;

    private static BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
    private static BlockingQueue<DeliveryShip> availableShips = new LinkedBlockingQueue<>();

    private static final Object lock = new Object();

    public static void main(String[] args) {
        initializeShips();
        startShippingThreads();
        startCustomerThreads();
    }

    private static void initializeShips() {
        for(int i=0;i<5;i++) {
            availableShips.add(new DeliveryShip());
        }
    }

    private static <InterruptedException> void startShippingThreads() {
        for(int i=0;i<5;i++) {
            new Thread(() -> {
                while (totalEarnings < targetEarnings) {
                    try {
                        DeliveryShip ship = availableShips.take();
                        Order order = orderQueue.take();

                        ship.loadCargo(order);

                        Thread.sleep(100);

                        ship.deliverCargo(orderCost);
                        totalDelivered++;

                        synchronized (lock) {
                            totalEarnings += orderCost;
                            checkCompletion();
                        }

                        availableShips.add(ship);
                    }
                    catch (java.lang.InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    private static void startCustomerThreads() {
        for(int i=0;i<7;i++) {
            new Thread(() -> {
                while (totalEarnings < targetEarnings) {
                    try {

                        Order order = new Order();
                        orderQueue.put(order);


                        Thread.sleep(50);



                        synchronized (lock) {
                            if(orderQueue.size() > 1) {
                                totalCancelled++;
                                totalEarnings -= penaltyCost;
                            }

                            checkCompletion();
                        }

                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }
    private static void checkCompletion() {
        if(totalEarnings >= targetEarnings) {
            System.out.println("Simulation completed!");
            printResults();
            System.exit(0);
        }
    }

    private static void printResults() {
        System.out.println("Total order delivered: " + totalDelivered);
        System.out.println("Total cancled orders: " + totalCancelled);
        System.out.println("Total earnings: $" + totalEarnings);
    }
}


