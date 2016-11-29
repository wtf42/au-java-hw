package eakimov.torrent.protocol;

import eakimov.torrent.common.Commands;
import eakimov.torrent.common.FileInformation;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class TrackerUpdateQueryWriter {
    private final int port;
    private final List<FileInformation> files;

    public TrackerUpdateQueryWriter(int port, List<FileInformation> files) {
        this.port = port;
        this.files = files;
    }

    public void writeTo(DataOutputStream outputStream) throws IOException {
        outputStream.writeByte(Commands.TRACKER_UPDATE_CMD_ID);
        outputStream.writeShort(port);
        outputStream.writeInt(files.size());
        for (FileInformation file : files) {
            outputStream.writeInt(file.getId());
        }
        outputStream.flush();
    }
}
