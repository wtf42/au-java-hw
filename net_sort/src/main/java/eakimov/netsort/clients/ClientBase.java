package eakimov.netsort.clients;

import eakimov.netsort.Metrics;
import eakimov.netsort.NetSortException;
import eakimov.netsort.protocol.SortArrayProtocol.ArrayMessage;
import eakimov.netsort.settings.ClientSettings;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public abstract class ClientBase implements Runnable {
    protected final ClientSettings settings;
    protected final Metrics metrics;
    protected final TaskGenerator taskGenerator;

    protected ClientBase(ClientSettings settings, Metrics metrics) {
        this.settings = settings;
        this.metrics = metrics;
        taskGenerator = new TaskGenerator(settings.getArguments());
    }

    @Override
    public void run() {
        final Metrics.MetricHandler metricHandler = metrics.createClientMetricHandler();
        metricHandler.start();

        try {
            actualRun();
        } catch (IOException | NetSortException e) {
            e.printStackTrace();
        }

        metricHandler.stop();
    }

    protected abstract void actualRun() throws IOException, NetSortException;
}
