package eakimov.VCS.commands;

import eakimov.VCS.VCSCommand;
import eakimov.VCS.VCSCommandTestsBase;
import eakimov.VCS.VCSDefaults;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class InitTest extends VCSCommandTestsBase {
    @Test
    public void initRepository() throws Exception {
        final Path repository = newRepository();
        final Path statePath = repository
                .resolve(Paths.get(VCSDefaults.STATE_DIRECTORY, VCSDefaults.STATE_FILENAME));
        final VCSCommand initCommand = new Init();

        assertFalse(statePath.toFile().exists());

        initCommand.run();

        assertTrue(statePath.toFile().exists());
        assertNotEquals(0, statePath.toFile().length());
    }
}
