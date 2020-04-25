package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

public class ClientServlet {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 3000;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else if (args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        String connStr = "rmi://"+host+":"+port+"/auction";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {
            ConnectLayer conn = new ConnectLayer(connStr);
            
            System.out.println("Your name? ");
            AuctionClientImpl client = new AuctionClientImpl(br.readLine());
            System.out.println("Type one of the following letters to choose: ");
            System.out.println("To get all the auction items, type l");
            System.out.println("To list new auction items, type n");
            System.out.println("To make a bid, type b");
            System.out.println("To see history, type h");
            System.out.println("to quit, type q");

            boolean end = false;
            while (!end) {
                String responseMsg = "";
                try {
                    switch (br.readLine().toLowerCase()) {
                        case "l":
                            responseMsg = conn.getServer().getOpenAuctions();
                            break;
                        case "n":
                            try {
                                System.out.println("Item name: ");
                                String name = br.readLine();
                                if (name.equals("")) throw new NumberFormatException();
                                System.out.println("Price starts at: ");
                                float startPrice = Float.valueOf(br.readLine());
                                System.out.println("Duration of auction: ");
                                long endTime = Long.valueOf(br.readLine());
                                responseMsg = conn.getServer().createAuctionItem(client, name, startPrice, endTime);
                            } catch (NumberFormatException nfe) {
                                System.err.println("Unknown input format. Please retry.");
                            }
                            break;
                        case "b":
                            try {
                                System.out.println("Auction Item ID (e.g., 0): ");
                                int auctionItemId = Integer.valueOf(br.readLine());
                                System.out.println("Bid Amount: ");
                                float bidAmount = Float.valueOf(br.readLine());
                                responseMsg = conn.getServer().bid(client, auctionItemId, bidAmount);
                            } catch (NumberFormatException nfe) {
                                System.err.println("Unknown input format. Please try again.");
                            }
                            break;
                        case "h":
                            responseMsg = conn.getServer().getClosedAuctions();
                            break;
                        case "q":
                            end = true;
                            break;
                        default:
                            break;
                    }
                } catch (RemoteException e) {
                    System.out.println(e);
                }
                System.out.println(responseMsg);
            }
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Unknown input" + e);
            System.exit(2);
        }
    }
}
