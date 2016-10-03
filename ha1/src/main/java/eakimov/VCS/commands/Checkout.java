package eakimov.VCS.commands;

import eakimov.VCS.*;
import eakimov.VCS.errors.BranchManagementException;
import eakimov.VCS.errors.RecoverableRepositoryException;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.errors.UnexpectedIOException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

@Command(name = "checkout", description = "Checkout files")
public class Checkout extends VCSCommand
{
    @Option(name = "-rev", description = "Revision", required = false)
    public int revisionIdx = -1;
    @Option(name = "-branch", description = "Branch name", required = false)
    public String branchName;
    @Arguments(description = "Files to checkout", required = false)
    public List<String> files;

    @Override
    protected void actualRun() throws RepositoryException
    {
        Branch branch;
        if (branchName == null) {
            branch = state.getCurrentBranch();
            if (branch == null) {
                throw new BranchManagementException("branch name should be specified, current branch is not set");
            }
        } else {
            branch = state.findBranchByName(branchName);
            if (branch == null) {
                throw new BranchManagementException(branchName, "not found");
            }
        }

        Revision revision = branch.getHeadRevision();
        if (revisionIdx != -1) {
            while (revision != null && revision.getId() != revisionIdx) {
                revision = revision.getParent();
            }
            if (revision == null || revision.getId() != revisionIdx) {
                throw new RecoverableRepositoryException("revision " + Integer.toString(revisionIdx) + " not found");
            }
        }

        final Path workingDirectoryPath = Paths.get(repositoryWorkingDirectory);
        if (files == null) {
            VCSFileUtils.cleanUpDirectory(workingDirectoryPath);
        }
        if (revision == null) {
            if (files != null && !files.isEmpty()) {
                throw new RecoverableRepositoryException("files not found in revision");
            }
        } else {
            final Collection<String> filesToCopy = files == null ? revision.getFiles() : files;
            try {
                for (String file : filesToCopy) {
                    final Path srcPath = VCSFileUtils.getRevisionFilePath(repositoryWorkingDirectory, revision, file);
                    final Path destPath = workingDirectoryPath.resolve(file);
                    FileUtils.copyFile(srcPath.toFile(), destPath.toFile());
                }
            } catch (IOException e) {
                throw new UnexpectedIOException(e);
            }
        }

        state.setCurrentBranch(branch);
        state.setCurrentRevision(revision);
    }
}
