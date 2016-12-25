package eakimov.netsort.servers;

import eakimov.netsort.Metrics;
import eakimov.netsort.settings.ServerSettings;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.ClosedSelectorException;

public abstract class SyncServerBase extends ServerBase {
    protected volatile boolean stopped;

    protected SyncServerBase(ServerSettings settings, Metrics metrics) {
        super(settings, metrics);
        stopped = false;
    }

    public void stop() throws InterruptedException {
        stopped = true;
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                actualRun();
            } catch (SocketException | ClosedSelectorException e) {
                if (!stopped) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void actualRun() throws IOException;
}
