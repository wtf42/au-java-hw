package eakimov.VCS;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import eakimov.VCS.errors.MergeException;
import eakimov.VCS.errors.RepositoryException;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class VCSMerge {
    public static void merge(Path mergeFrom, Path mergeTo, Path changesTo) throws RepositoryException {
        final DiffVisitor diffVisitor = new DiffVisitor(mergeFrom, mergeTo, changesTo);
        try {
            Files.walkFileTree(mergeFrom, diffVisitor);
            Files.walkFileTree(mergeTo, diffVisitor);
        } catch (IOException e) {
            throw new RepositoryException("unexpected i/o exception: " + e.getMessage());
        }
        if (!diffVisitor.getConflicts().isEmpty()) {
            throw new MergeException("conflicts:\n" + diffVisitor.getConflicts()
                    .stream()
                    .map(Path::toString)
                    .collect(Collectors.joining("\n")));
        }
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

    private static class DiffVisitor extends SimpleFileVisitor<Path> {
        private final Path sourcePathRoot;
        private final Path targetPathRoot;
        private final Path changesPathRoot;
        private final HashSet<Path> processedPaths = new HashSet<>();
        private final List<Path> conflicts;
        private Path relativePath;
        private Path sourcePath;
        private Path targetPath;
        private Path changesPath;

        public DiffVisitor(Path sourcePathRoot, Path targetPathRoot, Path changesPathRoot) {
            this.sourcePathRoot = sourcePathRoot;
            this.targetPathRoot = targetPathRoot;
            this.changesPathRoot = changesPathRoot;
            conflicts = new ArrayList<>();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                                                 BasicFileAttributes attrs) throws IOException {
            relativizePath(dir);
            if (processedPaths.contains(relativePath)) {
                return FileVisitResult.CONTINUE;
            }
            processedPaths.add(relativePath);

            if (Files.exists(sourcePath) && !Files.exists(targetPath)) {
                // dir was deleted
                if (!Files.exists(changesPath) || !Files.isDirectory(changesPath)) {
                    conflicts.add(relativePath);
                } else {
                    FileUtils.deleteDirectory(changesPath.toFile());
                }
                return FileVisitResult.SKIP_SUBTREE;
            } else if (!Files.exists(sourcePath) && Files.exists(targetPath)) {
                // dir was created
                if (Files.exists(changesPath)) {
                    conflicts.add(relativePath);
                } else {
                    Files.createDirectory(relativePath);
                }
                return FileVisitResult.CONTINUE;
            } else if (Files.exists(sourcePath)
                    && Files.exists(targetPath)
                    && Files.isDirectory(sourcePath)
                    && !Files.isDirectory(targetPath)) {
                if (!Files.exists(changesPath) || !Files.isDirectory(changesPath)) {
                    conflicts.add(relativePath);
                } else {
                    FileUtils.deleteDirectory(changesPath.toFile());
                    FileUtils.copyFile(targetPath.toFile(), changesPath.toFile());
                }
                return FileVisitResult.SKIP_SUBTREE;
            } else if (Files.exists(sourcePath)
                    && Files.exists(targetPath)
                    && !Files.isDirectory(sourcePath)
                    && Files.isDirectory(targetPath)) {
                if (!Files.exists(changesPath) || Files.isDirectory(changesPath)) {
                    conflicts.add(relativePath);
                } else {
                    Files.delete(changesPath);
                    FileUtils.copyDirectory(targetPath.toFile(), changesPath.toFile());
                }
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file,
                                         BasicFileAttributes attrs) throws IOException {
            relativizePath(file);
            if (processedPaths.contains(relativePath)) {
                return FileVisitResult.CONTINUE;
            }
            processedPaths.add(relativePath);

            if (Files.exists(sourcePath) && !Files.exists(targetPath)) {
                // file was deleted
                if (!Files.exists(changesPath) || Files.isDirectory(changesPath)) {
                    conflicts.add(relativePath);
                } else {
                    Files.delete(changesPath);
                }
            } else if (!Files.exists(sourcePath) && Files.exists(targetPath)) {
                // file was created
                if (Files.exists(changesPath)) {
                    conflicts.add(relativePath);
                } else {
                    FileUtils.copyFile(targetPath.toFile(), changesPath.toFile());
                }
            } else if (Files.exists(sourcePath)
                    && Files.exists(targetPath)
                    && !Files.isDirectory(sourcePath)
                    && !Files.isDirectory(targetPath)) {
                if (!mergeFile(sourcePath, targetPath, changesPath)) {
                    conflicts.add(relativePath);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        public List<Path> getConflicts() {
            return conflicts;
        }

        private void relativizePath(Path path) {
            if (path.startsWith(sourcePathRoot)) {
                relativePath = sourcePathRoot.relativize(path);
            } else if (path.startsWith(targetPathRoot)) {
                relativePath = targetPathRoot.relativize(path);
            } else {
                // something changed during merge
                throw new RuntimeException("failed to relativize directory " + path.toString());
            }
            sourcePath = sourcePathRoot.resolve(relativePath);
            targetPath = targetPathRoot.resolve(relativePath);
            changesPath = changesPathRoot.resolve(relativePath);
        }
    }
}
