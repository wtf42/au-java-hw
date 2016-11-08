package eakimov.torrent.protocol;

import java.io.DataInputStream;
import java.io.IOException;

public class ClientStatQueryReader {
    private final int id;

    public ClientStatQueryReader(DataInputStream inputStream) throws IOException {
        id = inputStream.readInt();
    }

    public int getId() {
        return id;
    }
}
