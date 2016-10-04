package eakimov.VCS;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;

public abstract class VCSCommandTestsBase {
    @Rule
    public final TemporaryFolder workDir = new TemporaryFolder();

    protected Path newRepository() throws IOException {
        final Path repositoryPath = workDir.newFolder().toPath();
        System.setProperty("user.dir", repositoryPath.toString());
        return repositoryPath;
    }
}
