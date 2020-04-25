package server;

import client.AuctionClient;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AuctionItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int itemIDCounter = 0;
    private int itemID;

    private AuctionClient owner;
    private LinkedList<Bid> bids;

    private Set<AuctionClient> bidOservers;
    private String name;
    private final float minBid;
    private final Date startDate, closingDate;

    public AuctionItem(AuctionClient owner, String name, float minBid, long endTime) {
        this.owner = owner;
        this.startDate = new Date(System.currentTimeMillis());
        this.closingDate = new Date(System.currentTimeMillis() + 1000 * endTime);
        synchronized(this) {
            this.itemID = itemIDCounter;
            itemIDCounter += 1;
        }
        this.name = name;
        this.bids = new LinkedList<>();
        this.bidOservers = new HashSet<>();
        this.bidOservers.add(owner);
        this.minBid = minBid;
    }

    /**
     * Thread-safe bidding
     * @param b bid object
     * @return
     */
    public synchronized String makeBid(Bid b) {
        Bid currentBid = getCurrentBid();
        if (closingDate.getTime() - startDate.getTime() < 0) {
            return ErrorCodes.AUCTION_CLOSED.MESSAGE;
        } else if (b.getAmount() <= minBid) {
            return ErrorCodes.LOW_BID.MESSAGE;
        } else if (currentBid != null) {
            if (b.getAmount() <= currentBid.getAmount()) {
                return ErrorCodes.LOW_BID.MESSAGE;
            } else if (b.getOwner() == currentBid.getOwner()) {
                return ErrorCodes.ALREADY_MAX_BIDDER.MESSAGE;
            }
        }
        bids.push(b);
        bidOservers.add(b.getOwner());
        // Notify clients about the new bid
        for (AuctionClient client : bidOservers) {
            try {
                if (client == b.getOwner()) {
                    client.callback("You're the max bidder with " + b.getAmount());
                } else if (client != getOwner()){
                    client.callback("You've been outbid on " + this.getName());
                }
            } catch (RemoteException e) {
                System.err.println("Unable to access client - " + e);
            }
        }
        return ErrorCodes.SUCCESS_BID.MESSAGE;
    }

    /**
     * Notifies all bidders and the owner with a message
     * @param message
     */
    public void notifyObservers(String message) {
        for (AuctionClient client : bidOservers) {
            try {
                client.callback(message);
            } catch (RemoteException e) {
                System.err.println("Unable to access client - " + e);
            }
        }
    }



    public synchronized Bid getCurrentBid() {
        if (bids.size() > 0) {
            return bids.peek();
        }
        return null;
    }

    public int getItemID() {
        return itemID;
    }

    public AuctionClient getOwner() {
        return owner;
    }

    public LinkedList<Bid> getBids() {
        return bids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getMinBid() {
        return minBid;
    }

    public Set<AuctionClient> getBidOservers() { return bidOservers; }

    public Date getStartDate() { return startDate; }


    public Date getClosingDate() { return closingDate; }


    public long getClosingTime() {
        return this.closingDate.getTime();
    }


    public long getStartTime() {
        return this.startDate.getTime();
    }


    @Override
    public String toString() {
        synchronized(this) {
            Bid currentBid = getCurrentBid();
            SimpleDateFormat dF = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

            long timeDiff = closingDate.getTime() - System.currentTimeMillis();
            boolean hasEnded = timeDiff <= 0;
            String timeLeftStr = "";
            int minutes = 60 * 1000;
            if (!hasEnded) {
                if (timeDiff < minutes) {
                    timeLeftStr = String.valueOf(timeDiff / 1000) + "s";
                } else if (timeDiff >= minutes && timeDiff < 60 * minutes) {
                    timeLeftStr = String.valueOf(timeDiff / 1000 / 60) + "min " + (timeDiff / 1000) % 60 + "s";
                } else if (timeDiff >= 60 * minutes) {
                    timeLeftStr = String.valueOf(timeDiff / 1000 / 60 / 60) + "h " + (timeDiff / 1000 / 60) % 60 + "min";
                }
            }
            StringBuilder result = new StringBuilder("Auction Item # ");
            result.append(itemID).append(": ").append(name).append("\n");
            result.append("Minimum bid: ").append(minBid).append("\n");
            if (hasEnded && getCurrentBid() != null) {
                result.append("Winning bid: ").append(currentBid)
                        .append(" by ").append(currentBid.getOwnerName()).append("\n");
            } else {
                result.append("Current bid: ").append(currentBid == null ? "none" : currentBid).append("\n");
            }
            result.append("Start date: ").append(dF.format(startDate)).append("\n");
            result.append("End date: ").append(dF.format(closingDate)).append("\n");
            if (!hasEnded) {
                result.append("Time left: ").append(timeLeftStr);
            }
            return result.append("\n").toString();
        }
    }

}
