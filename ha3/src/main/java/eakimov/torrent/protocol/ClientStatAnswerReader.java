package eakimov.torrent.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientStatAnswerReader {
    private final List<Integer> parts;

    public ClientStatAnswerReader(DataInputStream inputStream) throws IOException {
        parts = new ArrayList<>();
        final int count = inputStream.readInt();
        for (int i = 0; i < count; i++) {
            parts.add(inputStream.readInt());
        }
    }

    public List<Integer> getParts() {
        return parts;
    }
}
