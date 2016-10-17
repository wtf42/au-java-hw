package eakimov.ftp.common;

import org.apache.commons.io.IOUtils;

import java.io.*;

public class GetCommandAnswer {
    private final File file;

    public GetCommandAnswer(File srcFile) {
        this.file = srcFile;
    }

    public GetCommandAnswer(DataInputStream inputStream, File destFile) throws IOException {
        this.file = destFile;
        final long size = inputStream.readLong();
        final FileWriter fileWriter = new FileWriter(file);
        IOUtils.copyLarge(new InputStreamReader(inputStream), fileWriter, 0, size);
        fileWriter.close();
    }

    public void writeTo(DataOutputStream outputStream) throws IOException {
        if (file.exists() && !file.isDirectory()) {
            outputStream.writeLong(file.length());
            IOUtils.copyLarge(new FileInputStream(file), outputStream);
        } else {
            outputStream.writeLong(0);
        }
        outputStream.flush();
    }
}
