package eakimov.torrent.protocol;

import java.io.DataInputStream;
import java.io.IOException;

public class ClientGetQueryReader {
    private final int id;
    private final int part;

    public ClientGetQueryReader(DataInputStream inputStream) throws IOException {
        id = inputStream.readInt();
        part = inputStream.readInt();
    }

    public int getId() {
        return id;
    }

    public int getPart() {
        return part;
    }
}
