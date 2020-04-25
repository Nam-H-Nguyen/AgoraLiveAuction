package test;

import client.ConnectLayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

public class RunSimulation {
    // How often to check load
    private static long INTERVAL = 5;
    public static void main(String args[]) {
        String host = "localhost";
        int port = 3000;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else if (args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        ConnectLayer connection = new ConnectLayer("rmi://"+host+":"+port+"/auction", 1000);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        int noOfWorkers = 0;
        long duration = 0;
        float cumulative = 0;
        try {
            System.out.println("Number of workers would you like to test against?");
            noOfWorkers = Integer.valueOf(br.readLine());
            System.out.println("And for how long (in seconds, at least " + INTERVAL + ")?");
            duration = Long.valueOf(br.readLine());
            if (duration < INTERVAL) throw new IOException("Duration must be at least " + INTERVAL + "s");

            for (int i = 0; i < noOfWorkers; i++) {
                AuctionClientWorker cl = new AuctionClientWorker(connection, String.valueOf(i), duration * 1000, noOfWorkers);
                new Thread(cl).start();
            }

        } catch (RemoteException e) {
            System.err.println("Unable to determine server load - " + e);
        } catch (IOException e) {
            System.err.println("Unable to parse input " + e);
        }
        System.out.println("Finished! Average - " + cumulative / (duration / INTERVAL));
    }
}
