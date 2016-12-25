package eakimov.netsort.settings;

import java.net.InetSocketAddress;

public class ServerSettings {
    private final RunArguments arguments;
    private final int port;

    public ServerSettings(RunArguments arguments, int port) {
        this.arguments = arguments;
        this.port = port;
    }

    public RunArguments getArguments() {
        return arguments;
    }

    public int getPort() {
        return port;
    }

    public InetSocketAddress getAddress() {
        return new InetSocketAddress("localhost", port);
    }
}
