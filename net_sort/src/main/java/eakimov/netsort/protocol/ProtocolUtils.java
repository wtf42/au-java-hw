package eakimov.netsort.protocol;

import eakimov.netsort.protocol.SortArrayProtocol.ArrayMessage;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;

public class ProtocolUtils {
    public static byte[] toBytesWithLength(ArrayMessage message) throws IOException {
        ByteArrayOutputStream messageStream = new ByteArrayOutputStream();
        message.writeDelimitedTo(messageStream);
        return messageStream.toByteArray();
    }

    public static ArrayMessage fromBytesWithLength(byte[] buffer) throws IOException {
        ByteArrayOutputStream messageStream = new ByteArrayOutputStream();
        messageStream.write(buffer);
        return ArrayMessage.parseDelimitedFrom(messageStream.toInputStream());
    }
}
