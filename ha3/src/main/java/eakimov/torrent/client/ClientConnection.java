package eakimov.torrent.client;

import eakimov.torrent.common.ClientInformation;
import eakimov.torrent.common.TorrentTracker;
import eakimov.torrent.protocol.ClientGetQueryWriter;
import eakimov.torrent.protocol.ClientStatAnswerReader;
import eakimov.torrent.protocol.ClientStatQueryWriter;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.List;

public class ClientConnection implements Closeable {
    private final Socket trackerSocket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public ClientConnection(InetAddress address, int port) throws IOException {
        trackerSocket = new Socket(address, port);
        inputStream = new DataInputStream(trackerSocket.getInputStream());
        outputStream = new DataOutputStream(trackerSocket.getOutputStream());
    }

    public ClientConnection(ClientInformation info) throws IOException {
        this(info.getAddress(), info.getPort());
    }

    public void close() throws IOException {
        trackerSocket.close();
    }

    public synchronized List<Integer> stat(int fileId) throws IOException {
        new ClientStatQueryWriter(fileId).writeTo(outputStream);
        return new ClientStatAnswerReader(inputStream).getParts();
    }

    public synchronized void get(int fileId,
                                 int part,
                                 long partSize,
                                 FileChannel output) throws IOException {
        new ClientGetQueryWriter(fileId, part).writeTo(outputStream);
        output.transferFrom(Channels.newChannel(inputStream),
                part * TorrentTracker.FILE_PART_SIZE,
                partSize);
    }
}
