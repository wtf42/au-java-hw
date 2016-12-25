package eakimov.netsort.servers;

import eakimov.netsort.Metrics;
import eakimov.netsort.settings.ServerSettings;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer1 extends SyncServerBase {
    private final ServerSocket serverSocket;

    public TCPServer1(ServerSettings settings, Metrics metrics) throws IOException {
        super(settings, metrics);
        serverSocket = new ServerSocket(settings.getPort());
    }

    public void stop() throws InterruptedException {
        super.stop();
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    protected void actualRun() throws IOException {
        final Socket socket = serverSocket.accept();
        new Thread(new TCPOneConnectionClientHandler(metrics, socket)).start();
    }
}
