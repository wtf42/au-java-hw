package eakimov.torrent.client;

import eakimov.torrent.common.ClientInformation;
import eakimov.torrent.common.TorrentTracker;
import eakimov.torrent.common.FileInformation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Collectors;

public class Client {
    private final Storage storage;
    private final PrintStream logger;

    private final String trackerHost;
    private final int trackerPort;
    private final Timer updateTimer;
    private TimerTask updateTimerTask;

    private ServerSocket p2pSocket;
    private int p2pPort;
    private boolean p2pStopped;

    public Client(Storage storage, String trackerHost, int trackerPort, PrintStream logger) {
        this.storage = storage;
        this.logger = logger;
        this.trackerHost = trackerHost;
        this.trackerPort = trackerPort;
        updateTimer = new Timer("keep-alive timer for tracker updates", true);
        updateTimerTask = null;
        p2pSocket = null;
        p2pPort = 0;
        p2pStopped = false;
    }

    public Client(Storage storage, String trackerHost, int trackerPort) {
        this(storage, trackerHost, trackerPort, System.err);
    }

    public Client(Storage storage) {
        this(storage, "localhost", TorrentTracker.TRACKER_PORT);
    }

    public Client(Storage storage, PrintStream logger) {
        this(storage, "localhost", TorrentTracker.TRACKER_PORT, logger);
    }

    public synchronized void startP2p(int p2pPort) throws IOException, ClientException {
        if (p2pSocket != null) {
            throw new ClientException("already started");
        }
        p2pSocket = new ServerSocket(p2pPort);
        // in case p2pPort == 0 we got random available port here
        this.p2pPort = p2pSocket.getLocalPort();
        new Thread(this::p2pListener).start();
    }

    public synchronized void stopP2p() throws ClientException, IOException, InterruptedException {
        if (p2pSocket == null) {
            throw new ClientException("not started");
        }
        p2pStopped = true;
        p2pSocket.close();
        p2pSocket = null;
        wait();
    }

    private void p2pListener() {
        while (!p2pStopped) {
            try {
                final Socket socket = p2pSocket.accept();
                new Thread(new P2pHandler(socket, storage)).start();
            } catch (SocketException e) {
                if (!p2pStopped) {
                    System.err.println(e.getMessage());
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        synchronized (this) {
            notify();
        }
    }

    public synchronized void scheduleUpdates() {
        if (updateTimerTask == null) {
            updateTimerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        update();
                    } catch (ClientException | IOException e) {
                        logger.println("failed to update: " + e.getMessage());
                    }
                }
            };
        }
        updateTimer.schedule(updateTimerTask, 0, TorrentTracker.CLIENT_UPDATE_INTERVAL);
    }

    public synchronized void stopUpdates() {
        if (updateTimerTask != null) {
            updateTimerTask.cancel();
        }
    }

    public List<FileInformation> listFiles() throws ClientException, IOException {
        try (TrackerConnection connection = connectToTracker()) {
            return connection.listFiles();
        }
    }

    public void uploadFile(File inputFile) throws ClientException, IOException {
        try (TrackerConnection connection = connectToTracker()) {
            final int fileId = connection.uploadFile(inputFile);
            storage.addExistingFile(inputFile, fileId);
        }
    }

    public void downloadFile(FileInformation info, File outputFile) throws ClientException, IOException {
        final FileStatus fileStatus = storage.addDownloadFile(outputFile, info);

        try (TrackerConnection trackerConnection = connectToTracker();
             FileChannel outputFileChannel = new FileOutputStream(outputFile).getChannel()) {
            outputFileChannel.truncate(info.getSize());

            while (!fileStatus.isReady()) {
                // to download file we
                // 1) download peers from tracker
                // 2) for each peer:
                // 2.1) download information about available file parts
                // 2.2) download interested file parts from peer
                // continue to (1) until all parts are downloaded

                final Set<Integer> remainingParts = fileStatus.getRemainingParts();
                final List<ClientInformation> sources = trackerConnection.getSources(info);
                for (ClientInformation source : sources) {
                    try (ClientConnection connection = new ClientConnection(source)) {
                        final List<Integer> clientParts = connection.stat(info.getId());
                        for (int part : clientParts) {
                            if (remainingParts.contains(part)) {
                                final long partSize = fileStatus.getPartSize(part);
                                connection.get(info.getId(), part, partSize, outputFileChannel);
                                fileStatus.setPartAvailable(part);
                                remainingParts.remove(part);
                            }
                        }
                    } catch (IOException e) {
                        logger.println("client communication error: " + e.getMessage());
                    }
                    if (fileStatus.isReady()) {
                        break;
                    }
                }
                if (!fileStatus.isReady()) {
                    try {
                        // file is not completely available at the moment
                        // wait here for new seeders before next query to tracker
                        Thread.sleep(TorrentTracker.NEW_CLIENTS_WAITING_TIME);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }

    public boolean update() throws ClientException, IOException {
        final List<FileInformation> availableFiles = storage.getFiles().stream()
                .filter(f -> !f.getAvailableParts().isEmpty())
                .map(FileStatus::getTrackerInfo)
                .collect(Collectors.toList());
        try (TrackerConnection connection = connectToTracker()) {
            return connection.update(p2pPort, availableFiles);
        }
    }

    private TrackerConnection connectToTracker() throws IOException {
        return new TrackerConnection(trackerHost, trackerPort);
    }
}
