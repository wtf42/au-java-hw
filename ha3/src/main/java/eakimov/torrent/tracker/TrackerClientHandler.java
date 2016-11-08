package eakimov.torrent.tracker;

import eakimov.torrent.common.*;
import eakimov.torrent.protocol.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrackerClientHandler extends AbstractRequestsHandler {
    private final TrackerFilesInformation trackerFilesInformation;

    public TrackerClientHandler(Socket socket,
                                TrackerFilesInformation trackerFilesInformation) throws IOException {
        super(socket);
        this.trackerFilesInformation = trackerFilesInformation;
    }

    @Override
    protected void processCommand(int command) throws TorrentException, IOException {
        switch (command) {
            case Commands.TRACKER_LIST_CMD_ID:
                executeList();
                break;
            case Commands.TRACKER_UPLOAD_CMD_ID:
                executeUpload();
                break;
            case Commands.TRACKER_SOURCES_CMD_ID:
                executeSource();
                break;
            case Commands.TRACKER_UPDATE_CMD_ID:
                executeUpdate();
                break;
            default:
                throw new TrackerException("invalid command");
        }
    }

    private void executeList() throws IOException {
        new TrackerListAnswerWriter(trackerFilesInformation.getAllFiles()).writeTo(outputStream);
    }

    private void executeUpload() throws IOException {
        final TrackerUploadQueryReader uploadQuery = new TrackerUploadQueryReader(inputStream);
        final int id = trackerFilesInformation.nextUniqueId();
        trackerFilesInformation.addFile(new FileInformation(id,
                uploadQuery.getName(),
                uploadQuery.getSize()));
        new TrackerUploadAnswerWriter(id).writeTo(outputStream);
    }

    private void executeSource() throws IOException {
        final int id = new TrackerSourcesQueryReader(inputStream).getId();
        final List<ClientInformation> clients = trackerFilesInformation.getFileById(id)
                .map(trackerFilesInformation::getClientsAndActualize)
                .orElseGet(ArrayList::new);
        new TrackerSourcesAnswerWriter(clients).writeTo(outputStream);
    }

    private void executeUpdate() throws IOException {
        final TrackerUpdateQueryReader updateQuery = new TrackerUpdateQueryReader(inputStream);
        final boolean success = updateQuery.getFiles().stream()
                .map(trackerFilesInformation::getFileById)
                .allMatch(Optional::isPresent);
        if (success) {
            final List<FileInformation> updatedFiles = updateQuery.getFiles().stream()
                    .map(trackerFilesInformation::getFileById)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            trackerFilesInformation.clientUpdate(socket.getInetAddress(),
                    updateQuery.getPort(),
                    updatedFiles);
        }
        new TrackerUpdateAnswerWriter(success).writeTo(outputStream);
    }
}
