package client;

import server.AuctionServer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Ideally, the failure detector should be optional
 * It should be assigned using connection.setFailureDetector(new FailureDetector(param1, param2, ...))
 * Having failure detector params in the constructor is a bad idea
 */
public class ConnectLayer {
    private FailureDetector failureDetector;
    private String connStr;

    private boolean connected = false;

    private AuctionServer server;

    /**
     * Constructor that only requires a connection string
     * @param connStr
     */
    public ConnectLayer(String connStr) {
        this.connStr = connStr;
        connect();
        failureDetector = new FailureDetector(this);
    }

    /**
     * The period parameter is passed to the FailureDetector, determines how often to retry in case connection breaks
     * @param connStr
     * @param period
     */
    public ConnectLayer(String connStr, long period) {
        this.connStr = connStr;
        connect();
        failureDetector = new FailureDetector(this, period);
    }

    private void connect() {
        if (!connectOK()) {
            try {
                server = (AuctionServer) Naming.lookup(connStr);
                // Flag used by the servlet
                setConnected(true);
            } catch (MalformedURLException e) {
                System.err.println("Malformed URL: " + e);
            } catch (NotBoundException e) {
                System.err.println("Unable to bind the server - " + e);
            } catch (RemoteException e) {
                System.err.println("Unable to contact the server - " + e);
            }
        }
    }
    public synchronized boolean connectOK() {
        return connected;
    }

    public synchronized void setConnected(boolean connected) {
        this.connected = connected;
    }


    public void reconnect() {
        connect();
        if (connectOK()) {
            System.out.println("Reconnection succeeds.");
        }
    }



    public FailureDetector getFailureDetector() {
        return failureDetector;
    }

    public AuctionServer getServer() throws RemoteException {
        if (connectOK()) {
            return server;
        } else {
            throw new RemoteException("Server is dead.");
        }
    }

    public void setServer(AuctionServer server) {
        this.server = server;
    }
}
