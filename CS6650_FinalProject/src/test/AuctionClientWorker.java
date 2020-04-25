package test;

import client.ConnectLayer;
import client.AuctionClient;
import server.AuctionServer;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class AuctionClientWorker extends UnicastRemoteObject implements Runnable, AuctionClient {
    private static final long serialVersionUID = 1L;

    private static ArrayList<String> itemNames = new ArrayList<>(Arrays.asList("Shoe",
            "Card", "Book", "drug", "tag", "Dog",
            "man", "Diddle", "Cat", "Purse", "Bike", "Apple"));

    private ConnectLayer connection;
    private String name;
    private long end;
    private int items;
    private final long DEFAULT_INCREMENT = 50;

    public AuctionClientWorker(ConnectLayer connection, String name, long runtime, int items) throws RemoteException {
        super();
        this.connection = connection;
        this.name = name;
        this.end = System.currentTimeMillis() + runtime;
        this.items = items;
    }

    @Override
    public void run() {
        Random randNum = new Random();
        long counter = 0;
        while (System.currentTimeMillis() < end) {
            try {
                AuctionServer srv = connection.getServer();
                int rndInt = randNum.nextInt(itemNames.size());
                float rndFloat = randNum.nextFloat() * 100;
                ArrayList<Integer> openAuctionIds = srv.getOpenAuctionIds();
                if (openAuctionIds.size() < items) {
                    String itemName = itemNames.get(rndInt) + " - thread " + name;
                    System.out.println("Create " + itemName + " minBid " + rndFloat + " ends " + rndInt * 60 + 60);
                    srv.createAuctionItem(this, itemName, rndFloat, 60 + rndInt * 60);
                } else {
                    int rndId = randNum.nextInt(openAuctionIds.size());
                    srv.bid(this, rndId, counter + rndFloat * 2);
                    counter += DEFAULT_INCREMENT;
                }
                Thread.sleep(50);
            } catch (RemoteException e) {
                System.err.println("Unable to contact the server " + e);
                System.exit(1);
            } catch (InterruptedException e) {
                System.err.println("Unable to sleep thread - " + e);
            }
        }
    }

    @Override
    public String getName() throws RemoteException { return name; }

    @Override
    public void callback(String message) throws RemoteException {
        // Disable so console doesn't get flooded
        //System.out.println(name + " got message - " + message);
    }
}
