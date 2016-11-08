package eakimov.torrent;

import com.google.common.base.Splitter;

import eakimov.torrent.client.Client;
import eakimov.torrent.client.ClientException;
import eakimov.torrent.client.Storage;
import eakimov.torrent.common.TorrentTracker;
import eakimov.torrent.common.FileInformation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class CommandLineClient {
    private static final String STATE_FILENAME = "client.dat";
    private static final String EXIT_COMMAND = "exit";
    private static final String LIST_COMMAND = "list";
    private static final String DOWNLOAD_COMMAND = "download";
    private static final String UPLOAD_COMMAND = "upload";

    public static void main(String[] args) {
        final String host = args.length > 0 ? args[0] : "localhost";
        final File stateFile = new File(STATE_FILENAME);
        final Storage storage = Storage.loadFromFileOrNew(stateFile);
        final Client client = new Client(storage, host, TorrentTracker.TRACKER_PORT);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            client.startP2p(0);
            client.scheduleUpdates();

            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (!executeTorrentCommand(client, line)) {
                    break;
                }
            }
        } catch (ClientException | IOException e) {
            System.err.println("error occurred: " + e.getMessage());
        } finally {
            try {
                storage.saveToFile(stateFile);
                client.stopUpdates();
                client.stopP2p();
            } catch (IOException | InterruptedException | ClientException e) {
                System.err.println("failed to stop client: " + e.getMessage());
            }
        }
    }

    private static boolean executeTorrentCommand(Client client, String line)
            throws IOException, ClientException {
        final Iterator<String> it = Splitter.on(" ").limit(3).split(line).iterator();
        try {
            final String cmd = it.next();
            switch (cmd) {
                case EXIT_COMMAND:
                    return false;
                case LIST_COMMAND:
                    final List<FileInformation> files = client.listFiles();
                    files.forEach(System.out::println);
                    System.out.println("total: " + files.size());
                    break;
                case DOWNLOAD_COMMAND:
                    final int id = Integer.parseInt(it.next());
                    final File outputFile = new File(it.next());
                    new Thread(new Downloader(id, client, outputFile)).start();
                    break;
                case UPLOAD_COMMAND:
                    client.uploadFile(new File(it.next()));
                    client.update();
                    System.out.println("ok");
                    break;
                default:
                    System.err.println("unknown command");
                    break;
            }
        } catch (NoSuchElementException e) {
            System.err.println("invalid command arguments");
        }
        return true;
    }

    private static class Downloader implements Runnable {
        private final int id;
        private final Client client;
        private final File outputFile;

        private Downloader(int id, Client client, File outputFile) {
            this.id = id;
            this.client = client;
            this.outputFile = outputFile;
        }

        @Override
        public void run() {
            try {
                System.out.println("download started");
                final List<FileInformation> files = client.listFiles();
                final Optional<FileInformation> fileInfo = files.stream()
                        .filter(f -> f.getId() == id).findAny();
                if (!fileInfo.isPresent()) {
                    System.err.println("failed to find file information on server");
                    return;
                }
                client.downloadFile(fileInfo.get(), outputFile);
                System.out.println("download finished");
            } catch (ClientException | IOException e) {
                System.err.println("failed to download file: " + e.getMessage());
            }
        }
    }
}
