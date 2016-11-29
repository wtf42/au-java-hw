package eakimov.torrent.protocol;

import eakimov.torrent.common.FileInformation;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrackerListAnswerReader {
    private final List<FileInformation> files;

    public TrackerListAnswerReader(DataInputStream inputStream) throws IOException {
        this.files = new ArrayList<>();

        final int count = inputStream.readInt();
        for (int i = 0; i < count; i++) {
            final int id = inputStream.readInt();
            final String name = inputStream.readUTF();
            final long size = inputStream.readLong();
            files.add(new FileInformation(id, name, size));
        }
    }

    public List<FileInformation> getFiles() {
        return files;
    }
}
