package eakimov.torrent;

import eakimov.torrent.client.Client;
import eakimov.torrent.client.Storage;
import eakimov.torrent.client.TrackerConnection;
import eakimov.torrent.common.FileInformation;
import eakimov.torrent.common.TorrentTracker;
import eakimov.torrent.tracker.Tracker;
import eakimov.torrent.tracker.TrackerFilesInformation;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ClientExpirationTest extends TorrentTrackerTestBase {
    @Test(timeout = 10000)
    public void expiredClient() throws Exception {
        try (Tracker tracker = new Tracker(new TrackerFilesInformation())) {
            final Client client = new Client(new Storage());

            final File file = workDir.newFile();
            Utils.generateRandomFile(file, TorrentTracker.FILE_PART_SIZE);

            client.uploadFile(file);
            final FileInformation fileInformation = client.listFiles().get(0);

            try {
                client.startP2p(0);
                client.update();
                try (TrackerConnection connection = new TrackerConnection()) {
                    assertFalse(connection.getSources(fileInformation).isEmpty());

                    Thread.sleep(TorrentTracker.FILE_UPDATE_EXPIRATION.toMillis() * 2);
                    assertTrue(connection.getSources(fileInformation).isEmpty());

                    client.update();
                    assertFalse(connection.getSources(fileInformation).isEmpty());
                }
            } finally {
                client.stopP2p();
            }
        }
    }
    @Test(timeout = 10000)
    public void autoUpdates() throws Exception {
        try (Tracker tracker = new Tracker(new TrackerFilesInformation())) {
            final Client client = new Client(new Storage());

            final File file = workDir.newFile();
            Utils.generateRandomFile(file, TorrentTracker.FILE_PART_SIZE);

            client.uploadFile(file);
            final FileInformation fileInformation = client.listFiles().get(0);

            try {
                client.startP2p(0);
                client.scheduleUpdates();
                try (TrackerConnection connection = new TrackerConnection()) {
                    for (int i = 0; i < 5; i++) {
                        Thread.sleep(TorrentTracker.FILE_UPDATE_EXPIRATION.toMillis() * 2 / 3);
                        assertFalse(connection.getSources(fileInformation).isEmpty());
                    }
                }
            } finally {
                client.stopUpdates();
                client.stopP2p();
            }
        }
    }
}
