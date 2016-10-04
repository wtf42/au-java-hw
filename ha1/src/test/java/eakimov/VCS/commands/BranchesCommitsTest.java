package eakimov.VCS.commands;

import eakimov.VCS.VCSCommandTestsBase;
import eakimov.VCS.errors.BranchManagementException;
import org.junit.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class BranchesCommitsTest extends VCSCommandTestsBase {
    @Test
    public void branchesCommits() throws Exception {
        final String branchName = "branch1";
        final String commitMessage = "commit1";
        final String clonedBranchName = "branch2";
        final String fileName = "file1.txt";
        final Path repositoryPath = newRepository();

        new Init().run();

        final NewBranch newBranchCommand = new NewBranch();
        newBranchCommand.name = branchName;
        newBranchCommand.run();

        assertEquals(branchName, newBranchCommand.getState().getCurrentBranch().getName());

        assertTrue(repositoryPath.resolve(fileName).toFile().createNewFile());
        final AddFile addFile = new AddFile();
        addFile.files = Collections.singletonList(fileName);
        addFile.run();

        final Commit commit = new Commit();
        commit.message = Collections.singletonList(commitMessage);
        commit.run();

        assertEquals(commitMessage, commit.getState().getCurrentRevision().getCommitMessage());
        assertEquals(1, commit.getState().getCurrentRevision().getId());

        final CloneBranch cloneBranch = new CloneBranch();
        cloneBranch.name = clonedBranchName;
        cloneBranch.run();

        assertNotNull(cloneBranch.getState().findBranchByName(branchName));
        assertNotNull(cloneBranch.getState().findBranchByName(clonedBranchName));
        assertEquals(Arrays.asList(clonedBranchName, branchName),
                cloneBranch.getState().getAllBranchNames());

        assertEquals(clonedBranchName, cloneBranch.getState().getCurrentBranch().getName());
        assertEquals(commitMessage, cloneBranch.getState().getCurrentRevision().getCommitMessage());
        assertEquals(1, cloneBranch.getState().getCurrentRevision().getId());

        final DeleteBranch deleteBranch = new DeleteBranch();
        deleteBranch.name = branchName;
        deleteBranch.run();

        assertNull(deleteBranch.getState().findBranchByName(branchName));
        assertNotNull(deleteBranch.getState().findBranchByName(clonedBranchName));
        assertEquals(Collections.singletonList(clonedBranchName),
                deleteBranch.getState().getAllBranchNames());
    }

    @Test(expected = BranchManagementException.class)
    public void notExistingBranchDelete() throws Exception {
        newRepository();
        new Init().run();

        final DeleteBranch deleteBranch = new DeleteBranch();
        deleteBranch.name = "wtf";
        deleteBranch.loadState();
        deleteBranch.actualRun();
    }

    @Test(expected = BranchManagementException.class)
    public void notExistingBranchClone() throws Exception {
        newRepository();
        new Init().run();

        final CloneBranch cloneBranch = new CloneBranch();
        cloneBranch.name = "wtf";
        cloneBranch.loadState();
        cloneBranch.actualRun();
    }

    @Test(expected = BranchManagementException.class)
    public void alreadyExistingBranch() throws Exception {
        newRepository();
        new Init().run();

        final NewBranch newBranchCommand = new NewBranch();
        newBranchCommand.name = "branch1";
        newBranchCommand.loadState();
        newBranchCommand.actualRun();
        newBranchCommand.actualRun();
    }
}
