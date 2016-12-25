package eakimov.netsort.settings;

import java.net.InetSocketAddress;

public class ClientSettings {
    private final RunArguments arguments;
    private final InetSocketAddress serverAddress;

    public ClientSettings(RunArguments arguments, InetSocketAddress serverAddress) {
        this.arguments = arguments;
        this.serverAddress = serverAddress;
    }

    public RunArguments getArguments() {
        return arguments;
    }

    public InetSocketAddress getServerAddress() {
        return serverAddress;
    }
}
