package eakimov.VCS;

import eakimov.VCS.errors.RecoverableRepositoryException;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.errors.UnrecoverableRepositoryException;

import java.io.IOException;
import java.nio.file.*;

public class VCSCommand implements Runnable
{
    protected final String repositoryRootPath = System.getProperty("user.dir");
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
        state = VCSFileUtils.loadState(Paths.get(repositoryRootPath,
                VCSDefaults.STATE_DIRECTORY,
                VCSDefaults.STATE_FILENAME));
    }

    protected void saveState() throws RepositoryException {
        VCSFileUtils.saveState(Paths.get(repositoryRootPath,
                VCSDefaults.STATE_DIRECTORY,
                VCSDefaults.STATE_FILENAME), state);
    }

    protected void lockState() throws RepositoryException {
        try {
            Files.createFile(Paths.get(repositoryRootPath,
                    VCSDefaults.STATE_DIRECTORY,
                    VCSDefaults.LOCK_FILENAME));
        } catch (IOException e) {
            throw new UnrecoverableRepositoryException("failed to lock repository state: "
                    + e.getMessage());
        }
    }

    protected void unlockState() {
        try {
            Files.delete(Paths.get(repositoryRootPath,
                    VCSDefaults.STATE_DIRECTORY,
                    VCSDefaults.LOCK_FILENAME));
        } catch (IOException e) {
            System.err.println("failed to unlock repository state: "
                    + e.getMessage());
        }
    }

    protected Path getRevisionPath(Revision revision) {
        String revisionDirectoryName = VCSDefaults.EMPTY_REVISION_DIRECTORY;
        if (revision != null) {
            revisionDirectoryName = revision.getDirectory();
        }
        return Paths.get(repositoryRootPath,
                VCSDefaults.STATE_DIRECTORY,
                revisionDirectoryName);
    }

    protected void actualRun() throws RepositoryException {}
}
