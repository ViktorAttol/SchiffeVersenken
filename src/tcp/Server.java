package tcp;

import java.io.IOException;

public interface Server {
    /**Waits for connection on given port and accepts the connection
     *
     * @param port
     * @return connection obj with input and output stream
     */
    Connection acceptConnection(int port) throws IOException;
}
