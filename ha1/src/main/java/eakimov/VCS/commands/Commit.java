package eakimov.VCS.commands;

import eakimov.VCS.*;
import eakimov.VCS.errors.BranchManagementException;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.errors.StageException;
import eakimov.VCS.errors.UnexpectedIOException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Command(name = "commit", description = "Commit changes to current branch")
public class Commit extends VCSCommand
{
    @Arguments(description = "Commit message", required = true)
    public List<String> message;

    @Override
    protected void actualRun() throws RepositoryException
    {
        final String messageString = message.stream().collect(Collectors.joining(" "));
        final Branch currentBranch = state.getCurrentBranch();
        if (currentBranch == null) {
            throw new BranchManagementException("current", "not set");
        }
        if (state.getStageFiles().isEmpty()) {
            throw new StageException("nothing to commit");
        }

        final Revision currentRevision = state.getCurrentRevision();
        final Map<String, Revision> nextRevisionFiles = new HashMap<>();
        if (currentRevision != null) {
            nextRevisionFiles.putAll(currentRevision.getAllFileRevisions());
        }
        for (String file : state.getStageFiles()) {
            if (Files.exists(getStageFilePath(file))) {
                nextRevisionFiles.put(file, null);
            } else {
                nextRevisionFiles.remove(file);
            }
        }
        final Revision nextRevision = new Revision(currentBranch,
                currentRevision,
                messageString,
                nextRevisionFiles);

        final Path nextRevisionDirectoryPath = getRevisionPath(nextRevision);
        try {
            for (String file : state.getStageFiles()) {
                final Path srcFilePath = getStageFilePath(file);
                final Path destFilePath = nextRevisionDirectoryPath.resolve(file);
                if (Files.exists(srcFilePath)) {
                    FileUtils.copyFile(srcFilePath.toFile(), destFilePath.toFile());
                }
            }
            VCSFileUtils.cleanUpDirectory(Paths.get(repositoryWorkingDirectory,
                    VCSDefaults.STATE_DIRECTORY,
                    VCSDefaults.STAGE_DIRECTORY));
        } catch (IOException e) {
            throw new UnexpectedIOException(e);
        }
        state.clearStageFiles();

        currentBranch.setHeadRevision(nextRevision);
        state.setCurrentRevision(nextRevision);
    }
}
