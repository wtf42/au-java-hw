package eakimov.VCS.commands;

import eakimov.VCS.VCSCommand;
import eakimov.VCS.errors.RepositoryException;
import eakimov.VCS.errors.StageException;
import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Command(name = "add", description = "Stage files")
public class AddFile extends VCSCommand
{
    @Arguments(description = "Files to add", required = true)
    public List<String> files;

    @Override
    protected void actualRun() throws RepositoryException
    {
        try {
            for (String file : files) {
                final Path workDirFilePath = Paths.get(repositoryWorkingDirectory, file);
                final Path stageFilePath = getStageFilePath(file);
                if (Files.isDirectory(workDirFilePath)) {
                    throw new StageException(file + " is a directory");
                }
                if (Files.notExists(workDirFilePath)) {
                    if (Files.exists(stageFilePath)) {
                        Files.delete(stageFilePath);
                    }
                } else {
                    FileUtils.copyFile(workDirFilePath.toFile(), stageFilePath.toFile());
                }
                state.addStageFile(file);
            }
        } catch (IOException e) {
            throw new StageException("failed to stage file: " + e.getMessage());
        }
    }
}
