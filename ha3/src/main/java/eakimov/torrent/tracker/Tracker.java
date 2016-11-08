package eakimov.torrent.tracker;

import eakimov.torrent.common.TorrentTracker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Tracker implements AutoCloseable {
    private final TrackerFilesInformation trackerFilesInformation;
    private final ServerSocket serverSocket;
    private volatile boolean stopped = true;

    public Tracker(TrackerFilesInformation trackerFilesInformation, int port) throws IOException {
        this.trackerFilesInformation = trackerFilesInformation;
        serverSocket = new ServerSocket(port);
        stopped = false;
        new Thread(this::listener).start();
    }

    public Tracker(TrackerFilesInformation trackerFilesInformation) throws IOException {
        this(trackerFilesInformation, TorrentTracker.TRACKER_PORT);
    }

    @Override
    public synchronized void close() throws IOException, InterruptedException {
        stopped = true;
        serverSocket.close();
        wait();
    }

    private void listener() {
        while (!stopped) {
            try {
                final Socket socket = serverSocket.accept();
                new Thread(new TrackerClientHandler(socket, trackerFilesInformation)).start();
            } catch (SocketException e) {
                if (!stopped) {
                    System.err.println(e.getMessage());
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        synchronized (this) {
            notifyAll();
        }
    }
}
