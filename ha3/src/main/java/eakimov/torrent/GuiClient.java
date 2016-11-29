package eakimov.torrent;

import eakimov.torrent.client.Client;
import eakimov.torrent.client.ClientException;
import eakimov.torrent.client.FileStatus;
import eakimov.torrent.client.Storage;
import eakimov.torrent.common.FileInformation;
import eakimov.torrent.common.TorrentTracker;
import eakimov.torrent.gui.TorrentGUI;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GuiClient {
    private static final File STATE_FILE = new File("client.dat");

    private final Storage storage = Storage.loadFromFileOrNew(STATE_FILE);
    private final Client client = new Client(storage, "localhost", TorrentTracker.TRACKER_PORT);
    private volatile TorrentGUI gui;

    private final Subscriber<Boolean> exitSubscriber = new ExitSubscriber();
    private final Subscriber<Boolean> updateSubscriber = new UpdateSubscriber();
    private final Subscriber<DownloadInfo> downloadSubscriber = new DownloadSubscriber();
    private final Subscriber<File> uploadSubscriber = new UploadSubscriber();
    private final PublishSubject<List<FileInformation>> updateObservable = PublishSubject.create();
    private final PublishSubject<FileStatus> progressObservable = PublishSubject.create();

    public static void main(String[] args) {
        new GuiClient().run();
    }

    private void run() {
        try {
            client.startP2p(0);
            client.scheduleUpdates();
        } catch (ClientException | IOException e) {
            System.err.println("error occurred: " + e.getMessage());
            return;
        }
        java.awt.EventQueue.invokeLater(() -> {
            gui = new TorrentGUI();
            gui.setVisible(true);

            final Subscriber<FileStatus> progressSubscriber = gui.getProgressSubscriber();
            storage.getFiles().forEach(progressSubscriber::onNext);

            gui.subscribeExit(exitSubscriber);
            gui.subscribeUpdate(updateSubscriber);
            gui.subscribeDownload(downloadSubscriber);
            gui.subscribeUpload(uploadSubscriber);
            updateObservable.subscribe(gui.getTrackerFilesSubscriber());
            progressObservable.subscribe(gui.getProgressSubscriber());
        });
    }

    public static class DownloadInfo {
        private final File file;
        private final FileInformation trackerInfo;

        public DownloadInfo(File file, FileInformation trackerInfo) {
            this.file = file;
            this.trackerInfo = trackerInfo;
        }

        public File getFile() {
            return file;
        }

        public FileInformation getTrackerInfo() {
            return trackerInfo;
        }
    }

    private class ExitSubscriber extends Subscriber<Boolean> {
        @Override
        public void onCompleted() {
            try {
                storage.saveToFile(STATE_FILE);
                client.stopUpdates();
                client.stopP2p();
            } catch (IOException | InterruptedException | ClientException e) {
                final String message = e.getMessage() == null ? e.toString() : e.getMessage();
                System.err.println("failed to stop client: " + message);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            System.err.println("error occurred: " + throwable.getMessage());
            onCompleted();
        }

        @Override
        public void onNext(Boolean success) {}
    }

    private class UpdateSubscriber extends Subscriber<Boolean> {
        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable throwable) {}

        @Override
        public void onNext(Boolean b) {
            try {
                updateObservable.onNext(client.listFiles());
            } catch (ClientException | IOException e) {
                updateObservable.onError(e);
            }
        }
    }

    private class DownloadSubscriber extends Subscriber<DownloadInfo> {
        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable throwable) {}

        @Override
        public void onNext(DownloadInfo downloadInfo) {
            client.downloadFileObservable(downloadInfo.getTrackerInfo(), downloadInfo.getFile())
                    .observeOn(Schedulers.newThread())
                    .subscribe(gui.getProgressSubscriber());
        }
    }

    private class UploadSubscriber extends Subscriber<File> {
        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable throwable) {}

        @Override
        public void onNext(File file) {
            try {
                progressObservable.onNext(client.uploadFile(file));
                client.update();
            } catch (ClientException | IOException e) {
                progressObservable.onError(e);
            }
        }
    }
}
