package server;

public enum ErrorCodes {
    AUCTION_CLOSED(-1, "The auction is closed."),
    ALREADY_MAX_BIDDER(-2, "You are already the max bidder"),
    OWNER_EMPTY(-3, "Owner name cannot be empty"),
    NAME_NULL(-4, "Item name cannot be empty"),
    NAME_EMPTY(-5, "Item name cannot be an empty string"),
    NEGATIVE_MINVAL(-6, "Starting price can't be negative"),
    NEGATIVE_CLOSING_TIME(-7, "Closing time must be positive"),
    AUCTION_DOES_NOT_EXIST(-8, "Auction does not exist"),
    BID_ON_OWN_ITEM(-9, "You are the owner. Can't bid on your own item"),
    LOW_BID(0, "The bid is too low"),
    SUCCESS_BID(1, "The bid was successful"),
    ITEM_CREATED(2, "Item created successfully");

    public final int ID;
    public final String MESSAGE;
    ErrorCodes(int id, String msg) { this.ID = id; this.MESSAGE = msg; }

    @Override
    public String toString() {
        return "Error " + ID + ": " + MESSAGE;
    }
}
