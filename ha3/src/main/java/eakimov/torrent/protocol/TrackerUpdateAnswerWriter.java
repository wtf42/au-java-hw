package eakimov.torrent.protocol;

import java.io.DataOutputStream;
import java.io.IOException;

public class TrackerUpdateAnswerWriter {
    private final boolean success;

    public TrackerUpdateAnswerWriter(boolean success) {
        this.success = success;
    }

    public void writeTo(DataOutputStream outputStream) throws IOException {
        outputStream.writeBoolean(success);
        outputStream.flush();
    }
}
