package eakimov.VCS;

import eakimov.VCS.errors.RepositoryException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class VCSCommandTest extends VCSCommandTestsBase {

    @Test(expected = RepositoryException.class)
    public void emptyRepository() throws Exception {
        newRepository();
        final VCSCommand command = new VCSCommand();
        command.loadState();
    }

    @Test
    public void saveLoadRepository() throws Exception {
        newRepository();
        final VCSCommand command = new VCSCommand();

        final RepositoryState state = new RepositoryState();
        final Branch branch = new Branch("branch1", null);
        state.addBranch(branch);
        final Revision revision = new Revision(branch, null, "revision1", new HashMap<>());
        state.setCurrentBranch(branch);
        state.setCurrentRevision(revision);

        VCSFileUtils.initVCSFiles(command.repositoryWorkingDirectory);
        command.state = state;
        command.saveState();
        command.loadState();

        assertNotEquals(state, command.state);
        assertEquals("branch1", command.state.getCurrentBranch().getName());
        assertEquals("revision1", command.state.getCurrentRevision().getCommitMessage());
    }

    @Test(expected = RepositoryException.class)
    public void lockPass() throws Exception {
        newRepository();
        final VCSCommand command = new VCSCommand();
        command.lockState();
        command.unlockState();
        command.lockState();
    }

    @Test(expected = RepositoryException.class)
    public void lockFail() throws Exception {
        newRepository();
        final VCSCommand command = new VCSCommand();
        command.lockState();
        command.lockState();
    }
}