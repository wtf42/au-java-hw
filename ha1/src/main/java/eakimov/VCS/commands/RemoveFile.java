package eakimov.VCS.commands;

import eakimov.VCS.VCSCommand;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.errors.StageException;
import eakimov.VCS.errors.UnexpectedIOException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Command(name = "rm", description = "Remove file from stage and working directory")
public class RemoveFile extends VCSCommand
{
    @Arguments(description = "Files to remove", required = true)
    public List<String> files;

    @Override
    protected void actualRun() throws RepositoryException
    {
        try {
            for (String file : files) {
                if (!state.isStageFile(file)) {
                    throw new StageException("file " + file + " not staged");
                }
                final Path stageFilePath = getStageFilePath(file);
                if (Files.exists(stageFilePath)) {
                    Files.delete(stageFilePath);
                }
                state.deleteStageFile(file);
                final Path workDirFilePath = Paths.get(repositoryWorkingDirectory, file);
                if (Files.exists(workDirFilePath)) {
                    Files.delete(workDirFilePath);
                }
            }
        } catch (IOException e) {
            throw new UnexpectedIOException(e);
        }
    }
}
