package eakimov.torrent.protocol;

import eakimov.torrent.common.Commands;

import java.io.DataOutputStream;
import java.io.IOException;

public class TrackerUploadQueryWriter {
    private final String name;
    private final long size;

    public TrackerUploadQueryWriter(String name, long size) {
        this.name = name;
        this.size = size;
    }

    public void writeTo(DataOutputStream outputStream) throws IOException {
        outputStream.writeByte(Commands.TRACKER_UPLOAD_CMD_ID);
        outputStream.writeUTF(name);
        outputStream.writeLong(size);
        outputStream.flush();
    }
}
