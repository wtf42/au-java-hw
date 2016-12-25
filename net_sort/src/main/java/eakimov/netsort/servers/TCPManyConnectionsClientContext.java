package eakimov.netsort.servers;

import com.google.protobuf.InvalidProtocolBufferException;
import eakimov.netsort.Metrics;
import eakimov.netsort.protocol.ProtocolUtils;
import eakimov.netsort.protocol.SortArrayProtocol;
import eakimov.netsort.protocol.SortArrayProtocol.ArrayMessage;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

public class TCPManyConnectionsClientContext {
    private final Metrics metrics;
    private final TaskSolver solver;
    private final ByteBuffer inputBuffer;
    private final ByteArrayOutputStream inputMessageStream;

    private volatile Metrics.MetricHandler metricHandler;
    private volatile WritableByteChannel inputMessageChannel;
    private volatile ByteBuffer outputBuffer;

    public TCPManyConnectionsClientContext(Metrics metrics) {
        this.metrics = metrics;
        solver = new TaskSolver(metrics);
        inputBuffer = ByteBuffer.allocate(4096);

        inputMessageStream = new ByteArrayOutputStream();
        inputMessageChannel = Channels.newChannel(inputMessageStream);
    }

    public ByteBuffer getInputBuffer() {
        return inputBuffer;
    }

    public ByteBuffer getOutputBuffer() {
        return outputBuffer;
    }

    public boolean isReadReady() {
        try {
            return getMessage() != null;
        } catch (IOException ignored) {
            return false;
        }
    }

    public boolean isWriteReady() {
        return !outputBuffer.hasRemaining();
    }

    public void metricStart() {
        if (metricHandler == null) {
            metricHandler = metrics.createServerClientMetricHandler();
            metricHandler.start();
        }
    }

    public void metricStop() {
        if (metricHandler != null) {
            metricHandler.stop();
            metricHandler = null;
        }
    }

    public void readFromBuffer() throws IOException {
        inputBuffer.flip();
        inputMessageChannel.write(inputBuffer);
        inputBuffer.clear();
    }

    public void processClientMessage() throws IOException {
        inputMessageChannel.close();

        ArrayMessage query = getMessage();
        ArrayMessage answer = solver.processMessage(query);

        outputBuffer = ByteBuffer.wrap(ProtocolUtils.toBytesWithLength(answer));

        inputBuffer.clear();
        inputMessageStream.reset();
        inputMessageChannel = Channels.newChannel(inputMessageStream);
    }

    private ArrayMessage getMessage() throws IOException {
        return ArrayMessage.parseDelimitedFrom(inputMessageStream.toInputStream());
    }
}
