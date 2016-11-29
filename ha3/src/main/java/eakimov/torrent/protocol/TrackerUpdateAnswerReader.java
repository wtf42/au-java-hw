package eakimov.torrent.protocol;

import java.io.DataInputStream;
import java.io.IOException;

public class TrackerUpdateAnswerReader {
    private final boolean success;

    public TrackerUpdateAnswerReader(DataInputStream inputStream) throws IOException {
        success = inputStream.readBoolean();
    }

    public boolean isSuccess() {
        return success;
    }
}
