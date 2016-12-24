package eakimov.netsort;

import eakimov.netsort.settings.RunArguments;
import eakimov.netsort.settings.RunSettings;

public class NetSortRunner implements Runnable {
    private final RunSettings settings;
    private final ProgressHandler progressHandler;
    private final Results results;

    public NetSortRunner(RunSettings settings, ProgressHandler progressHandler) {
        this.settings = settings;
        this.progressHandler = progressHandler;
        results = new Results();
    }

    @Override
    public void run() {
        progressHandler.setProgress(settings.getProgressStart());
        for (int n = settings.getnStart(); n <= settings.getnEnd(); n += settings.getnStep()) {
            for (int m = settings.getmStart(); m <= settings.getmEnd(); m += settings.getmStep()) {
                for (int d = settings.getdStart(); d <= settings.getdEnd(); d += settings.getdStep()) {
                    runOne(new RunArguments(settings.getArchId(), settings.getX(), n, m, d));
                    progressHandler.setProgress(settings.getProgressValue(n, m, d));
                }
            }
        }
        progressHandler.setCompleted();
    }

    private void runOne(RunArguments arguments) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //
    }

    public Results getResults() {
        return results;
    }
}
