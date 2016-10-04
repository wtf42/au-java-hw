package eakimov.VCS;

import eakimov.VCS.errors.RecoverableRepositoryException;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.errors.UnrecoverableRepositoryException;

import java.io.IOException;
import java.nio.file.*;

public class VCSCommand implements Runnable
{
    protected final String repositoryWorkingDirectory = System.getProperty("user.dir");
    protected RepositoryState state;

    @Override
    public void run()
    {
        try {
            lockState();
            loadState();

            actualRun();

            saveState();
            unlockState();
        } catch (RecoverableRepositoryException e) {
            System.err.println(e.getMessage());
            unlockState();
        } catch (RepositoryException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            System.err.println("unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public RepositoryState getState() {
        return state;
    }

    public void loadState() throws RepositoryException {
        state = VCSFileUtils.loadState(Paths.get(repositoryWorkingDirectory,
                VCSDefaults.STATE_DIRECTORY,
                VCSDefaults.STATE_FILENAME));
    }

    protected void saveState() throws RepositoryException {
        VCSFileUtils.saveState(Paths.get(repositoryWorkingDirectory,
                VCSDefaults.STATE_DIRECTORY,
                VCSDefaults.STATE_FILENAME), state);
    }

    protected void lockState() throws RepositoryException {
        try {
            Files.createFile(Paths.get(repositoryWorkingDirectory,
                    VCSDefaults.STATE_DIRECTORY,
                    VCSDefaults.LOCK_FILENAME));
        } catch (IOException e) {
            throw new UnrecoverableRepositoryException("failed to lock repository state: "
                    + e.getMessage());
        }
    }

    protected void unlockState() {
        try {
            Files.delete(Paths.get(repositoryWorkingDirectory,
                    VCSDefaults.STATE_DIRECTORY,
                    VCSDefaults.LOCK_FILENAME));
        } catch (IOException e) {
            System.err.println("failed to unlock repository state: "
                    + e.getMessage());
        }
    }

    protected Path getRevisionPath(Revision revision) {
        String revisionDirectory = VCSDefaults.EMPTY_REVISION_DIRECTORY;
        if (revision != null) {
            revisionDirectory = revision.getRevisionDirectory();
        }
        return Paths.get(repositoryWorkingDirectory,
                VCSDefaults.STATE_DIRECTORY,
                revisionDirectory);
    }

    protected Path getStageFilePath(String fileName) {
        return Paths.get(repositoryWorkingDirectory,
                VCSDefaults.STATE_DIRECTORY,
                VCSDefaults.STAGE_DIRECTORY,
                fileName);
    }

    protected void actualRun() throws RepositoryException {}
}
