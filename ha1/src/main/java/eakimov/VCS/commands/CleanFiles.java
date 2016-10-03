package eakimov.VCS.commands;

import eakimov.VCS.Revision;
import eakimov.VCS.VCSCommand;
import eakimov.VCS.VCSFileUtils;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.errors.UnexpectedIOException;
import io.airlift.airline.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@Command(name = "clean", description = "Remove untracked files")
public class CleanFiles extends VCSCommand
{
    @Override
    protected void actualRun() throws RepositoryException {
        final Set<String> knownFiles = new HashSet<>();
        final Revision currentRevision = state.getCurrentRevision();
        if (currentRevision != null) {
            knownFiles.addAll(currentRevision.getFiles());
        }
        knownFiles.addAll(state.getStageFiles());

        final Path workingDirectoryPath = Paths.get(repositoryWorkingDirectory);
        try {
            for (String file : VCSFileUtils.getWorkDirFiles(workingDirectoryPath)) {
                if (!knownFiles.contains(file)) {
                    Files.delete(workingDirectoryPath.resolve(file));
                    System.out.println(file + " deleted");
                }
            }
        } catch (IOException e) {
            throw new UnexpectedIOException(e);
        }
    }
}
