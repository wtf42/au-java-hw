package eakimov.torrent.protocol;

import java.io.DataInputStream;
import java.io.IOException;

public class TrackerSourcesQueryReader {
    private final int id;

    public TrackerSourcesQueryReader(DataInputStream inputStream) throws IOException {
        id = inputStream.readInt();
    }

    public int getId() {
        return id;
    }
}
