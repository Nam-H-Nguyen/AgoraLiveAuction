package server;

import client.AuctionClient;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class Bid implements Serializable {
    private static final long serialVersionUID = 1L;

    private final AuctionClient owner;
    private final String ownerName;
    private final float amount;
    private final long timestamp;

    public Bid(AuctionClient owner, String ownerName, float amount) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    public String getOwnerName() { return ownerName; }
    public AuctionClient getOwner() {
        return owner;
    }

    public float getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        SimpleDateFormat dF = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append(amount).append("£").append(" @ ").append(dF.format(timestamp));
        return sb.toString();
    }
}
