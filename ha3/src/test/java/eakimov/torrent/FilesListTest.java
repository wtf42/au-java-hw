package eakimov.torrent;

import eakimov.torrent.client.Client;
import eakimov.torrent.client.Storage;
import eakimov.torrent.common.FileInformation;
import eakimov.torrent.tracker.TrackerFilesInformation;
import eakimov.torrent.tracker.Tracker;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

public class FilesListTest {
    @Rule
    public TemporaryFolder workDir = new TemporaryFolder();

    @Test(timeout=3000)
    public void uploadTwoFiles() throws Exception {
        final String file1Contents = "file contents";
        final String file2Contents = "file2 contents";

        try (Tracker tracker = new Tracker(new TrackerFilesInformation())) {
            final Client client = new Client(new Storage());
            assertTrue(client.listFiles().isEmpty());

            final File uploadFile1 = workDir.newFile();
            FileUtils.writeStringToFile(uploadFile1, file1Contents, Charset.defaultCharset());
            client.uploadFile(uploadFile1);

            final List<FileInformation> files = client.listFiles();
            assertEquals(1, files.size());

            final FileInformation fileInformation = files.get(0);
            assertEquals(0, fileInformation.getId());
            assertEquals(uploadFile1.getName(), fileInformation.getName());
            assertEquals(file1Contents.length(), fileInformation.getSize());

            final File uploadFile2 = workDir.newFile();
            FileUtils.writeStringToFile(uploadFile2, file2Contents, Charset.defaultCharset());
            client.uploadFile(uploadFile2);

            final List<FileInformation> files2 = client.listFiles();
            files.sort(Comparator.comparing(FileInformation::getId));
            assertEquals(2, files2.size());

            final FileInformation file1Information = files2.get(0);
            assertEquals(0, file1Information.getId());
            assertEquals(uploadFile1.getName(), file1Information.getName());
            assertEquals(file1Contents.length(), file1Information.getSize());

            final FileInformation file2Information = files2.get(1);
            assertEquals(1, file2Information.getId());
            assertEquals(uploadFile2.getName(), file2Information.getName());
            assertEquals(file2Contents.length(), file2Information.getSize());
        }
    }

    @Test(timeout=3000)
    public void uploadEqualFileNames() throws Exception {
        final String fileName = "file.txt";
        final String file1Contents = "file contents";
        final String file2Contents = "file2 contents";

        try (Tracker tracker = new Tracker(new TrackerFilesInformation())) {
            final Client client1 = new Client(new Storage());
            assertTrue(client1.listFiles().isEmpty());
            final Client client2 = new Client(new Storage());
            assertTrue(client2.listFiles().isEmpty());

            final File file1 = Paths.get(workDir.newFolder().getAbsolutePath(), fileName).toFile();
            final File file2 = Paths.get(workDir.newFolder().getAbsolutePath(), fileName).toFile();
            FileUtils.writeStringToFile(file1, file1Contents, Charset.defaultCharset());
            FileUtils.writeStringToFile(file2, file2Contents, Charset.defaultCharset());
            client1.uploadFile(file1);
            client2.uploadFile(file2);

            final List<FileInformation> files = client1.listFiles();
            files.sort(Comparator.comparing(FileInformation::getId));
            assertEquals(2, files.size());

            final FileInformation file1Info = files.get(0);
            assertEquals(0, file1Info.getId());
            assertEquals(fileName, file1Info.getName());
            assertEquals(file1Contents.length(), file1Info.getSize());

            final FileInformation file2Info = files.get(1);
            assertEquals(1, file2Info.getId());
            assertEquals(fileName, file2Info.getName());
            assertEquals(file2Contents.length(), file2Info.getSize());
        }
    }
}
