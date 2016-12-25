package eakimov.netsort.servers;

import eakimov.netsort.Metrics;
import eakimov.netsort.protocol.SortArrayProtocol.ArrayMessage;
import eakimov.netsort.settings.ServerSettings;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class TCPServer5 extends ServerBase {
    private final AsynchronousServerSocketChannel serverSocketChannel;
    private volatile boolean stopped;

    public TCPServer5(ServerSettings settings, Metrics metrics) throws IOException {
        super(settings, metrics);
        stopped = false;

        serverSocketChannel = AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(settings.getPort()));
    }

    @Override
    public void stop() throws InterruptedException {
        stopped = true;
        try {
            serverSocketChannel.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void run() {
        serverSocketChannel.accept(null, new ClientHandler());
    }

    private static class Context extends TCPManyConnectionsClientContext {
        private final AsynchronousSocketChannel socketChannel;

        private Context(AsynchronousSocketChannel socketChannel, Metrics metrics) {
            super(metrics);
            this.socketChannel = socketChannel;
        }
    }

    private class ClientHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
        @Override
        public void completed(AsynchronousSocketChannel result, Object attachment) {
            Context context = new Context(result, metrics);
            result.read(context.getInputBuffer(), context, new ReadHandler());
            serverSocketChannel.accept(null, this);
        }

        @Override
        public void failed(Throwable e, Object attachment) {
            if (!stopped) {
                e.printStackTrace();
            }
        }
    }

    private class ReadHandler implements CompletionHandler<Integer, Context> {
        @Override
        public void completed(Integer result, Context context) {
            try {
                if (result == -1) {
                    context.socketChannel.close();
                    return;
                }
                context.metricStart();
                context.readFromBuffer();
                if (!context.isReadReady()) {
                    context.socketChannel.read(context.getInputBuffer(), context, this);
                    return;
                }
                context.processClientMessage();
                context.socketChannel.write(context.getOutputBuffer(), context, new WriteHandler());
            } catch (IOException e) {
                if (!stopped) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void failed(Throwable e, Context attachment) {
            if (!stopped) {
                e.printStackTrace();
            }
        }
    }

    private class WriteHandler implements CompletionHandler<Integer, Context> {
        @Override
        public void completed(Integer result, Context context) {
            if (!context.isWriteReady()) {
                context.socketChannel.write(context.getOutputBuffer(), context, this);
                return;
            }
            context.metricStop();
            context.socketChannel.read(context.getInputBuffer(), context, new ReadHandler());
        }

        @Override
        public void failed(Throwable e, Context attachment) {
            if (!stopped) {
                e.printStackTrace();
            }
        }
    }
}
