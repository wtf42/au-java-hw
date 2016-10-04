package eakimov.VCS.commands;

import eakimov.VCS.VCSCommand;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.errors.StageException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Command(name = "reset", description = "Unstage files")
public class ResetFile extends VCSCommand
{
    @Arguments(description = "Files to reset", required = true)
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
            }
        } catch (IOException e) {
            throw new StageException("failed to reset files: " + e.getMessage());
        }
    }
}
