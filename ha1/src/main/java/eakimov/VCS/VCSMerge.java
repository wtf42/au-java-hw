package eakimov.VCS;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import eakimov.VCS.errors.MergeException;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.errors.UnexpectedIOException;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static eakimov.VCS.VCSFileUtils.getRevisionFilePath;

public class VCSMerge {
    public static void merge(String workingDirectory,
                             Revision mergeFrom,
                             Revision mergeTo,
                             Path changesTo) throws RepositoryException {
        final Set<String> files = new HashSet<>();
        if (mergeFrom != null) {
            files.addAll(mergeFrom.getFiles());
        }
        files.addAll(mergeTo.getFiles());

        final List<String> conflicts = new ArrayList<>();
        try {
            for (String file : files) {
                final Path sourcePath = getRevisionFilePath(workingDirectory, mergeFrom, file);
                final Path targetPath = getRevisionFilePath(workingDirectory, mergeTo, file);
                final Path changesPath = changesTo.resolve(file);
                if (!processFile(sourcePath, targetPath, changesPath)) {
                    conflicts.add(file);
                }
            }
        } catch (IOException e) {
            throw new UnexpectedIOException(e);
        }
        if (!conflicts.isEmpty()) {
            throw new MergeException("conflicts:\n" + conflicts
                    .stream().collect(Collectors.joining("\n")));
        }
    }

    private static boolean processFile(Path sourcePath,
                                       Path targetPath,
                                       Path changesPath) throws IOException {
        if (Files.exists(sourcePath) && !Files.exists(targetPath)) {
            // file was deleted
            if (!Files.exists(changesPath) || Files.isDirectory(changesPath)) {
                return false;
            } else {
                Files.delete(changesPath);
            }
        } else if (!Files.exists(sourcePath) && Files.exists(targetPath)) {
            // file was created
            if (Files.exists(changesPath)) {
                return false;
            } else {
                FileUtils.copyFile(targetPath.toFile(), changesPath.toFile());
            }
        } else if (Files.exists(sourcePath)
                && Files.exists(targetPath)
                && !Files.isDirectory(sourcePath)
                && !Files.isDirectory(targetPath)) {
            if (!mergeFile(sourcePath, targetPath, changesPath)) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private static boolean mergeFile(Path srcFile, Path destFile, Path changesFile) throws IOException {
        final List<String> srcLines = FileUtils.readLines(srcFile.toFile(), Charset.defaultCharset());
        final List<String> destLines = FileUtils.readLines(destFile.toFile(), Charset.defaultCharset());
        final List<String> changesLines = FileUtils.readLines(changesFile.toFile(), Charset.defaultCharset());
        final Patch patch = DiffUtils.diff(srcLines, destLines);
        try {
            final List<?> newLines = patch.applyTo(changesLines);
            FileUtils.writeLines(changesFile.toFile(), newLines);
        } catch (PatchFailedException ignored) {
            return false;
        }
        return true;
    }
}
