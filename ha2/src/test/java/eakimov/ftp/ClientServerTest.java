package eakimov.ftp;

import eakimov.ftp.client.Client;
import eakimov.ftp.client.ClientException;
import eakimov.ftp.common.ListCommandAnswer.DirectoryContent;
import eakimov.ftp.server.Server;
import eakimov.ftp.server.ServerException;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class ClientServerTest {
    private static final String HOST = "localhost";
    private static final int PORT = 4242;
    @Rule
    public TemporaryFolder workDir = new TemporaryFolder();

    @Test
    public void getCommand() throws Exception {
        final String fileContents = "contents1";
        final String fileName = "file1.txt";

        final File file = workDir.getRoot().toPath().resolve(fileName).toFile();
        FileUtils.write(file, fileContents, Charset.defaultCharset());

        final Server server = new Server();
        server.start(PORT);

        final Client client = new Client(HOST, PORT);
        client.connect();

        final File outputFile = workDir.newFile();
        client.executeGet(file.getAbsolutePath(), outputFile);
        assertEquals(fileContents, FileUtils.readFileToString(outputFile, Charset.defaultCharset()));

        client.disconnect();
        server.stop();
    }

    @Test
    public void listCommand() throws Exception {
        final List<String> files = Arrays.asList("file1.txt", "file2.txt", "file3.txt");
        final List<String> dirs = Arrays.asList("dir1", "dir2");
        final List<DirectoryContent> contents =
                Stream.concat(
                        files.stream().map(f -> new DirectoryContent(f, false)),
                        dirs.stream().map(d -> new DirectoryContent(d, true))
                ).collect(Collectors.toList());

        for (String file : files) {
            workDir.newFile(file);
        }
        for (String dir : dirs) {
            workDir.newFolder(dir);
        }
        final String workDirPath = workDir.getRoot().getPath();

        final Server server = new Server();
        server.start(PORT);

        final Client client = new Client(HOST, PORT);
        client.connect();

        final List<DirectoryContent> clientContents = client.executeList(workDirPath);
        assertEquals(new HashSet<>(contents), new HashSet<>(clientContents));

        client.disconnect();

        server.stop();
    }

    @Test
    public void manyClients() throws Exception {
        final String fileContents = "contents1";
        final File inputFile = workDir.newFile("file.txt");
        final File outputFile1 = workDir.newFile();
        final File outputFile2 = workDir.newFile();

        final List<String> files = Arrays.asList("file1.txt", "file2.txt", "file3.txt");
        final List<DirectoryContent> contents = files.stream()
                .map(f -> new DirectoryContent(f, false))
                .collect(Collectors.toList());

        final File dirPath = workDir.newFolder();
        for (String file : files) {
            FileUtils.write(dirPath.toPath().resolve(file).toFile(), "", Charset.defaultCharset());
        }

        FileUtils.write(inputFile, fileContents, Charset.defaultCharset());

        final Server server = new Server();
        server.start(PORT);

        final Client firstClient = new Client(HOST, PORT);
        firstClient.connect();

        final Client secondClient = new Client(HOST, PORT);
        secondClient.connect();

        firstClient.executeGet(inputFile.getAbsolutePath(), outputFile1);
        assertEquals(fileContents, FileUtils.readFileToString(outputFile1, Charset.defaultCharset()));

        secondClient.executeGet(inputFile.getAbsolutePath(), outputFile2);
        assertEquals(fileContents, FileUtils.readFileToString(outputFile2, Charset.defaultCharset()));

        assertEquals(contents, firstClient.executeList(dirPath.getPath()));

        assertEquals(contents, secondClient.executeList(dirPath.getPath()));

        firstClient.disconnect();
        secondClient.disconnect();

        server.stop();
    }

    @Test(expected = ServerException.class)
    public void doubleStart() throws Exception {
        Server server = null;
        try {
            server = new Server();
            server.start(PORT);
            server.start(PORT);
        } finally {
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test(expected = ServerException.class)
    public void failedStop() throws Exception {
        final Server server = new Server();
        server.stop();
    }

    @Test(expected = ClientException.class)
    public void doubleConnect() throws Exception {
        Server server = null;
        Client client = null;
        try {
            server = new Server();
            server.start(PORT);
            client = new Client(HOST, PORT);
            client.connect();
            client.connect();
        } finally {
            if (client != null) {
                client.disconnect();
            }
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test(expected = ClientException.class)
    public void failedDisconnect() throws Exception {
        Server server = null;
        try {
            server = new Server();
            server.start(PORT);
            final Client client = new Client(HOST, PORT);
            client.disconnect();
        } finally {
            if (server != null) {
                server.stop();
            }
        }
    }
}
