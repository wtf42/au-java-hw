package eakimov.VCS.commands;

import eakimov.VCS.Branch;
import eakimov.VCS.Revision;
import eakimov.VCS.VCSCommand;
import eakimov.VCS.errors.BranchManagementException;
import eakimov.VCS.errors.RepositoryException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(name = "log", description = "Show log of changes")
public class Log extends VCSCommand
{
    @Arguments(description = "Number of commits", required = false)
    public int count = 5;

    @Override
    protected void actualRun() throws RepositoryException
    {
        final Branch currentBranch = state.getCurrentBranch();
        if (currentBranch == null) {
            throw new BranchManagementException("current", "not set");
        }
        System.out.println("branch: " + currentBranch.getName());
        Revision revision = state.getCurrentRevision();
        if (revision == null) {
            System.out.println("revision history is empty!");
        } else {
            for (int i = 0; i < count && revision != null; i++) {
                printRevisionMessage(revision);
                revision = revision.getParent();
            }
        }
    }

    private static void printRevisionMessage(Revision revision) {
        System.out.println(Stream.generate(() -> "-").limit(80).collect(Collectors.joining()));
        System.out.printf("revision: %d (%s)\n", revision.getId(), revision.getBranch().getName());
        if (revision.getMergeParent() != null) {
            System.out.println("merged with: "
                    + revision.getMergeParent().getBranch().getName()
                    + " at revision "
                    + revision.getMergeParent().getId());
        }
        System.out.println("commit message: " + revision.getCommitMessage());
    }
}
