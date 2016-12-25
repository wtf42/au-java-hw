package eakimov.netsort;

import eakimov.netsort.settings.RunSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class BatchRun {
    private static final String ARGS_FILE = "results/args.txt";
    public static void main(String[] args) throws Exception {
        try (FileReader fileReader = new FileReader(ARGS_FILE);
             BufferedReader reader = new BufferedReader(fileReader)) {
            while (true) {
                String name = reader.readLine();
                String values = reader.readLine();
                if (name == null || values == null) {
                    break;
                }
                File outputFile = new File("results/"+name+".csv");
                if (outputFile.exists() || name.startsWith("#")) {
                    continue;
                }
                RunSettings settings = RunSettings.parse(values);
                CLIProgressHandler progressHandler =
                        new CLIProgressHandler(settings.getProgressStart(), settings.getProgressEnd());
                NetSortRunner netSortRunner = new NetSortRunner(settings, progressHandler);

                System.out.println(settings.toString());
                netSortRunner.run();
                Results results = netSortRunner.getResults();
                results.exportToCSV(outputFile);
            }
        }
    }

    private static class CLIProgressHandler implements ProgressHandler {
        private final int begin, end;

        private CLIProgressHandler(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        public void setProgress(int value) {
            System.out.println(begin + "/" + end + ": " + value);
        }

        @Override
        public void setCompleted() {
            System.out.println("completed!");
        }

        @Override
        public void setError(String message) {
            System.out.println("error: " + message);
        }
    }
}
