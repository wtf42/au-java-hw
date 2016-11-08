package eakimov.torrent.client;

import eakimov.torrent.common.ClientInformation;
import eakimov.torrent.common.Commands;
import eakimov.torrent.common.TorrentTracker;
import eakimov.torrent.common.FileInformation;
import eakimov.torrent.protocol.*;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class TrackerConnection implements Closeable {
    private final Socket trackerSocket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public TrackerConnection(String trackerHost, int trackerPort) throws IOException {
        trackerSocket = new Socket(trackerHost, trackerPort);
        inputStream = new DataInputStream(trackerSocket.getInputStream());
        outputStream = new DataOutputStream(trackerSocket.getOutputStream());
    }

    public TrackerConnection() throws IOException {
        this("localhost", TorrentTracker.TRACKER_PORT);
    }

    public void close() throws IOException {
        trackerSocket.close();
    }

    public synchronized List<FileInformation> listFiles() throws IOException {
        outputStream.writeByte(Commands.TRACKER_LIST_CMD_ID);
        outputStream.flush();
        return new TrackerListAnswerReader(inputStream).getFiles();
    }

    public synchronized int uploadFile(File file) throws IOException {
        new TrackerUploadQueryWriter(file.getName(), file.length()).writeTo(outputStream);
        return new TrackerUploadAnswerReader(inputStream).getId();
    }

    public synchronized List<ClientInformation> getSources(FileInformation fileInfo) throws IOException {
        new TrackerSourcesQueryWriter(fileInfo.getId()).writeTo(outputStream);
        return new TrackerSourcesAnswerReader(inputStream).getClients();
    }

    public synchronized boolean update(int port, List<FileInformation> files) throws IOException {
        new TrackerUpdateQueryWriter(port, files).writeTo(outputStream);
        return new TrackerUpdateAnswerReader(inputStream).isSuccess();
    }
}
