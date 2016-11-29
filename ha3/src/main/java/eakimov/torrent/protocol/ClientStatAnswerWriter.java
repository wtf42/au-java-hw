package eakimov.torrent.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class ClientStatAnswerWriter {
    private final Collection<Integer> parts;

    public ClientStatAnswerWriter(Collection<Integer> parts) {
        this.parts = parts;
    }

    public void writeTo(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(parts.size());
        for (int part : parts) {
            outputStream.writeInt(part);
        }
        outputStream.flush();
    }
}
