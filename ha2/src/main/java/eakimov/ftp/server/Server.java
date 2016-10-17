package eakimov.ftp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    private ServerSocket serverSocket;
    private boolean stopped = true;

    public synchronized void start(int port) throws IOException, ServerException {
        if (serverSocket != null) {
            throw new ServerException("already started");
        }
        serverSocket = new ServerSocket(port);
        stopped = false;
        new Thread(this::listener).start();
    }

    public synchronized void stop() throws IOException, ServerException, InterruptedException {
        if (serverSocket == null) {
            throw new ServerException("not started");
        }
        stopped = true;
        serverSocket.close();
        serverSocket = null;
        wait();
    }

    private void listener() {
        while (!stopped) {
            try {
                final Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            } catch (SocketException e) {
                if (!stopped) {
                    System.err.println(e.getMessage());
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        synchronized (this) {
            notify();
        }
    }
}
