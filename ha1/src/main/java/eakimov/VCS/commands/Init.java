package eakimov.VCS.commands;

import eakimov.VCS.VCSDefaults;
import eakimov.VCS.VCSFileUtils;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.RepositoryState;
import eakimov.VCS.VCSCommand;
import io.airlift.airline.Command;

import java.nio.file.Files;
import java.nio.file.Paths;

@Command(name = "init", description = "Init new repository")
public class Init extends VCSCommand
{
    public void run()
    {
        if (Files.exists(Paths.get(repositoryWorkingDirectory,
                VCSDefaults.STATE_DIRECTORY,
                VCSDefaults.STATE_FILENAME))) {
            System.err.println("repository already exists");
            return;
        }
        try {
            state = new RepositoryState();
            VCSFileUtils.initVCSFiles(repositoryWorkingDirectory);
            lockState();
            saveState();
            unlockState();
        } catch (RepositoryException e) {
            System.err.println(e.getMessage());
        }
    }
}
