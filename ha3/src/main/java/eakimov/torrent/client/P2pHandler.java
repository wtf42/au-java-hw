package eakimov.torrent.client;

import eakimov.torrent.common.AbstractRequestsHandler;
import eakimov.torrent.common.Commands;
import eakimov.torrent.common.TorrentTracker;
import eakimov.torrent.common.TorrentException;
import eakimov.torrent.protocol.ClientGetQueryReader;
import eakimov.torrent.protocol.ClientStatAnswerWriter;
import eakimov.torrent.protocol.ClientStatQueryReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.Channels;
import java.util.Optional;

public class P2pHandler extends AbstractRequestsHandler {
    private final Storage storage;

    public P2pHandler(Socket socket, Storage storage) throws IOException {
        super(socket);
        this.storage = storage;
    }

    @Override
    protected void processCommand(int command) throws TorrentException, IOException {
        switch (command) {
            case Commands.CLIENT_GET_CMD_ID:
                executeGet();
                break;
            case Commands.CLIENT_STAT_CMD_ID:
                executeStat();
                break;
            default:
                throw new ClientException("invalid command");
        }
    }

    private void executeGet() throws ClientException, IOException {
        final ClientGetQueryReader getQuery = new ClientGetQueryReader(inputStream);
        final Optional<FileStatus> fileStatus = storage.getFileStatus(getQuery.getId());
        if (!fileStatus.isPresent()) {
            throw new InvalidQueryException("file not found");
        }
        if (!fileStatus.get().getAvailableParts().contains(getQuery.getPart())) {
            throw new InvalidQueryException("part is not available");
        }
        final File localPath = fileStatus.get().getLocalPath();
        try (FileInputStream fileInputStream = new FileInputStream(localPath)) {
            fileInputStream.getChannel().transferTo(
                    getQuery.getPart() * TorrentTracker.FILE_PART_SIZE,
                    TorrentTracker.FILE_PART_SIZE,
                    Channels.newChannel(outputStream));
            outputStream.flush();
        }
    }

    private void executeStat() throws ClientException, IOException {
        final ClientStatQueryReader statQuery = new ClientStatQueryReader(inputStream);
        final Optional<FileStatus> fileStatus = storage.getFileStatus(statQuery.getId());
        if (!fileStatus.isPresent()) {
            throw new InvalidQueryException("file not found");
        }
        new ClientStatAnswerWriter(fileStatus.get().getAvailableParts()).writeTo(outputStream);
    }
}
