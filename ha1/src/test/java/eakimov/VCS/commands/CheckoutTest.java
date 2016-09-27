package eakimov.VCS.commands;

import eakimov.VCS.VCSCommandTestsBase;
import eakimov.VCS.errors.RepositoryException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.Assert.*;

public class CheckoutTest extends VCSCommandTestsBase {
    @Test
    public void checkoutCommits() throws Exception {
        final String branch1Name = "branch1";
        final String branch2Name = "branch2";
        final String commit1Message = "commit1";
        final String commit2Message = "commit2";
        final String commit3Message = "commit3";
        final String fileName = "file.txt";
        final String fileContents1 = "file contents1";
        final String fileContents2 = "file contents2";
        final String fileContents3 = "file contents3";

        final Path repositoryPath = newRepository();
        final Path filePath = repositoryPath.resolve(fileName);

        new Init().run();

        final NewBranch branch1 = new NewBranch();
        branch1.name = branch1Name;
        branch1.run();

        FileUtils.writeStringToFile(filePath.toFile(), fileContents1, Charset.defaultCharset());
        final Commit commit1 = new Commit();
        commit1.message = Collections.singletonList(commit1Message);
        commit1.run();

        FileUtils.writeStringToFile(filePath.toFile(), fileContents2, Charset.defaultCharset());
        final Commit commit2 = new Commit();
        commit2.message = Collections.singletonList(commit2Message);
        commit2.run();

        assertEquals(fileContents2, FileUtils.readFileToString(filePath.toFile(), Charset.defaultCharset()));

        final Checkout checkout1 = new Checkout();
        checkout1.revisionIdx = 1;
        checkout1.run();

        assertEquals(fileContents1, FileUtils.readFileToString(filePath.toFile(), Charset.defaultCharset()));

        final CloneBranch branch2 = new CloneBranch();
        branch2.name = branch2Name;
        branch2.run();

        FileUtils.writeStringToFile(filePath.toFile(), fileContents3, Charset.defaultCharset());

        final Commit commit3 = new Commit();
        commit3.message = Collections.singletonList(commit3Message);
        commit3.run();

        final Checkout checkout2 = new Checkout();
        checkout2.branchName = branch1Name;
        checkout2.run();

        assertEquals(2, checkout2.getState().getCurrentRevision().getId());
        assertEquals(fileContents2, FileUtils.readFileToString(filePath.toFile(), Charset.defaultCharset()));

        final Checkout checkout3 = new Checkout();
        checkout3.branchName = branch2Name;
        checkout3.run();

        assertEquals(1, checkout3.getState().getCurrentRevision().getId());
        assertEquals(fileContents3, FileUtils.readFileToString(filePath.toFile(), Charset.defaultCharset()));
    }

    @Test(expected = RepositoryException.class)
    public void notExistingBranch() throws Exception {
        newRepository();
        new Init().run();

        final Checkout checkout = new Checkout();
        checkout.branchName = "wtf";
        checkout.loadState();
        checkout.actualRun();
    }

    @Test(expected = RepositoryException.class)
    public void notExistingRevision() throws Exception {
        newRepository();
        new Init().run();

        final NewBranch branch1 = new NewBranch();
        branch1.name = "branch1";
        branch1.run();

        final Checkout checkout = new Checkout();
        checkout.revisionIdx = 42;
        checkout.loadState();
        checkout.actualRun();
    }
}