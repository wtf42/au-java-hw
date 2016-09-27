package eakimov.VCS.commands;

import eakimov.VCS.*;
import eakimov.VCS.errors.BranchManagementException;
import eakimov.VCS.errors.RepositoryException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
        final Revision currentRevision = state.getCurrentRevision();
        final Revision nextRevision = new Revision(currentBranch, currentRevision, messageString);

        final Path rootDirectory = Paths.get(repositoryRootPath);
        final Path nextRevisionDirectory = getRevisionPath(nextRevision);
        VCSFileUtils.copyRevisionFiles(rootDirectory, nextRevisionDirectory);

        currentBranch.setHeadRevision(nextRevision);
        state.setCurrentRevision(nextRevision);
    }
}
