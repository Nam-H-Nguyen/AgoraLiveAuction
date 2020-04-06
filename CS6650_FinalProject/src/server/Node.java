package server;

import java.io.Serializable;

public class Node implements Serializable {
    String host = null;
    int port = -1;

    public Node(String host, int port) {
        this.host = host;
        this.port = port;
    }
}
