package eakimov.VCS.commands;

import eakimov.VCS.*;
import eakimov.VCS.errors.BranchManagementException;
import eakimov.VCS.errors.RepositoryException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import java.nio.file.Paths;

@Command(name = "delete", description = "Delete branch")
public class DeleteBranch extends VCSCommand
{
    @Arguments(description = "Branch name", required = true)
    public String name;

    @Override
    protected void actualRun() throws RepositoryException
    {
        final Branch branch = state.findBranchByName(name);
        if (branch == null) {
            throw new BranchManagementException(name, "not found");
        }
        state.deleteBranch(branch);

        Revision revision = branch.getHeadRevision();
        while (revision != null) {
            if (revision.getBranch() == branch) {
                VCSFileUtils.removeDirectory(getRevisionPath(revision));
            }
            revision = revision.getParent();
        }
    }
}
