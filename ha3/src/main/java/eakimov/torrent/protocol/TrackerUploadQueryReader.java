package eakimov.torrent.protocol;

import java.io.DataInputStream;
import java.io.IOException;

public class TrackerUploadQueryReader {
    private final String name;
    private final long size;

    public TrackerUploadQueryReader(DataInputStream inputStream) throws IOException {
        name = inputStream.readUTF();
        size = inputStream.readLong();
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}
