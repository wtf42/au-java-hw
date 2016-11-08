package eakimov.torrent;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Utils {
    public static void generateRandomFile(File file, long length) throws IOException {
        final Random random = new Random();
        final byte[] data = new byte[(int)length];
        random.nextBytes(data);
        FileUtils.writeByteArrayToFile(file, data);
    }
}
