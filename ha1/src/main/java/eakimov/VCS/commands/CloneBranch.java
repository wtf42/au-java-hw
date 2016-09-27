package eakimov.VCS.commands;

import eakimov.VCS.Branch;
import eakimov.VCS.Revision;
import eakimov.VCS.VCSCommand;
import eakimov.VCS.VCSFileUtils;
import eakimov.VCS.errors.BranchManagementException;
import eakimov.VCS.errors.RepositoryException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import java.nio.file.Path;
import java.nio.file.Paths;

@Command(name = "clone", description = "Create new branch from current state")
public class CloneBranch extends VCSCommand
{
    @Arguments(description = "New branch name", required = true)
    public String name;

    @Override
    protected void actualRun() throws RepositoryException
    {
        Branch currentBranch = state.getCurrentBranch();
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
