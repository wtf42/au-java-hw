package eakimov.VCS.commands;

import eakimov.VCS.Branch;
import eakimov.VCS.VCSCommand;
import eakimov.VCS.errors.BranchManagementException;
import eakimov.VCS.errors.RepositoryException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "clone", description = "Create new branch from current state")
public class CloneBranch extends VCSCommand
{
    @Arguments(description = "New branch name", required = true)
    public String name;

    @Override
    protected void actualRun() throws RepositoryException
    {
        final Branch currentBranch = state.getCurrentBranch();
        if (currentBranch == null) {
            throw new BranchManagementException("current", "not set");
        }
        if (state.findBranchByName(name) != null) {
            throw new BranchManagementException(name, "already exists");
        }
        final Branch clonedBranch = new Branch(name, state.getCurrentRevision());
        state.addBranch(clonedBranch);
        state.setCurrentBranch(clonedBranch);
    }
}
