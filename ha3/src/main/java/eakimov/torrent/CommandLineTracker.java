package eakimov.torrent;

import eakimov.torrent.tracker.TrackerFilesInformation;
import eakimov.torrent.tracker.Tracker;

import java.io.File;
import java.io.IOException;

public class CommandLineTracker {
    private static final String STATE_FILENAME = "tracker.dat";

    public static void main(String[] args) {
        final File stateFile = new File(STATE_FILENAME);
        final TrackerFilesInformation info =
                TrackerFilesInformation.loadFromFileOrNew(stateFile);
        try (Tracker tracker = new Tracker(info)) {
            System.out.println("started!");
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
            System.out.println("stopping...");
        } catch (IOException | InterruptedException e) {
            System.err.println("error occurred: " + e.getMessage());
        } finally {
            try {
                info.saveToFile(stateFile);
            } catch (IOException e) {
                System.err.println("failed to save tracker state: " + e.getMessage());
            }
        }
    }
}
