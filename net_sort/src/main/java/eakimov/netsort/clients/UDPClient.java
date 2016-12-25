package eakimov.netsort.clients;

import eakimov.netsort.Metrics;
import eakimov.netsort.NetSortException;
import eakimov.netsort.protocol.ProtocolUtils;
import eakimov.netsort.protocol.SortArrayProtocol.ArrayMessage;
import eakimov.netsort.settings.ClientSettings;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class UDPClient extends ClientBase {
    public UDPClient(ClientSettings settings, Metrics metrics) {
        super(settings, metrics);
    }

    protected void actualRun() throws IOException, NetSortException {
        for (int i = 0; i < settings.getArguments().getX(); i++) {
            try (DatagramSocket socket = new DatagramSocket()) {
                ArrayMessage inputMessage = taskGenerator.generateArrayMessage();
                byte[] sendBuf = ProtocolUtils.toBytesWithLength(inputMessage);

                DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, settings.getServerAddress());
                socket.send(sendPacket);

                byte[] recvBuf = new byte[settings.getArguments().calculatePacketSize()];
                DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(recvPacket);

                ArrayMessage outputMessage = ProtocolUtils.fromBytesWithLength(recvBuf);
                taskGenerator.validateSortedArray(outputMessage);
            } catch (SocketTimeoutException e) {
                throw new NetSortException("udp timeout!");
            }
        }
    }
}
