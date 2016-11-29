package eakimov.torrent.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrackerUpdateQueryReader {
    private final int port;
    private final List<Integer> files;

    public TrackerUpdateQueryReader(DataInputStream inputStream) throws IOException {
        port = inputStream.readUnsignedShort();
        files = new ArrayList<>();
        final int count = inputStream.readInt();
        for (int i = 0; i < count; i++) {
            files.add(inputStream.readInt());
        }
    }

    public int getPort() {
        return port;
    }

    public List<Integer> getFiles() {
        return files;
    }
}
