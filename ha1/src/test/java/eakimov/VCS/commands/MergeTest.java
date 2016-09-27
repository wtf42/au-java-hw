package eakimov.VCS.commands;

import eakimov.VCS.VCSCommandTestsBase;
import eakimov.VCS.errors.MergeException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class MergeTest extends VCSCommandTestsBase {
    @Test
    public void simpleMerge() throws Exception {
        final String branch1Name = "branch1";
        final String branch2Name = "branch2";
        final String commit1Message = "commit1";
        final String commit2Message = "commit2";
        final String commit3Message = "commit3";
        final String file1Name = "file1.txt";
        final String file2Name = "file2.txt";
        final List<String> file1Contents1 = Arrays.asList("file1 contents",
                "aaa", "bbb", "ccc", "ddd");
        final List<String> file1Contents2 = Arrays.asList("file1 contents",
                "aaa", "bbb", "ccc", "ddd", "eee");
        final List<String> file1Contents3 = Arrays.asList("file1 contents",
                "bbb", "xxx", "yyy", "ccc", "ddd");
        final List<String> fileMergedContents = Arrays.asList("file1 contents",
                "bbb", "xxx", "yyy", "ccc", "ddd", "eee");
        final List<String> file2Contents = Arrays.asList("file2 contents", "");

        final Path repositoryPath = newRepository();
        final Path file1Path = repositoryPath.resolve(file1Name);
        final Path file2Path = repositoryPath.resolve(file2Name);
        new Init().run();

        final NewBranch branch1 = new NewBranch();
        branch1.name = branch1Name;
        branch1.run();

        FileUtils.writeLines(file1Path.toFile(), file1Contents1);
        final Commit commit1 = new Commit();
        commit1.message = Collections.singletonList(commit1Message);
        commit1.run();

        FileUtils.writeLines(file1Path.toFile(), file1Contents2);
        FileUtils.writeLines(file2Path.toFile(), file2Contents);
        final Commit commit2 = new Commit();
        commit2.message = Collections.singletonList(commit2Message);
        commit2.run();

        final Checkout checkout1 = new Checkout();
        checkout1.revisionIdx = 1;
        checkout1.run();

        final CloneBranch branch2 = new CloneBranch();
        branch2.name = branch2Name;
        branch2.run();

        FileUtils.writeLines(file1Path.toFile(), file1Contents3);
        final Commit commit3 = new Commit();
        commit3.message = Collections.singletonList(commit3Message);
        commit3.run();

        final Checkout checkout2 = new Checkout();
        checkout2.branchName = branch1Name;
        checkout2.run();

        final Merge merge = new Merge();
        merge.fromBranch = branch2Name;
        merge.loadState();
        merge.actualRun();

        assertEquals(3, merge.getState().getCurrentRevision().getId());
        assertEquals(branch1Name, merge.getState().getCurrentBranch().getName());
        assertEquals(fileMergedContents, FileUtils.readLines(file1Path.toFile(), Charset.defaultCharset()));
        assertEquals(file2Contents, FileUtils.readLines(file2Path.toFile(), Charset.defaultCharset()));
    }

    @Test(expected = MergeException.class)
    public void mergeConflict() throws Exception {
        final String branch1Name = "branch1";
        final String branch2Name = "branch2";
        final String commit1Message = "commit1";
        final String commit2Message = "commit2";
        final String file1Name = "file1.txt";
        final String fileContents1 = "contents1";
        final String fileContents2 = "contents2";

        final Path repositoryPath = newRepository();
        final Path file1Path = repositoryPath.resolve(file1Name);
        new Init().run();

        final NewBranch branch1 = new NewBranch();
        branch1.name = branch1Name;
        branch1.run();

        FileUtils.writeStringToFile(file1Path.toFile(), fileContents1, Charset.defaultCharset());
        final Commit commit1 = new Commit();
        commit1.message = Collections.singletonList(commit1Message);
        commit1.run();

        final NewBranch branch2 = new NewBranch();
        branch2.name = branch2Name;
        branch2.run();

        FileUtils.writeStringToFile(file1Path.toFile(), fileContents2, Charset.defaultCharset());
        final Commit commit2 = new Commit();
        commit2.message = Collections.singletonList(commit2Message);
        commit2.run();

        final Merge merge = new Merge();
        merge.fromBranch = branch1Name;
        merge.loadState();
        merge.actualRun();
    }
}