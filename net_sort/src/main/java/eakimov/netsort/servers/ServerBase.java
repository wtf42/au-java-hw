package eakimov.netsort.servers;

import eakimov.netsort.Metrics;
import eakimov.netsort.settings.ServerSettings;

public abstract class ServerBase implements Runnable {
    protected final ServerSettings settings;
    protected final Metrics metrics;

    protected ServerBase(ServerSettings settings, Metrics metrics) {
        this.settings = settings;
        this.metrics = metrics;
    }

    public abstract void stop() throws InterruptedException;
}
