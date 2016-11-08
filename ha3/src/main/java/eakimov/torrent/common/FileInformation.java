package eakimov.torrent.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileInformation implements Serializable {
    private final int id;
    private final String name;
    private final long size;
    private final List<ClientInformation> clients;

    public FileInformation(int id, String name, long size) {
        this.id = id;
        this.name = name;
        this.size = size;
        clients = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public List<ClientInformation> getClients() {
        return clients;
    }

    public void addNewClient(ClientInformation client) {
        clients.add(client);
    }

    public void removeOldClient(ClientInformation client) {
        clients.remove(client);
    }

    @Override
    public String toString() {  // for client
        return String.format("%d: %s (%d)", id, name, size);
    }
}
