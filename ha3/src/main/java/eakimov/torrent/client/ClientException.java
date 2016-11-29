package eakimov.torrent.client;

import eakimov.torrent.common.TorrentException;

public class ClientException extends TorrentException {
    public ClientException(String message) {
        super(message);
    }
}
