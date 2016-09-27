package eakimov.VCS.commands;

import eakimov.VCS.*;
import eakimov.VCS.errors.BranchManagementException;
import eakimov.VCS.errors.RepositoryException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

@Command(name = "new", description = "Create new branch from scratch")
public class NewBranch extends VCSCommand
{
    @Arguments(description = "New branch name", required = true)
    public String name;

    @Override
    protected void actualRun() throws RepositoryException
    {
        if (state.findBranchByName(name) != null) {
            throw new BranchManagementException(name, "already exists");
        }
        final Branch branch = new Branch(name, null);
        state.addBranch(branch);
        state.setCurrentBranch(branch);
    }
}
