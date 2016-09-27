package eakimov.VCS;

import eakimov.VCS.errors.FilesException;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.errors.UnrecoverableRepositoryException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;

import java.io.*;
import java.nio.file.*;

public class VCSFileUtils {
    public static RepositoryState loadState(Path path) throws RepositoryException {
        try {
            final FileInputStream fileStream = new FileInputStream(path.toFile());
            final ObjectInputStream objectStream = new ObjectInputStream(fileStream);
            final RepositoryState state = (RepositoryState) objectStream.readObject();
            objectStream.close();
            fileStream.close();
            return state;
        } catch (IOException | ClassNotFoundException e) {
            throw new UnrecoverableRepositoryException("failed to load repository state: " + e.getMessage());
        }
    }

    public static void saveState(Path path, RepositoryState state) throws RepositoryException {
        try {
            final FileOutputStream fileStream = new FileOutputStream(path.toFile());
            final ObjectOutputStream objectSteam = new ObjectOutputStream(fileStream);
            objectSteam.writeObject(state);
            objectSteam.close();
            fileStream.close();
        } catch (IOException e) {
            throw new UnrecoverableRepositoryException("failed to save repository state: " + e.getMessage());
        }
    }

    public static void initVCSFiles(String repositoryRoot) throws RepositoryException {
        try {
            final Path rootDirectory = Paths.get(repositoryRoot,
                    VCSDefaults.STATE_DIRECTORY);
            if (Files.notExists(rootDirectory)) {
                Files.createDirectory(rootDirectory);
            }
            final Path stateFile = Paths.get(repositoryRoot,
                    VCSDefaults.STATE_DIRECTORY,
                    VCSDefaults.STATE_FILENAME);
            if (Files.notExists(stateFile)) {
                Files.createFile(stateFile);
            }
            final Path emptyRevisionDirectory = Paths.get(repositoryRoot,
                    VCSDefaults.STATE_DIRECTORY,
                    VCSDefaults.EMPTY_REVISION_DIRECTORY);
            if (Files.notExists(emptyRevisionDirectory)) {
                Files.createDirectory(emptyRevisionDirectory);
            }
        } catch (IOException e) {
            throw new UnrecoverableRepositoryException("failed to init repository files: " + e.getMessage());
        }
    }

    public static void copyRevisionFiles(Path srcDirectory,
                                         Path destDirectory) throws RepositoryException {
        try {
            if (Files.notExists(destDirectory)) {
                Files.createDirectory(destDirectory);
            }
            FileUtils.copyDirectory(srcDirectory.toFile(),
                    destDirectory.toFile(),
                    stateDirectoryFilter);
        } catch (IOException e) {
            throw new RepositoryException("failed to copy revision files: " + e.getMessage());
        }
    }

    public static void removeDirectory(Path directory) throws RepositoryException {
        try {
            FileUtils.deleteDirectory(directory.toFile());
        } catch (IOException e) {
            throw new FilesException(directory.toString(), "delete", e.getMessage());
        }
    }

    public static void cleanUpDirectory(Path directory) throws RepositoryException {
        final File[] files = directory.toFile().listFiles(stateDirectoryFilter);
        if (files == null) {
            throw new FilesException(directory.toString(), "cleanup", "invalid directory to cleanup");
        }
        try {
            for (File file : files) {
                FileUtils.forceDelete(file);
            }
        } catch (IOException e) {
            throw new FilesException(directory.toString(), "cleanup", e.getMessage());
        }
    }

    private static final FileFilter stateDirectoryFilter =
            new NotFileFilter(new AndFileFilter(
                    DirectoryFileFilter.INSTANCE,
                    new NameFileFilter(VCSDefaults.STATE_DIRECTORY)));
}
