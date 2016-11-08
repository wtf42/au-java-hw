package eakimov.torrent;

import eakimov.torrent.client.*;
import eakimov.torrent.common.ClientInformation;
import eakimov.torrent.common.TorrentTracker;
import eakimov.torrent.common.FileInformation;
import eakimov.torrent.tracker.TrackerFilesInformation;
import eakimov.torrent.tracker.Tracker;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TrackerSourcesTest {
    @Rule
    public TemporaryFolder workDir = new TemporaryFolder();

    @Test(timeout = 5000)
    public void checkSources() throws Exception {
        final int clientsCount = 5;
        final int clientPortsStart = 8000;

        try (Tracker tracker = new Tracker(new TrackerFilesInformation())) {
            final File file = workDir.newFile();
            Utils.generateRandomFile(file, TorrentTracker.FILE_PART_SIZE);

            final Storage clientStorage = new Storage();
            final Client baseClient = new Client(clientStorage);
            baseClient.uploadFile(file);
            final FileInformation fileInformation = baseClient.listFiles().get(0);

            final List<Client> clients = new ArrayList<>();
            try {
                for (int i = 0; i < clientsCount; i++) {
                    final Client client = new Client(clientStorage);
                    clients.add(client);
                    client.startP2p(clientPortsStart + i);
                    assertTrue(client.update());
                }
                try (TrackerConnection connection = new TrackerConnection()){
                    final List<ClientInformation> sources = connection.getSources(fileInformation);
                    assertEquals(clientsCount, sources.size());

                    final Set<Integer> ports = sources.stream()
                            .map(ClientInformation::getPort).collect(Collectors.toSet());
                    for (int i = 0; i < clientsCount; i++) {
                        assertTrue(ports.contains(clientPortsStart + i));
                    }
                }
            } finally {
                for (Client client : clients) {
                    client.stopP2p();
                }
            }
        }
    }
}
