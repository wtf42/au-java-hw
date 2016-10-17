package eakimov.ftp.server;

import eakimov.ftp.common.Commands;
import eakimov.ftp.common.GetCommandAnswer;
import eakimov.ftp.common.ListCommandAnswer;
import eakimov.ftp.common.PathQuery;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed() && socket.isConnected()) {
                final int command = inputStream.readInt();
                switch (command) {
                    case Commands.LIST_CMD_ID:
                        executeList();
                        break;
                    case Commands.GET_CMD_ID:
                        executeGet();
                        break;
                    default:
                        throw new ServerException("invalid command");
                }
            }
        } catch (EOFException ignored) {
            // success
        } catch (IOException | ServerException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void executeGet() throws IOException {
        final PathQuery query = new PathQuery(inputStream);
        final File file = new File(query.getPath());
        new GetCommandAnswer(file).writeTo(outputStream);
    }

    private void executeList() throws IOException {
        final PathQuery query = new PathQuery(inputStream);

        final List<ListCommandAnswer.DirectoryContent> contents = new ArrayList<>();
        final File[] files = new File(query.getPath()).listFiles();
        if (files != null) {
            for (File file : files) {
                contents.add(new ListCommandAnswer.DirectoryContent(file.getName(), file.isDirectory()));
            }
        }

        new ListCommandAnswer(contents).writeTo(outputStream);
    }
}
