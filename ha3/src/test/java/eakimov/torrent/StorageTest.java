package eakimov.torrent;

import eakimov.torrent.client.Client;
import eakimov.torrent.client.FileStatus;
import eakimov.torrent.client.Storage;
import eakimov.torrent.common.FileInformation;
import eakimov.torrent.tracker.TrackerFilesInformation;
import eakimov.torrent.tracker.Tracker;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StorageTest {
    @Rule
    public TemporaryFolder workDir = new TemporaryFolder();

    @Test(timeout=3000)
    public void saveLoadTracker() throws Exception {
        final File file1 = workDir.newFile();
        final File file2 = workDir.newFile();
        final File savedTrackerFile = workDir.newFile();

        final TrackerFilesInformation tracker1Files = new TrackerFilesInformation();
        try (Tracker tracker1 = new Tracker(tracker1Files)) {
            final Client client = new Client(new Storage());

            client.uploadFile(file1);
            client.uploadFile(file2);

            final List<FileInformation> files1 = client.listFiles();
            files1.sort(Comparator.comparing(FileInformation::getId));
            assertEquals(2, files1.size());
            assertEquals(file1.getName(), files1.get(0).getName());
            assertEquals(file2.getName(), files1.get(1).getName());
        }

        tracker1Files.saveToFile(savedTrackerFile);

        final TrackerFilesInformation tracker2Files =
                TrackerFilesInformation.loadFromFile(savedTrackerFile);
        try (Tracker tracker2 = new Tracker(tracker2Files)) {
            final Client client = new Client(new Storage());
            final List<FileInformation> files2 = client.listFiles();
            files2.sort(Comparator.comparing(FileInformation::getId));
            assertEquals(2, files2.size());
            assertEquals(file1.getName(), files2.get(0).getName());
            assertEquals(file2.getName(), files2.get(1).getName());
        }
    }

    @Test(timeout=3000)
    public void saveLoadClient() throws Exception {
        try (Tracker tracker = new Tracker(new TrackerFilesInformation())) {
            final Storage clientStorage1 = new Storage();
            final Client client = new Client(clientStorage1);

            final File file1 = workDir.newFile();
            final File file2 = workDir.newFile();
            client.uploadFile(file1);
            client.uploadFile(file2);

            final List<FileInformation> files1 = client.listFiles();
            files1.sort(Comparator.comparing(FileInformation::getId));
            assertEquals(2, files1.size());
            assertEquals(file1.getName(), files1.get(0).getName());
            assertEquals(file2.getName(), files1.get(1).getName());

            final File storageFile = workDir.newFile();
            clientStorage1.saveToFile(storageFile);

            final Storage clientStorage2 = Storage.loadFromFile(storageFile);

            final List<FileStatus> files = clientStorage2.getFiles();
            files.sort(Comparator.comparing(f -> f.getTrackerInfo().getId()));
            assertEquals(file1.getName(), files.get(0).getTrackerInfo().getName());
            assertEquals(file2.getName(), files.get(1).getTrackerInfo().getName());
        }
    }
}
