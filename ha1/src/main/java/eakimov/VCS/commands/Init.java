package eakimov.VCS.commands;

import eakimov.VCS.VCSFileUtils;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.RepositoryState;
import eakimov.VCS.VCSCommand;
import io.airlift.airline.Command;

@Command(name = "init", description = "Init new repository")
public class Init extends VCSCommand
{
    public void run()
    {
        state = new RepositoryState();
        try {
            VCSFileUtils.initVCSFiles(repositoryRootPath);
            lockState();
            saveState();
            unlockState();
        } catch (RepositoryException e) {
            System.err.println(e.getMessage());
        }
    }
}
