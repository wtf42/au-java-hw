package eakimov.torrent.protocol;

import eakimov.torrent.common.Commands;

import java.io.DataOutputStream;
import java.io.IOException;

public class TrackerSourcesQueryWriter {
    private final int id;

    public TrackerSourcesQueryWriter(int id) {
        this.id = id;
    }

    public void writeTo(DataOutputStream outputStream) throws IOException {
        outputStream.writeByte(Commands.TRACKER_SOURCES_CMD_ID);
        outputStream.writeInt(id);
        outputStream.flush();
    }
}
