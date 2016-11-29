package eakimov.torrent.tracker;

import eakimov.torrent.common.ClientInformation;
import eakimov.torrent.common.FileInformation;
import eakimov.torrent.common.TorrentTracker;

import java.io.*;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class TrackerFilesInformation implements Serializable {
    private final HashMap<Integer, FileInformation> files = new HashMap<>();
    private final List<ClientInformation> clients = new ArrayList<>();
    private int uniqueIdCounter = 0;

    public synchronized List<FileInformation> getAllFiles() {
        return new ArrayList<>(files.values());  // to make thread-safe iterator
    }

    public synchronized Optional<FileInformation> getFileById(int fileId) {
        return Optional.ofNullable(files.get(fileId));
    }

    public synchronized void addFile(FileInformation fileInformation) {
        files.put(fileInformation.getId(), fileInformation);
    }

    public synchronized void clientUpdate(InetAddress address,
                                          int port,
                                          List<FileInformation> files) {
        clients.stream()
                .filter(c -> c.getAddress().equals(address) && c.getPort() == port)
                .findAny()
                .ifPresent(this::removeOldClient);

        final ClientInformation updatedClient =
                new ClientInformation(address, port, new Date().toInstant(), files);
        clients.add(updatedClient);
        updatedClient.getFiles().forEach(f -> f.addNewClient(updatedClient));
    }

    public synchronized List<ClientInformation> getClientsAndActualize(FileInformation file) {
        final Instant now = new Date().toInstant();
        new ArrayList<>(file.getClients()).stream()
                .filter(client -> clientExpired(client.getUpdateTime(), now))
                .forEach(this::removeOldClient);
        return file.getClients();
    }

    public synchronized int nextUniqueId() {
        return uniqueIdCounter++;
    }

    public void saveToFile(File file) throws IOException {
        try (FileOutputStream fileStream = new FileOutputStream(file);
             ObjectOutputStream objectSteam = new ObjectOutputStream(fileStream)) {
            objectSteam.writeObject(this);
        }
    }

    public static TrackerFilesInformation loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (FileInputStream fileStream = new FileInputStream(file);
             ObjectInputStream objectStream = new ObjectInputStream(fileStream)) {
            return (TrackerFilesInformation) objectStream.readObject();
        }
    }

    public static TrackerFilesInformation loadFromFileOrNew(File file) {
        try {
            return loadFromFile(file);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("tracker files information not found, created new");
            return new TrackerFilesInformation();
        }
    }

    protected boolean clientExpired(Instant updateTime, Instant now) {
        return Duration.between(updateTime, now)
                .compareTo(TorrentTracker.FILE_UPDATE_EXPIRATION) > 0;
    }

    private void removeOldClient(ClientInformation client) {
        client.getFiles().forEach(f -> f.removeOldClient(client));
        clients.remove(client);
    }
}
