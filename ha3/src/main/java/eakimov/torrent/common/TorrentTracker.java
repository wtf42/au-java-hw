package eakimov.torrent.common;

import java.time.Duration;

public class TorrentTracker {
    public static final int TRACKER_PORT = 8081;
    public static final long FILE_PART_SIZE = 1024 * 1024;
    public static final Duration FILE_UPDATE_EXPIRATION = Duration.ofMinutes(5);
    public static final long CLIENT_UPDATE_INTERVAL = FILE_UPDATE_EXPIRATION.toMillis() / 2;
    public static final long NEW_CLIENTS_WAITING_TIME = 100;  // ms to sleep
}
