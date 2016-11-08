package eakimov.torrent.common;

import java.io.Serializable;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClientInformation implements Serializable {
    private final InetAddress address;
    private final int port;
    private final Instant updateTime;
    private final List<FileInformation> files;

    public ClientInformation(InetAddress address,
                             int port,
                             Instant updateTime,
                             List<FileInformation> files) {
        this.address = address;
        this.port = port;
        this.updateTime = updateTime;
        this.files = files;
    }

    public ClientInformation(InetAddress address, int port) {
        this(address, port, new Date().toInstant(), new ArrayList<>());
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }

    public List<FileInformation> getFiles() {
        return files;
    }
}
