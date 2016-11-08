package eakimov.torrent.tracker;

import eakimov.torrent.common.TorrentException;

public class TrackerException extends TorrentException {
    public TrackerException(String message) {
        super(message);
    }
}
