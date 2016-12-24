package eakimov.netsort;

import eakimov.netsort.settings.RunArguments;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Results implements Serializable {
    private final List<Result> results = new ArrayList<>();

    public void addResult(Result result) {
        results.add(result);
    }

    public List<Result> getResults() {
        return results;
    }

    public void exportToCSV(File file) {
        // TODO
        //столбцы - параметры запуска + метрики-результаты
        //строки - [заголовок] + собственно значения
        // т.е. столбцы: a, x, n, m, d, t1, t2, t3
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
