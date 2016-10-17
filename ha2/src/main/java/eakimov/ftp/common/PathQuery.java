package eakimov.ftp.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PathQuery {
    private final String path;

    public PathQuery(String path) {
        this.path = path;
    }

    public PathQuery(DataInputStream inputStream) throws IOException {
        path = inputStream.readUTF();
    }

    public void writeTo(DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(path);
        outputStream.flush();
    }

    public String getPath() {
        return path;
    }
}
