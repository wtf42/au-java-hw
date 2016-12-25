package eakimov.netsort.servers;

import eakimov.netsort.Metrics;
import eakimov.netsort.protocol.SortArrayProtocol.ArrayMessage;
import eakimov.netsort.settings.ServerSettings;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer4 extends SyncServerBase {
    private final ServerSocket serverSocket;
    private final TaskSolver solver;

    public TCPServer4(ServerSettings settings, Metrics metrics) throws IOException {
        super(settings, metrics);
        serverSocket = new ServerSocket(settings.getPort());
        solver = new TaskSolver(metrics);
    }

    public void stop() throws InterruptedException {
        super.stop();
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    protected void actualRun() throws IOException {
        try (Socket socket = serverSocket.accept()) {
            Metrics.MetricHandler metricHandler = metrics.createServerClientMetricHandler();
            metricHandler.start();

            ArrayMessage query = ArrayMessage.parseDelimitedFrom(socket.getInputStream());

            ArrayMessage answer = solver.processMessage(query);
            answer.writeDelimitedTo(socket.getOutputStream());

            metricHandler.stop();
        } catch (EOFException e) {
            if (!stopped) {
                System.err.println("Unexpected EOF: " + e.getMessage());
            }
        }
    }
}
