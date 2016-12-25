package eakimov.netsort.servers;

import eakimov.netsort.Metrics;
import eakimov.netsort.settings.ServerSettings;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer3 extends SyncServerBase {
    private final ServerSocketChannel socketChannel;
    private final Selector selector;
    private final ExecutorService executorService;
    private final Map<SocketChannel, ClientHandler> clientHandlers = new HashMap<>();

    public TCPServer3(ServerSettings settings, Metrics metrics) throws IOException {
        super(settings, metrics);

        selector = Selector.open();
        socketChannel = ServerSocketChannel.open();
        socketChannel.bind(new InetSocketAddress(settings.getPort()));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void stop() throws InterruptedException {
        super.stop();
        try {
            socketChannel.close();
            executorService.shutdown();
        } catch (IOException ignored) {
        }
    }

    protected void actualRun() throws IOException {
        selector.select();

        for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
            SelectionKey selectionKey = it.next();
            it.remove();

            if (!selectionKey.isValid()) {
                if (!stopped) {
                    System.err.println("invalid selection key!");
                }
            } else if (selectionKey.isAcceptable()) {
                SocketChannel channel = socketChannel.accept();
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_READ);
                clientHandlers.put(channel, new ClientHandler(channel, metrics));
            } else if (selectionKey.isReadable()) {
                SocketChannel channel = (SocketChannel) selectionKey.channel();
                ClientHandler clientHandler = clientHandlers.get(channel);
                clientHandler.metricStart();

                if (channel.read(clientHandler.getInputBuffer()) == -1) {
                    channel.close();
                    continue;
                }
                clientHandler.readFromBuffer();

                if (clientHandler.isReadReady()) {
                    selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_READ);
                    selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);

                    clientHandler.processClientMessage();
                }
            } else if (selectionKey.isWritable()) {
                SocketChannel channel = (SocketChannel) selectionKey.channel();
                ClientHandler clientHandler = clientHandlers.get(channel);
                channel.write(clientHandler.getOutputBuffer());

                if (clientHandler.isWriteReady()) {
                    clientHandler.getOutputBuffer().clear();
                    clientHandler.metricStop();

                    selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                    selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_READ);
                }
            }
        }
    }

    private static class ClientHandler extends TCPManyConnectionsClientContext {
        private final SocketChannel channel;
        public ClientHandler(SocketChannel channel, Metrics metrics) {
            super(metrics);
            this.channel = channel;
        }
    }
}
