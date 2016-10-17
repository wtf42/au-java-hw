package eakimov.ftp;

import eakimov.ftp.common.PathQuery;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import static org.junit.Assert.*;

public class PathQueryTest {
    @Test
    public void pathTransfer() throws Exception {
        final String path = "file.txt";

        final ByteArrayOutputStream srcStream = new ByteArrayOutputStream();

        final DataOutputStream dataOutputStream = new DataOutputStream(srcStream);
        new PathQuery(path).writeTo(dataOutputStream);

        final DataInputStream dataInputStream = new DataInputStream(srcStream.toInputStream());
        final PathQuery destQuery = new PathQuery(dataInputStream);

        assertEquals(path, destQuery.getPath());
    }
}
