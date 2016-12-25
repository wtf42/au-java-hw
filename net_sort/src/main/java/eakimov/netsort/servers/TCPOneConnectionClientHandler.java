package eakimov.netsort.servers;

import eakimov.netsort.Metrics;
import eakimov.netsort.protocol.SortArrayProtocol.ArrayMessage;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class TCPOneConnectionClientHandler implements Runnable {
    private final Metrics metrics;
    private final Socket socket;
    private final TaskSolver solver;

    public TCPOneConnectionClientHandler(Metrics metrics, Socket socket) {
        this.metrics = metrics;
        this.socket = socket;
        solver = new TaskSolver(metrics);
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed() && socket.isConnected()) {
                Metrics.MetricHandler metricHandler = metrics.createServerClientMetricHandler();
                metricHandler.start();

                ArrayMessage query = ArrayMessage.parseDelimitedFrom(socket.getInputStream());
                if (query == null) { // EOF
                    return;
                }

                ArrayMessage answer = solver.processMessage(query);
                answer.writeDelimitedTo(socket.getOutputStream());

                metricHandler.stop();
            }
        } catch (EOFException ignored) {
            // success
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
