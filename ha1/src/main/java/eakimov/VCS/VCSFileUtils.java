package eakimov.VCS;

import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.errors.UnexpectedIOException;
import eakimov.VCS.errors.UnrecoverableRepositoryException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

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

    public static void copyAllFiles(Path srcDirectory,
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
            throw new UnexpectedIOException(e);
        }
    }

    public static void cleanUpDirectory(Path directory) throws RepositoryException {
        final File[] files = directory.toFile().listFiles((FileFilter) stateDirectoryFilter);
        if (files == null) {
            throw new RepositoryException("failed to cleanup: invalid directory");
        }
        try {
            for (File file : files) {
                FileUtils.forceDelete(file);
            }
        } catch (IOException e) {
            throw new UnexpectedIOException(e);
        }
    }

    public static List<String> getWorkDirFiles(Path directory) {
        return FileUtils.listFiles(directory.toFile(),
                TrueFileFilter.INSTANCE,
                stateDirectoryFilter)
                .stream().map(f -> directory.relativize(f.toPath()).toString())
                .collect(Collectors.toList());
    }

    public static boolean isExistingFilesEqual(Path path1, Path path2) throws IOException {
        String contents1 = FileUtils.readFileToString(path1.toFile(), Charset.defaultCharset());
        String contents2 = FileUtils.readFileToString(path2.toFile(), Charset.defaultCharset());
        return contents1.equals(contents2);
    }

    public static Path getRevisionFilePath(String workingDirectory,
                                           Revision revision,
                                           String file) {
        String revisionDirectory = VCSDefaults.EMPTY_REVISION_DIRECTORY;
        if (revision != null) {
            Revision fileRevision = revision.getFileRevision(file);
            if (fileRevision != null) {
                revisionDirectory = fileRevision.getRevisionDirectory();
            }
        }
        return Paths.get(workingDirectory,
                VCSDefaults.STATE_DIRECTORY,
                revisionDirectory,
                file);
    }

    private static final IOFileFilter stateDirectoryFilter =
            new NotFileFilter(new AndFileFilter(
                    DirectoryFileFilter.INSTANCE,
                    new NameFileFilter(VCSDefaults.STATE_DIRECTORY)));
}
