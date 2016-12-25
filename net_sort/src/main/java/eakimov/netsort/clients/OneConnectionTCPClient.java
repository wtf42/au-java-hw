package eakimov.netsort.clients;

import eakimov.netsort.Metrics;
import eakimov.netsort.NetSortException;
import eakimov.netsort.protocol.SortArrayProtocol.ArrayMessage;
import eakimov.netsort.settings.ClientSettings;

import java.io.IOException;
import java.net.Socket;

public class OneConnectionTCPClient extends ClientBase {
    public OneConnectionTCPClient(ClientSettings settings, Metrics metrics) {
        super(settings, metrics);
    }

    protected void actualRun() throws IOException, NetSortException {
        try (Socket socket = new Socket(
                settings.getServerAddress().getAddress(),
                settings.getServerAddress().getPort())) {
            for (int i = 0; i < settings.getArguments().getX(); i++) {
                ArrayMessage inputMessage = taskGenerator.generateArrayMessage();
                inputMessage.writeDelimitedTo(socket.getOutputStream());
                ArrayMessage outputMessage = ArrayMessage.parseDelimitedFrom(socket.getInputStream());
                taskGenerator.validateSortedArray(outputMessage);
            }
        }
    }
}
