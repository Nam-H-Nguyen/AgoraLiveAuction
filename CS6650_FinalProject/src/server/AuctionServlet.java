package server;

import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AuctionServlet {

    private Timer timer = new Timer();
    private static final String DEFAULT_FILENAME = "auction.srv";
    private static final long SAVE_DELAY = 1000 * 60 * 5;


    public class SaveTask extends TimerTask {
        private IAuctionServer auction;
        private String fileName;
        SaveTask(IAuctionServer auction, String fileName) {
            this.auction = auction;
            this.fileName = fileName;
        }
        @Override
        public void run() {
            saveState(auction, fileName);
        }
    }

    public static void main(String args[]) {
        AuctionServlet servlet = new AuctionServlet();
        List<Node> nodes = new ArrayList<>();
        //System.setProperty("java.security.policy", "file:///home/justas/Uni/DAS/Auction/out/production/RMIAuction/server/policyf.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader brNodesFile = null;
        try {
            System.out.println();
            brNodesFile = new BufferedReader(new FileReader(AuctionServlet.class.getResource("nodes.txt").getFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        String fileName = "";

        String host = null;
        int port = -1;

        try {
            System.out.println("Enter this server's ID (From 0 to 4. Reading details from nodes.txt): ");
            String serverID = br.readLine();
            String nodeLine = null;

            while ((nodeLine = brNodesFile.readLine()) != null) {
                String[] fields = nodeLine.split(" ");
                if (fields[0].equals(serverID)) {
                    host = fields[1];
                    port = Integer.parseInt(fields[2]);
                } else {
                    nodes.add(new Node(fields[1], Integer.parseInt(fields[2])));
                }
            }

            System.out.println("Listening to port " + port);
            System.out.println("Other servers are");
            for (Node node: nodes) {
                System.out.println("\t" + node);
            }

            IAuctionServer auction = null;
            System.out.println("Choose an option");
            System.out.println("n - New server from scratch");
            System.out.println("l - Load server state from file");
            System.out.println("q - Quit");
            boolean loop = true;
            while (loop) {
                switch (br.readLine()) {
                    case "n":
                        System.out.print("File to save to (default: " + DEFAULT_FILENAME + "): ");
                        fileName = br.readLine();
                        if (fileName.equals("")) {
                            fileName = DEFAULT_FILENAME;
                        }
                        auction = new AuctionServerImpl();
                        ((AuctionServerImpl)auction).addNodes(nodes);
                        loop = false;
                        break;
                    case "l":
                        System.out.print("File to load from (default: " + DEFAULT_FILENAME + "): ");
                        fileName = br.readLine();
                        if (fileName.equals("")) {
                            fileName = DEFAULT_FILENAME;
                        }
                        auction = loadState(fileName);
                        loop = false;
                        break;
                    case "q":
                        System.exit(0);
                        break;
                    default:
                        break;
                }
            }
            LocateRegistry.createRegistry(port);
            Registry reg = LocateRegistry.getRegistry(host, port);
            reg.rebind("auction", auction);
            servlet.getTimer().schedule(servlet.new SaveTask(auction, fileName), SAVE_DELAY);
            System.out.println("Server ready. Saving every "+ (float)SAVE_DELAY / 1000 / 60 +"mins to " + fileName);
            System.out.println("Press s to trigger save or q to quit");
            while (true) {
                String inp = br.readLine();
                if (inp.equals("s")) {
                    saveState(auction, fileName);
                } else if (inp.equals("q")) {
                    System.exit(0);
                }
            }
        }
        catch (Exception e) {
            System.out.println("Server Error: " + e);
        }
    }

    public static IAuctionServer loadState(String filename) {
        Object o = null;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
            o = in.readObject();
        } catch (IOException e) {
            System.err.println("Could not load file - " + e);
        } catch (ClassNotFoundException e) {
            System.err.println("Could not find class - " + e);
        }
        if (o instanceof AuctionServerImpl) {
            ((AuctionServerImpl)o).reloadTimer();
        }
        return (IAuctionServer)o;
    }

    public static void saveState(IAuctionServer auction, String fileName) {
        try {
            FileOutputStream out = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(auction);
            oos.close();
            System.out.println("Successfully saved server state to " + fileName);
        } catch (FileNotFoundException e) {
            System.err.println("Unable to find file " + e);
        } catch (IOException e) {
            System.err.println("Unable to write to file " + e);
        }
    }

    public Timer getTimer() {
        return timer;
    }
}