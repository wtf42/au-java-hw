package eakimov.torrent.client;

import eakimov.torrent.common.FileInformation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Storage implements Serializable {
    private final List<FileStatus> files = new ArrayList<>();

    public void saveToFile(File file) throws IOException {
        try (FileOutputStream fileStream = new FileOutputStream(file);
             ObjectOutputStream objectSteam = new ObjectOutputStream(fileStream)) {
            objectSteam.writeObject(this);
        }
    }

    public static Storage loadFromFile(File file) throws IOException, ClassNotFoundException {
        try (FileInputStream fileStream = new FileInputStream(file);
             ObjectInputStream objectStream = new ObjectInputStream(fileStream)) {
            return (Storage) objectStream.readObject();
        }
    }

    public static Storage loadFromFileOrNew(File file) {
        try {
            return loadFromFile(file);
        } catch (ClassNotFoundException | IOException e) {
            System.out.println("client files information not found, created new");
            return new Storage();
        }
    }

    public synchronized List<FileStatus> getFiles() {
        return new ArrayList<>(files);
    }

    public synchronized Optional<FileStatus> getFileStatus(int id) {
        return files.stream().filter(f -> f.getTrackerInfo().getId() == id).findAny();
    }

    public synchronized FileStatus addExistingFile(File localPath, int newFileId) {
        final FileStatus fileStatus = new FileStatus(localPath, newFileId);
        files.add(fileStatus);
        return fileStatus;
    }

    public synchronized FileStatus addDownloadFile(File localPath, FileInformation trackerInfo) {
        final FileStatus fileStatus = new FileStatus(localPath, trackerInfo);
        files.add(fileStatus);
        return fileStatus;
    }
}
