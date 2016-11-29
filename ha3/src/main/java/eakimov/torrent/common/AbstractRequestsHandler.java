package eakimov.torrent.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public abstract class AbstractRequestsHandler implements Runnable {
    protected final Socket socket;
    protected final DataInputStream inputStream;
    protected final DataOutputStream outputStream;

    public AbstractRequestsHandler(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed() && socket.isConnected()) {
                final int command = inputStream.readUnsignedByte();
                processCommand(command);
            }
        } catch (EOFException ignored) {
            // success
        } catch (IOException | TorrentException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    protected abstract void processCommand(int command) throws TorrentException, IOException;
}
