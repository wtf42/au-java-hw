package eakimov.VCS.commands;

import eakimov.VCS.Revision;
import eakimov.VCS.VCSCommand;
import eakimov.VCS.VCSFileUtils;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.errors.UnexpectedIOException;
import io.airlift.airline.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Command(name = "status", description = "Show added / deleted / changed / unstaged files")
public class Status extends VCSCommand
{
    private Revision currentRevision;
    private List<String> addedFiles;
    private List<String> deletedFiles;
    private List<String> changedFiles;
    private List<String> unstagedFiles;

    @Override
    protected void actualRun() throws RepositoryException
    {
        addedFiles = new ArrayList<>();
        deletedFiles = new ArrayList<>();
        changedFiles = new ArrayList<>();
        unstagedFiles = new ArrayList<>();

        HashSet<String> allFiles = new HashSet<>();
        allFiles.addAll(state.getStageFiles());
        allFiles.addAll(VCSFileUtils.getWorkDirFiles(Paths.get(repositoryWorkingDirectory)));

        currentRevision = state.getCurrentRevision();
        if (currentRevision != null) {
            allFiles.addAll(currentRevision.getFiles());
        }

        try {
            for (String file : allFiles) {
                processFile(file);
            }
        } catch (IOException e) {
            throw new UnexpectedIOException(e);
        }

        if (!addedFiles.isEmpty()) {
            System.out.println("added files:");
            addedFiles.forEach(System.out::println);
        }
        if (!deletedFiles.isEmpty()) {
            System.out.println("deleted files:");
            deletedFiles.forEach(System.out::println);
        }
        if (!changedFiles.isEmpty()) {
            System.out.println("changed files:");
            changedFiles.forEach(System.out::println);
        }
        if (!unstagedFiles.isEmpty()) {
            System.out.println("unstaged files:");
            unstagedFiles.forEach(System.out::println);
        }
    }

    private void processFile(String fileName) throws IOException {
        final Path stageFilePath = getStageFilePath(fileName);
        final Path workDirFilePath = Paths.get(repositoryWorkingDirectory, fileName);
        final Path revisionFilePath = VCSFileUtils.getRevisionFilePath(repositoryWorkingDirectory,
                currentRevision,
                fileName);

        if (!state.isStageFile(fileName)) {
            if (!isFilesEqual(workDirFilePath, revisionFilePath)) {
                unstagedFiles.add(fileName);
            }
            // else: nothing changed
        } else {
            if (!isFilesEqual(workDirFilePath, stageFilePath)) {
                // staged and changed
                unstagedFiles.add(fileName);
            } else {
                // staged, changed or not
                if (Files.exists(stageFilePath) && !Files.exists(revisionFilePath)) {
                    addedFiles.add(fileName);
                } else if (!Files.exists(stageFilePath) && Files.exists(revisionFilePath)) {
                    deletedFiles.add(fileName);
                } else if (!VCSFileUtils.isExistingFilesEqual(stageFilePath, revisionFilePath)) {
                    changedFiles.add(fileName);
                }
                // else: staged, but not changed
            }
        }
    }

    private static boolean isFilesEqual(Path path1, Path path2) throws IOException {
        final boolean exists1 = Files.exists(path1);
        final boolean exists2 = Files.exists(path2);
        if (!exists1 && !exists2) {
            return true;
        } else if (exists1 != exists2) {
            return false;
        } else {
            return VCSFileUtils.isExistingFilesEqual(path1, path2);
        }
    }
}
