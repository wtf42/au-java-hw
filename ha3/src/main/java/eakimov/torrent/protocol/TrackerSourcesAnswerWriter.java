package eakimov.torrent.protocol;

import eakimov.torrent.common.ClientInformation;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class TrackerSourcesAnswerWriter {
    private final List<ClientInformation> clients;

    public TrackerSourcesAnswerWriter(List<ClientInformation> clients) {
        this.clients = clients;
    }

    public void writeTo(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(clients.size());
        for (ClientInformation client : clients) {
            outputStream.write(client.getAddress().getAddress());
            outputStream.writeShort(client.getPort());
        }
        outputStream.flush();
    }
}
