package client;

import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

public class FailureDetector {
    private final static long TIMEOUT = 5000, INTERVAL = 5000;
    private final static int NO_OF_PROBES = 10000;
    
    private ConnectLayer conn;
    private Timer timer = new Timer();
    private long timeout, interval;

    private class ProbeTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (!conn.connectOK()) {
                    conn.reconnect();
                }
                conn.getServer().probe();
            } catch (RemoteException e) {
                System.err.println("Retrying in " + interval + "ms");
                conn.setConnected(false);
            }
        }
    }

    /**
     * Init failure detector with TIMEOUT and INTERVAL
     * @param conn
     */
    public FailureDetector(ConnectLayer conn) {
        this(conn, TIMEOUT, INTERVAL);
    }
    
    /**
     * Create a failure detector that will send probes every <interval> milliseconds
     * @param conn
     * @param interval how often to probe the server in milliseconds
     */
    public FailureDetector(ConnectLayer conn, long interval) {
        this(conn, TIMEOUT, interval);
    }
    
    /**
     * Create a failure detector with a specific timeout and interval
     * @param conn
     * @param timeout in milliseconds
     * @param interval how often to probe the server in milliseconds
     */
    public FailureDetector(ConnectLayer conn, long timeout, long interval) {
        this.conn = conn;
        this.timeout = timeout;
        this.interval = interval;
        this.timer.schedule(new ProbeTask(), 1, interval);
    }
    


    /**
     * Calculates average turnaround with the default number of probes
     * @return average turnaround, in milliseconds
     * @throws RemoteException
     */
    public float determineLoad() throws RemoteException {
        return determineLoad(NO_OF_PROBES);
    }
    
    /**
     * Calculates average turnaround
     * @param noOfProbes how many probes to use
     * @return average turnaround, in milliseconds
     * @throws RemoteException
     */
    public float determineLoad(int noOfProbes) throws RemoteException {
        conn.getServer().probe();
        long start = System.currentTimeMillis();
        for (int i = 0; i < noOfProbes; i++) {
            conn.getServer().probe();
        }
        float averageTurnaround = Float.valueOf((System.currentTimeMillis() - start)) / noOfProbes;
        return averageTurnaround;
    }
}
