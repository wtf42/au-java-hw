package eakimov.netsort.servers;

import eakimov.netsort.Metrics;
import eakimov.netsort.protocol.SortArrayProtocol.ArrayMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskSolver {
    private final Metrics metrics;

    public TaskSolver(Metrics metrics) {
        this.metrics = metrics;
    }

    public ArrayMessage processMessage(ArrayMessage query) {
        List<Integer> values = new ArrayList<>(query.getValuesList());

        Metrics.MetricHandler metricHandler = metrics.createServerQueryMetricHandler();
        metricHandler.start();
        boolean swapped;
        do {
            swapped = false;
            for (int j = 0; j < values.size() - 1; j++) {
                if (values.get(j) > values.get(j + 1)) {
                    Collections.swap(values, j, j + 1);
                    swapped = true;
                }
            }
        } while(swapped);
        metricHandler.stop();
        // перенести в основной код
        // т.к. хотим померить то, что это вычисление распределяется по потокам
        // а не то, насколько оно занимает в одном потоке

        return ArrayMessage.newBuilder().addAllValues(values).build();
    }
}
