package eakimov.netsort;

import eakimov.netsort.settings.RunArguments;
import eakimov.netsort.settings.RunSettings;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Results implements Serializable {
    private final RunSettings settings;
    private final List<Result> results = new ArrayList<>();

    public Results(RunSettings settings) {
        this.settings = settings;
    }

    public void addResult(Result result) {
        results.add(result);
    }

    public void export(File directory) throws IOException {
        Path directoryPath = Paths.get(directory.getAbsolutePath());

        exportToCSV(directoryPath.resolve("out.csv").toFile());

        writeDescription(directoryPath.resolve("description.txt").toFile());
        writeStats(directoryPath.resolve("m1.txt").toFile(), Metrics::summarizeServerQueryTimes);
        writeStats(directoryPath.resolve("m2.txt").toFile(), Metrics::summarizeServerClientTimes);
        writeStats(directoryPath.resolve("m3.txt").toFile(), Metrics::summarizeClientTimes);
    }

    public void exportToCSV(File file) throws IOException {
        try (PrintWriter printWriter = new PrintWriter(file)) {
            //printWriter.println("a,x,n,m,d,t1,t2,t3");
            printWriter.println("arch,requests,size,clients,delay,sort time,query time,client time");
            for (Result result : results) {
                printWriter.format("%d,%d,%d,%d,%d,%f,%f,%f",
                        result.getArguments().getArch(),
                        result.getArguments().getX(),
                        result.getArguments().getN(),
                        result.getArguments().getM(),
                        result.getArguments().getDelta(),
                        result.getMetrics().summarizeServerQueryTimes(),
                        result.getMetrics().summarizeServerClientTimes(),
                        result.getMetrics().summarizeClientTimes());
                printWriter.println();
            }
        }
    }

    private void writeStats(File file, MetricSelector selector) throws IOException {
        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.println(settings.getSelectedName() + ",value");
            for (Result result : results) {
                printWriter.format("%d,%f",
                        settings.getSelectedValue(result.getArguments()),
                        selector.select(result.metrics));
                printWriter.println();
            }
        }
    }

    private void writeDescription(File file) throws IOException {
        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.println(settings.toString());
        }
    }

    private interface MetricSelector {
        double select(Metrics metrics);
    }

    public static class Result {
        private final RunArguments arguments;
        private final Metrics metrics;

        public Result(RunArguments arguments, Metrics metrics) {
            this.arguments = arguments;
            this.metrics = metrics;
        }

        public RunArguments getArguments() {
            return arguments;
        }

        public Metrics getMetrics() {
            return metrics;
        }
    }
}
