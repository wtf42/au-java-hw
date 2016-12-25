package eakimov.netsort.servers;

import eakimov.netsort.Metrics;
import eakimov.netsort.settings.ServerSettings;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer1 extends SyncServerBase {
    private final DatagramSocket serverSocket;

    public UDPServer1(ServerSettings settings, Metrics metrics) throws IOException {
        super(settings, metrics);
        serverSocket = new DatagramSocket(settings.getPort());
    }

    public void stop() throws InterruptedException {
        super.stop();
        serverSocket.close();
    }

    protected void actualRun() throws IOException {
        byte[] recvBuf = new byte[settings.getArguments().calculatePacketSize()];
        DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
        serverSocket.receive(recvPacket);

        new Thread(new UDPClientHandler(metrics, recvPacket)).start();
    }
}
