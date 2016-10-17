package eakimov.ftp.client;

import eakimov.ftp.common.Commands;
import eakimov.ftp.common.GetCommandAnswer;
import eakimov.ftp.common.ListCommandAnswer;
import eakimov.ftp.common.ListCommandAnswer.DirectoryContent;
import eakimov.ftp.common.PathQuery;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class Client {
    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void connect() throws IOException, ClientException {
        if (socket != null) {
            throw new ClientException("already connected");
        }
        socket = new Socket(host, port);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public synchronized void disconnect() throws IOException, ClientException {
        if (socket == null) {
            throw new ClientException("not connected");
        }
        socket.close();
    }

    public synchronized List<DirectoryContent> executeList(String path) throws IOException {
        final PathQuery query = new PathQuery(path);
        outputStream.writeInt(Commands.LIST_CMD_ID);
        query.writeTo(outputStream);
        final ListCommandAnswer answer = new ListCommandAnswer(inputStream);
        return answer.getContents();
    }

    public synchronized void executeGet(String path, File outputFile) throws IOException {
        final PathQuery query = new PathQuery(path);
        outputStream.writeInt(Commands.GET_CMD_ID);
        query.writeTo(outputStream);
        new GetCommandAnswer(inputStream, outputFile);
    }
}
