package eakimov.torrent.protocol;

import java.io.DataInputStream;
import java.io.IOException;

public class TrackerUploadAnswerReader {
    private final int id;

    public TrackerUploadAnswerReader(DataInputStream inputStream) throws IOException {
        id = inputStream.readInt();
    }

    public int getId() {
        return id;
    }
}
