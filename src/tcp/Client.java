package tcp;

import java.io.IOException;

public interface Client {
    /**Connects to Server
     *
     * @param hostname
     * @param port
     * @return connection obj with input and output stream
     */
    Connection connect(String hostname, int port) throws IOException;
}
