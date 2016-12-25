package eakimov.netsort.servers;

import eakimov.netsort.Metrics;
import eakimov.netsort.protocol.ProtocolUtils;
import eakimov.netsort.protocol.SortArrayProtocol.ArrayMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPClientHandler implements Runnable {
    private final Metrics metrics;
    private final DatagramPacket recvPacket;
    private final TaskSolver solver;

    public UDPClientHandler(Metrics metrics, DatagramPacket recvPacket) {
        this.metrics = metrics;
        this.recvPacket = recvPacket;
        this.solver = new TaskSolver(metrics);
    }

    @Override
    public void run() {
        try {
            Metrics.MetricHandler metricHandler = metrics.createServerClientMetricHandler();
            metricHandler.start();

            ArrayMessage query = ProtocolUtils.fromBytesWithLength(recvPacket.getData());
            ArrayMessage answer = solver.processMessage(query);

            byte[] sendBuf = ProtocolUtils.toBytesWithLength(answer);
            DatagramPacket sendPacket =
                    new DatagramPacket(sendBuf, sendBuf.length, recvPacket.getSocketAddress());
            new DatagramSocket().send(sendPacket);

            metricHandler.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
