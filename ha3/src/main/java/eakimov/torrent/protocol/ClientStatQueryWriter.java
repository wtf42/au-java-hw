package eakimov.torrent.protocol;

import eakimov.torrent.common.Commands;

import java.io.DataOutputStream;
import java.io.IOException;

public class ClientStatQueryWriter {
    private final int id;

    public ClientStatQueryWriter(int id) {
        this.id = id;
    }

    public void writeTo(DataOutputStream outputStream) throws IOException {
        outputStream.writeByte(Commands.CLIENT_STAT_CMD_ID);
        outputStream.writeInt(id);
        outputStream.flush();
    }
}
