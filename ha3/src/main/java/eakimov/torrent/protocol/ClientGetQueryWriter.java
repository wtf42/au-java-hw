package eakimov.torrent.protocol;

import eakimov.torrent.common.Commands;

import java.io.DataOutputStream;
import java.io.IOException;

public class ClientGetQueryWriter {
    private final int id;
    private final int part;

    public ClientGetQueryWriter(int id, int part) {
        this.id = id;
        this.part = part;
    }

    public void writeTo(DataOutputStream outputStream) throws IOException {
        outputStream.writeByte(Commands.CLIENT_GET_CMD_ID);
        outputStream.writeInt(id);
        outputStream.writeInt(part);
        outputStream.flush();
    }
}
