package eakimov.netsort;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Metrics implements Serializable {
    private final List<Duration> serverQueryTimes = new ArrayList<>();
    private final List<Duration> serverClientTimes = new ArrayList<>();
    private final List<Duration> clientTimes = new ArrayList<>();

    public MetricHandler createServerQueryMetricHandler() {
        return new MetricHandler(serverQueryTimes);
    }
    public MetricHandler createServerClientMetricHandler() {
        return new MetricHandler(serverClientTimes);
    }
    public MetricHandler createClientMetricHandler() {
        return new MetricHandler(clientTimes);
    }

    public static class MetricHandler {
        private final List<Duration> times;
        private Instant startTime;
        private Instant endTime;

        private MetricHandler(List<Duration> times) {
            this.times = times;
        }

        public void start() {
            startTime = new Date().toInstant();
        }

        public void stop() {
            endTime = new Date().toInstant();
        }

        public void report() {
            synchronized (times) {
                times.add(Duration.between(startTime, endTime));
            }
        }
    }
}
