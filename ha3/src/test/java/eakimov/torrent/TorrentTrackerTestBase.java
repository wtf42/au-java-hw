package eakimov.torrent;

import eakimov.torrent.common.TorrentTracker;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.time.Duration;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TorrentTracker.class)
public abstract class TorrentTrackerTestBase {
    private static final Duration updateExpiration = Duration.ofMillis(200);

    @Rule
    public TemporaryFolder workDir = new TemporaryFolder();

    @BeforeClass
    public static void setExpiration() {
        Whitebox.setInternalState(TorrentTracker.class,
                "FILE_UPDATE_EXPIRATION",
                updateExpiration);
        Whitebox.setInternalState(TorrentTracker.class,
                "CLIENT_UPDATE_INTERVAL",
                updateExpiration.toMillis() / 2);
    }
}
