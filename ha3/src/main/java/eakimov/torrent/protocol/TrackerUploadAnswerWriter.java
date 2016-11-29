package eakimov.torrent.protocol;

import java.io.DataOutputStream;
import java.io.IOException;

public class TrackerUploadAnswerWriter {
    private final int id;

    public TrackerUploadAnswerWriter(int id) {
        this.id = id;
    }

    public void writeTo(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(id);
        outputStream.flush();
    }
}
