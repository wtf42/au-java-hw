package eakimov.netsort.servers;

import eakimov.netsort.Metrics;
import eakimov.netsort.settings.ServerSettings;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer2 extends SyncServerBase {
    private final ServerSocket serverSocket;
    private final ExecutorService executorService;

    public TCPServer2(ServerSettings settings, Metrics metrics) throws IOException {
        super(settings, metrics);
        serverSocket = new ServerSocket(settings.getPort());
        executorService = Executors.newCachedThreadPool();
    }

    public void stop() throws InterruptedException {
        super.stop();
        try {
            serverSocket.close();
            executorService.shutdown();
        } catch (IOException ignored) {
        }
    }

    protected void actualRun() throws IOException {
        final Socket socket = serverSocket.accept();
        executorService.submit(new TCPOneConnectionClientHandler(metrics, socket));
    }
}
