package eakimov.torrent.client;

import eakimov.torrent.common.TorrentTracker;
import eakimov.torrent.common.FileInformation;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileStatus implements Serializable {
    private final File localPath;
    private final Set<Integer> availableParts;
    private FileInformation trackerInfo;
    private boolean ready;

    public FileStatus(File localPath, int fileId) {
        this.localPath = localPath;
        availableParts = fillAvailableSize(localPath.length());
        trackerInfo = new FileInformation(fileId, localPath.getName(), localPath.length());
        ready = true;
    }

    public FileStatus(File localPath, FileInformation trackerInfo) {
        this.localPath = localPath;
        availableParts = new HashSet<>();
        this.trackerInfo = trackerInfo;
        ready = trackerInfo.getSize() == 0;
    }

    public File getLocalPath() {
        return localPath;
    }

    public synchronized Set<Integer> getAvailableParts() {
        return new HashSet<>(availableParts);
    }

    public FileInformation getTrackerInfo() {
        return trackerInfo;
    }

    public boolean isReady() {
        return ready;
    }

    public synchronized void setPartAvailable(int part) {
        availableParts.add(part);
        ready = availableParts.size() == getPartsCount();
    }

    public int getPartsCount() {
        return countPartsFromSize(trackerInfo.getSize());
    }

    public long getPartSize(int part) {
        if (part == getPartsCount() - 1) {
            final long size = trackerInfo.getSize() % TorrentTracker.FILE_PART_SIZE;
            if (size != 0) {
                return size;
            }
            return TorrentTracker.FILE_PART_SIZE;
        }
        return TorrentTracker.FILE_PART_SIZE;
    }

    public synchronized Set<Integer> getRemainingParts() {
        return Stream.iterate(0, i -> i + 1).limit(getPartsCount())
                .filter(p -> !availableParts.contains(p))
                .collect(Collectors.toSet());
    }

    private static Set<Integer> fillAvailableSize(long fileLength) {
        final int partsCount = countPartsFromSize(fileLength);
        return Stream.iterate(0, i -> i + 1).limit(partsCount).collect(Collectors.toSet());
    }

    private static int countPartsFromSize(long fileLength) {
        return (int) ((fileLength + TorrentTracker.FILE_PART_SIZE - 1) / TorrentTracker.FILE_PART_SIZE);
    }
}
