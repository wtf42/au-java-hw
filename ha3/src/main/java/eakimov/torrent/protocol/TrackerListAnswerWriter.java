package eakimov.torrent.protocol;

import eakimov.torrent.common.FileInformation;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class TrackerListAnswerWriter {
    private final List<FileInformation> files;

    public TrackerListAnswerWriter(List<FileInformation> files) {
        this.files = files;
    }

    public void writeTo(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(files.size());
        for (FileInformation info : files) {
            outputStream.writeInt(info.getId());
            outputStream.writeUTF(info.getName());
            outputStream.writeLong(info.getSize());
        }
        outputStream.flush();
    }
}
