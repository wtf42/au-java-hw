package eakimov.torrent;

import eakimov.torrent.client.*;
import eakimov.torrent.common.ClientInformation;
import eakimov.torrent.common.TorrentTracker;
import eakimov.torrent.common.FileInformation;
import eakimov.torrent.tracker.TrackerFilesInformation;
import eakimov.torrent.tracker.Tracker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class P2pDownloadTest {
    @Rule
    public TemporaryFolder workDir = new TemporaryFolder();

    @Test(timeout = 2000)
    public void fileDownload() throws Exception {
        try (Tracker tracker = new Tracker(new TrackerFilesInformation())) {
            final Client client1 = new Client(new Storage());
            final Client client2 = new Client(new Storage());

            final File file1 = workDir.newFile();
            final File file2 = workDir.newFile();
            Utils.generateRandomFile(file1, TorrentTracker.FILE_PART_SIZE * 5 / 2);

            client1.uploadFile(file1);
            try {
                client1.startP2p(0);
                client1.update();

                final FileInformation fileInformation = client2.listFiles().get(0);
                client2.downloadFile(fileInformation, file2);

                assertTrue(FileUtils.contentEquals(file1, file2));
            } finally {
                client1.stopP2p();
            }
        }
    }

    @Test(timeout = 2000)
    public void checkSeeders() throws Exception {
        try (Tracker tracker = new Tracker(new TrackerFilesInformation())) {
            final Client client = new Client(new Storage());

            final File file = workDir.newFile();
            Utils.generateRandomFile(file, 42);

            client.uploadFile(file);
            try {
                client.startP2p(0);
                client.update();

                try (TrackerConnection connection = new TrackerConnection()) {
                    final FileInformation fileInformation = connection.listFiles().get(0);
                    assertEquals(file.getName(), fileInformation.getName());

                    final List<ClientInformation> sources = connection.getSources(fileInformation);
                    assertEquals(1, sources.size());

                    final ClientInformation seeder = sources.get(0);
                    try (ClientConnection conn = new ClientConnection(seeder)) {
                        final List<Integer> stat = conn.stat(fileInformation.getId());
                        assertEquals(Collections.singletonList(0), stat);
                    }
                }
            } finally {
                client.stopP2p();
            }
        }
    }

    @Test(timeout = 5000)
    public void checkManySeedersWithSinglePart() throws Exception {
        final int clientsCount = 5;
        final int downloadFileId = 0;

        final TrackerFilesInformation trackerFiles = new TrackerFilesInformation();
        try (Tracker tracker = new Tracker(trackerFiles)) {
            final File file = workDir.newFile();
            Utils.generateRandomFile(file, TorrentTracker.FILE_PART_SIZE * clientsCount);
            final FileInformation fileInformation =
                    new FileInformation(downloadFileId, file.getName(), file.length());
            trackerFiles.addFile(fileInformation);

            final List<Client> clients = new ArrayList<>();
            try {
                for (int i = 0; i < clientsCount; i++) {
                    final Storage clientStorage = new Storage();
                    final Client client = new Client(clientStorage);
                    final File clientFile = workDir.newFile();

                    final FileStatus fileStatus =
                            clientStorage.addDownloadFile(clientFile, fileInformation);
                    FileUtils.copyFile(file, clientFile);
                    fileStatus.setPartAvailable(i);

                    clients.add(client);
                    client.startP2p(0);
                    assertTrue(client.update());
                }

                final File downloadFile = workDir.newFile();
                final Client downloadClient = new Client(new Storage());
                downloadClient.downloadFile(fileInformation, downloadFile);

                assertTrue(FileUtils.contentEquals(file, downloadFile));
            } finally {
                for (Client client : clients) {
                    client.stopP2p();
                }
            }
        }
    }

    @Test(timeout = 2000)
    public void emptyFile() throws Exception {
        try (Tracker tracker = new Tracker(new TrackerFilesInformation())) {
            final Client client1 = new Client(new Storage());
            final Client client2 = new Client(new Storage());

            final File file1 = workDir.newFile();
            final File file2 = workDir.newFile();

            client1.uploadFile(file1);
            try {
                client1.startP2p(0);
                client1.update();

                final FileInformation fileInformation = client2.listFiles().get(0);
                client2.downloadFile(fileInformation, file2);

                assertTrue(FileUtils.contentEquals(file1, file2));
            } finally {
                client1.stopP2p();
            }
        }
    }

    @Test(timeout = 5000, expected = InvalidClientException.class)
    public void invalidClient() throws Exception {
        try (Tracker tracker = new Tracker(new TrackerFilesInformation())) {
            final PrintStream logger = spy(new PrintStream(new ByteArrayOutputStream()));
            doThrow(InvalidClientException.class).when(logger);

            final Client client1 = new Client(new Storage());
            final Client client2 = new Client(new Storage(), logger);

            final File file1 = workDir.newFile();
            final File file2 = workDir.newFile();
            Utils.generateRandomFile(file1, TorrentTracker.FILE_PART_SIZE * 5 / 2);

            client1.uploadFile(file1);
            try {
                client1.startP2p(0);
                client1.update();
            } finally {
                client1.stopP2p();
            }
            // client1 is not available now

            final FileInformation fileInformation = client2.listFiles().get(0);
            client2.downloadFile(fileInformation, file2);
        }
    }

    private static class InvalidClientException extends Exception {}
}
