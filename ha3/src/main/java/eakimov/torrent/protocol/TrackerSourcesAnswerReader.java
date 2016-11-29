package eakimov.torrent.protocol;

import eakimov.torrent.common.ClientInformation;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TrackerSourcesAnswerReader {
    private final List<ClientInformation> clients;

    public TrackerSourcesAnswerReader(DataInputStream inputStream) throws IOException {
        this.clients = new ArrayList<>();

        final int count = inputStream.readInt();
        for (int i = 0; i < count; i++) {
            final byte[] addressBuffer = new byte[4];
            inputStream.readFully(addressBuffer);
            final InetAddress address = Inet4Address.getByAddress(addressBuffer);
            final int port = inputStream.readUnsignedShort();
            clients.add(new ClientInformation(address, port));
        }
    }

    public List<ClientInformation> getClients() {
        return clients;
    }
}
