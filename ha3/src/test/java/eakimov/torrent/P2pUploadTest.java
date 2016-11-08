package eakimov.torrent;

import eakimov.torrent.client.*;
import eakimov.torrent.common.TorrentTracker;
import eakimov.torrent.common.FileInformation;
import eakimov.torrent.tracker.TrackerFilesInformation;
import eakimov.torrent.tracker.Tracker;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class P2pUploadTest extends TorrentTrackerTestBase {
    @Test(timeout = 2000)
    public void uploadStartedAfterDownload() throws Exception {
        try (Tracker tracker = new Tracker(new TrackerFilesInformation())) {
            final Client uploadClient = new Client(new Storage());
            final Client downloadClient = new Client(new Storage());

            final File uploadFile = workDir.newFile();
            final File downloadFile = workDir.newFile();
            Utils.generateRandomFile(uploadFile, TorrentTracker.FILE_PART_SIZE * 5 / 2);

            uploadClient.uploadFile(uploadFile);
            try {
                uploadClient.startP2p(0);
                uploadClient.scheduleUpdates();
                downloadClient.startP2p(0);
                downloadClient.scheduleUpdates();

                Thread.sleep(TorrentTracker.CLIENT_UPDATE_INTERVAL);
                final FileInformation fileInformation = downloadClient.listFiles().get(0);
                try (TrackerConnection connection = new TrackerConnection()) {
                    assertEquals(1, connection.getSources(fileInformation).size());
                }

                downloadClient.downloadFile(fileInformation, downloadFile);
                assertTrue(FileUtils.contentEquals(uploadFile, downloadFile));

                Thread.sleep(TorrentTracker.CLIENT_UPDATE_INTERVAL);
                try (TrackerConnection connection = new TrackerConnection()) {
                    assertEquals(2, connection.getSources(fileInformation).size());
                }
            } finally {
                uploadClient.stopUpdates();
                uploadClient.stopP2p();
                downloadClient.stopUpdates();
                downloadClient.stopP2p();
            }
        }
    }
}
