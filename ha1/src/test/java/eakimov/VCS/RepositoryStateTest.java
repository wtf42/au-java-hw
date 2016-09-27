package eakimov.VCS;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class RepositoryStateTest {
    @Rule
    public final TemporaryFolder workDir = new TemporaryFolder();

    @Test
    public void manageBranches() throws Exception {
        final RepositoryState state = new RepositoryState();
        assertNull(state.getCurrentBranch());
        assertNull(state.getCurrentRevision());

        final Branch branch1 = new Branch("branch1", null);
        state.addBranch(branch1);
        final Revision rev1 = new Revision(branch1, null, "rev1");
        branch1.setHeadRevision(rev1);

        final Branch branch2 = new Branch("branch2", null);
        state.addBranch(branch2);
        final Revision rev2 = new Revision(branch2, null, "rev2");
        final Revision rev3 = new Revision(branch2, rev2, "rev3");
        branch2.setHeadRevision(rev3);

        state.setCurrentBranch(branch1);
        assertEquals(branch1, state.getCurrentBranch());
        assertEquals(rev1, state.getCurrentRevision());

        state.setCurrentBranch(branch2);
        state.setCurrentRevision(rev2);
        assertEquals(rev2, state.getCurrentRevision());

        assertEquals(branch1, state.findBranchByName("branch1"));
        assertEquals(branch2, state.findBranchByName("branch2"));
        assertNull(state.findBranchByName("wtf"));

        state.deleteBranch(branch2);
        assertNull(state.getCurrentBranch());
        assertNull(state.getCurrentRevision());
        assertNull(state.findBranchByName("branch2"));
    }

    @Test
    public void saveLoadRepositoryState() throws Exception {
        final String branchName = "branch1";
        final String revisionCommitText = "rev1";
        RepositoryState state = new RepositoryState();

        final Branch branch1 = new Branch(branchName, null);
        state.addBranch(branch1);
        final Revision rev1 = new Revision(branch1, null, revisionCommitText);
        branch1.setHeadRevision(rev1);
        state.setCurrentBranch(branch1);
        state.setCurrentRevision(rev1);

        assertEquals(branchName, state.getCurrentBranch().getName());
        assertEquals(revisionCommitText, state.getCurrentRevision().getCommitMessage());
        assertEquals(1, state.getCurrentRevision().getId());

        final File stateFile = workDir.newFile();
        final Path stateFilePath = Paths.get(stateFile.toURI());
        VCSFileUtils.saveState(stateFilePath, state);
        final RepositoryState loadedState = VCSFileUtils.loadState(stateFilePath);

        assertEquals(branchName, loadedState.getCurrentBranch().getName());
        assertEquals(revisionCommitText, loadedState.getCurrentRevision().getCommitMessage());
        assertEquals(1, loadedState.getCurrentRevision().getId());
    }
}