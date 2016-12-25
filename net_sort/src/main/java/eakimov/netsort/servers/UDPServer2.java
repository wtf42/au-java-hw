package eakimov.netsort.servers;

import eakimov.netsort.Metrics;
import eakimov.netsort.settings.ServerSettings;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPServer2 extends SyncServerBase {
    private final DatagramSocket serverSocket;
    private final ExecutorService executorService;

    public UDPServer2(ServerSettings settings, Metrics metrics) throws IOException {
        super(settings, metrics);
        serverSocket = new DatagramSocket(settings.getPort());
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void stop() throws InterruptedException {
        super.stop();
        serverSocket.close();
    }

    protected void actualRun() throws IOException {
        byte[] recvBuf = new byte[settings.getArguments().calculatePacketSize()];
        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
        serverSocket.receive(recvPacket);

        executorService.submit(new UDPClientHandler(metrics, recvPacket));
    }
}
