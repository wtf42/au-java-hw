package eakimov.VCS.commands;

import eakimov.VCS.VCSCommand;
import eakimov.VCS.errors.RepositoryException;
import io.airlift.airline.Command;

import java.util.List;

@Command(name = "list", description = "Print all branches in repository")
public class ListBranches extends VCSCommand
{
    @Override
    protected void actualRun() throws RepositoryException
    {
        final List<String> branchNames = state.getAllBranchNames();
        if (branchNames.isEmpty()) {
            System.out.println("repository is empty");
        } else {
            branchNames.forEach(System.out::println);
        }
    }
}
