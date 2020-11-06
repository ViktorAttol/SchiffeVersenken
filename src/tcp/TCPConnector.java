package tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPConnector implements Client, Connection, Server{
    private Socket socket;

    @Override
    public Connection connect(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
        return this;
    }

    @Override
    public Connection acceptConnection(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        this.socket = serverSocket.accept();
        return this;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return this.socket.getOutputStream();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.socket.getInputStream();
    }

    @Override
    public Socket getSocket() {
        return this.socket;
    }
}
