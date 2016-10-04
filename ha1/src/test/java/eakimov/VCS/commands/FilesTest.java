package eakimov.VCS.commands;

import eakimov.VCS.Revision;
import eakimov.VCS.VCSCommandTestsBase;
import eakimov.VCS.VCSDefaults;
import eakimov.VCS.errors.StageException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.Assert.*;

public class FilesTest extends VCSCommandTestsBase {
    @Test
    public void addCommitCommands() throws Exception {
        final String branchName = "branch1";
        final String commitMessage1 = "commit1";
        final String commitMessage2 = "commit2";
        final String fileName1 = "file1.txt";
        final String fileName2 = "file2.txt";
        final String fileContents1 = "contents1";
        final String fileContents2 = "contents2";

        final Path repositoryPath = newRepository();
        final Path filePath1 = repositoryPath.resolve(fileName1);
        final Path filePath2 = repositoryPath.resolve(fileName2);
        final Path stagePath = repositoryPath
                .resolve(VCSDefaults.STATE_DIRECTORY)
                .resolve(VCSDefaults.STAGE_DIRECTORY);
        final Path stageFilePath1 = stagePath.resolve(fileName1);
        final Path stageFilePath2 = stagePath.resolve(fileName2);

        new Init().run();

        final NewBranch newBranchCommand = new NewBranch();
        newBranchCommand.name = branchName;
        newBranchCommand.run();

        FileUtils.writeStringToFile(filePath1.toFile(), fileContents1, Charset.defaultCharset());

        final AddFile addFile1 = new AddFile();
        addFile1.files = Collections.singletonList(fileName1);
        addFile1.run();

        assertEquals(fileContents1, FileUtils.readFileToString(stageFilePath1.toFile(), Charset.defaultCharset()));

        final Commit commit1 = new Commit();
        commit1.message = Collections.singletonList(commitMessage1);
        commit1.run();

        assertFalse(Files.exists(stageFilePath1));

        final Revision commit1Revision = commit1.getState().getCurrentRevision();
        assertEquals(commit1Revision, commit1Revision.getFileRevision(fileName1));

        final Path revisionFile1Path = repositoryPath.resolve(VCSDefaults.STATE_DIRECTORY)
                .resolve(commit1Revision.getRevisionDirectory())
                .resolve(fileName1);
        assertEquals(fileContents1, FileUtils.readFileToString(revisionFile1Path.toFile(), Charset.defaultCharset()));

        FileUtils.writeStringToFile(filePath2.toFile(), fileContents2, Charset.defaultCharset());

        final AddFile addFile2 = new AddFile();
        addFile2.files = Collections.singletonList(fileName2);
        addFile2.run();

        assertEquals(fileContents2, FileUtils.readFileToString(stageFilePath2.toFile(), Charset.defaultCharset()));

        final Commit commit2 = new Commit();
        commit2.message = Collections.singletonList(commitMessage2);
        commit2.run();

        final Revision commit2Revision = commit2.getState().getCurrentRevision();
        assertEquals(1, commit2Revision.getFileRevision(fileName1).getId());
        assertEquals(2, commit2Revision.getFileRevision(fileName2).getId());

        final Path revisionFile2Path = repositoryPath.resolve(VCSDefaults.STATE_DIRECTORY)
                .resolve(commit2Revision.getFileRevision(fileName2).getRevisionDirectory())
                .resolve(fileName2);
        assertEquals(fileContents2, FileUtils.readFileToString(revisionFile2Path.toFile(), Charset.defaultCharset()));
    }

    @Test
    public void resetCommand() throws Exception {
        final String branchName = "branch1";
        final String fileName1 = "file1.txt";
        final String fileContents1 = "contents1";

        final Path repositoryPath = newRepository();
        final Path filePath1 = repositoryPath.resolve(fileName1);
        final Path stagePath = repositoryPath
                .resolve(VCSDefaults.STATE_DIRECTORY)
                .resolve(VCSDefaults.STAGE_DIRECTORY);
        final Path stageFilePath1 = stagePath.resolve(fileName1);

        new Init().run();

        final NewBranch newBranchCommand = new NewBranch();
        newBranchCommand.name = branchName;
        newBranchCommand.run();

        FileUtils.writeStringToFile(filePath1.toFile(), fileContents1, Charset.defaultCharset());

        final AddFile addFile1 = new AddFile();
        addFile1.files = Collections.singletonList(fileName1);
        addFile1.run();

        assertEquals(fileContents1, FileUtils.readFileToString(stageFilePath1.toFile(), Charset.defaultCharset()));
        assertEquals(fileContents1, FileUtils.readFileToString(filePath1.toFile(), Charset.defaultCharset()));
        assertEquals(Collections.singleton(fileName1), addFile1.getState().getStageFiles());

        final ResetFile resetFile = new ResetFile();
        resetFile.files = Collections.singletonList(fileName1);
        resetFile.run();

        assertFalse(Files.exists(stageFilePath1));
        assertEquals(fileContents1, FileUtils.readFileToString(filePath1.toFile(), Charset.defaultCharset()));
        assertTrue(resetFile.getState().getStageFiles().isEmpty());
    }

    @Test
    public void removeCommand() throws Exception {
        final String branchName = "branch1";
        final String fileName1 = "file1.txt";
        final String fileContents1 = "contents1";

        final Path repositoryPath = newRepository();
        final Path filePath1 = repositoryPath.resolve(fileName1);
        final Path stagePath = repositoryPath
                .resolve(VCSDefaults.STATE_DIRECTORY)
                .resolve(VCSDefaults.STAGE_DIRECTORY);
        final Path stageFilePath1 = stagePath.resolve(fileName1);

        new Init().run();

        final NewBranch newBranchCommand = new NewBranch();
        newBranchCommand.name = branchName;
        newBranchCommand.run();

        FileUtils.writeStringToFile(filePath1.toFile(), fileContents1, Charset.defaultCharset());

        final AddFile addFile1 = new AddFile();
        addFile1.files = Collections.singletonList(fileName1);
        addFile1.run();

        assertEquals(fileContents1, FileUtils.readFileToString(stageFilePath1.toFile(), Charset.defaultCharset()));
        assertEquals(fileContents1, FileUtils.readFileToString(filePath1.toFile(), Charset.defaultCharset()));
        assertEquals(Collections.singleton(fileName1), addFile1.getState().getStageFiles());

        final RemoveFile removeFile = new RemoveFile();
        removeFile.files = Collections.singletonList(fileName1);
        removeFile.run();

        assertFalse(Files.exists(stageFilePath1));
        assertFalse(Files.exists(filePath1));
        assertTrue(removeFile.getState().getStageFiles().isEmpty());
    }

    @Test
    public void cleanCommand() throws Exception {
        final String branchName = "branch1";
        final String commitMessage1 = "commit1";
        final String fileName1 = "file1.txt";
        final String fileName2 = "file2.txt";
        final String fileName3 = "file3.txt";
        final String fileContents1 = "contents1";
        final String fileContents2 = "contents2";
        final String fileContents3 = "contents3";

        final Path repositoryPath = newRepository();
        final Path filePath1 = repositoryPath.resolve(fileName1);
        final Path filePath2 = repositoryPath.resolve(fileName2);
        final Path filePath3 = repositoryPath.resolve(fileName3);
        final Path stagePath = repositoryPath
                .resolve(VCSDefaults.STATE_DIRECTORY)
                .resolve(VCSDefaults.STAGE_DIRECTORY);
        final Path stageFilePath1 = stagePath.resolve(fileName1);
        final Path stageFilePath2 = stagePath.resolve(fileName2);

        new Init().run();

        final NewBranch newBranchCommand = new NewBranch();
        newBranchCommand.name = branchName;
        newBranchCommand.run();

        FileUtils.writeStringToFile(filePath1.toFile(), fileContents1, Charset.defaultCharset());

        final AddFile addFile1 = new AddFile();
        addFile1.files = Collections.singletonList(fileName1);
        addFile1.run();

        assertEquals(fileContents1, FileUtils.readFileToString(stageFilePath1.toFile(), Charset.defaultCharset()));

        final Commit commit1 = new Commit();
        commit1.message = Collections.singletonList(commitMessage1);
        commit1.run();

        assertFalse(Files.exists(stageFilePath1));

        FileUtils.writeStringToFile(filePath2.toFile(), fileContents2, Charset.defaultCharset());

        final AddFile addFile2 = new AddFile();
        addFile2.files = Collections.singletonList(fileName2);
        addFile2.run();

        FileUtils.writeStringToFile(filePath3.toFile(), fileContents3, Charset.defaultCharset());

        assertEquals(fileContents1, FileUtils.readFileToString(filePath1.toFile(), Charset.defaultCharset()));
        assertEquals(fileContents2, FileUtils.readFileToString(filePath2.toFile(), Charset.defaultCharset()));
        assertEquals(fileContents2, FileUtils.readFileToString(stageFilePath2.toFile(), Charset.defaultCharset()));
        assertEquals(fileContents3, FileUtils.readFileToString(filePath3.toFile(), Charset.defaultCharset()));

        final CleanFiles cleanFiles = new CleanFiles();
        cleanFiles.run();

        assertEquals(fileContents1, FileUtils.readFileToString(filePath1.toFile(), Charset.defaultCharset()));
        assertEquals(fileContents2, FileUtils.readFileToString(filePath2.toFile(), Charset.defaultCharset()));
        assertEquals(fileContents2, FileUtils.readFileToString(stageFilePath2.toFile(), Charset.defaultCharset()));
        assertFalse(Files.exists(filePath3));
    }

    @Test(expected = StageException.class)
    public void resetFail() throws Exception {
        final String branchName = "branch1";
        final String fileName = "file1.txt";
        final String unstagedFileName = "file2.txt";
        final String fileContents1 = "contents1";
        final String fileContents2 = "contents2";

        final Path repositoryPath = newRepository();
        final Path filePath = repositoryPath.resolve(fileName);
        final Path unstagedFilePath = repositoryPath.resolve(unstagedFileName);
        final Path stagePath = repositoryPath
                .resolve(VCSDefaults.STATE_DIRECTORY)
                .resolve(VCSDefaults.STAGE_DIRECTORY);
        final Path stageFilePath = stagePath.resolve(fileName);
        final Path unstagedStageFilePath = stagePath.resolve(unstagedFileName);

        new Init().run();

        final NewBranch newBranchCommand = new NewBranch();
        newBranchCommand.name = branchName;
        newBranchCommand.run();

        FileUtils.writeStringToFile(filePath.toFile(), fileContents1, Charset.defaultCharset());
        FileUtils.writeStringToFile(unstagedFilePath.toFile(), fileContents2, Charset.defaultCharset());

        final AddFile addFile1 = new AddFile();
        addFile1.files = Collections.singletonList(fileName);
        addFile1.run();

        assertEquals(fileContents1, FileUtils.readFileToString(stageFilePath.toFile(), Charset.defaultCharset()));
        assertEquals(fileContents1, FileUtils.readFileToString(filePath.toFile(), Charset.defaultCharset()));
        assertEquals(Collections.singleton(fileName), addFile1.getState().getStageFiles());
        assertEquals(fileContents2, FileUtils.readFileToString(unstagedFilePath.toFile(), Charset.defaultCharset()));
        assertFalse(Files.exists(unstagedStageFilePath));

        final ResetFile resetFile = new ResetFile();
        resetFile.files = Collections.singletonList(unstagedFileName);
        resetFile.loadState();
        resetFile.actualRun();
    }
}
