package eakimov.VCS.commands;

import eakimov.VCS.*;
import eakimov.VCS.errors.BranchManagementException;
import eakimov.VCS.errors.RepositoryException;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(name = "checkout", description = "Checkout at revision")
public class Checkout extends VCSCommand
{
    @Option(name = "-rev", description = "Revision", required = false)
    public int revisionIdx = -1;
    @Option(name = "-branch", description = "Branch name", required = false)
    public String branchName;

    @Override
    protected void actualRun() throws RepositoryException
    {
        Branch branch;
        if (branchName == null) {
            branch = state.getCurrentBranch();
            if (branch == null) {
                throw new BranchManagementException("branch name should be specified, current branch is not set");
            }
        } else {
            branch = state.findBranchByName(branchName);
            if (branch == null) {
                throw new BranchManagementException(branchName, "not found");
            }
        }

        Revision revision = branch.getHeadRevision();
        if (revisionIdx != -1) {
            while (revision != null && revision.getId() != revisionIdx) {
                revision = revision.getParent();
            }
            if (revision == null || revision.getId() != revisionIdx) {
                throw new RepositoryException("revision " + Integer.toString(revisionIdx) + " not found");
            }
        }

        final Path repositoryPath = Paths.get(repositoryRootPath);
        final Path revisionPath = getRevisionPath(revision);
        VCSFileUtils.cleanUpDirectory(repositoryPath);
        VCSFileUtils.copyRevisionFiles(revisionPath, repositoryPath);

        state.setCurrentBranch(branch);
        state.setCurrentRevision(revision);
    }
}
