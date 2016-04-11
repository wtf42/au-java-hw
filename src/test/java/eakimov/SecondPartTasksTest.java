package eakimov;

import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class SecondPartTasksTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testFindQuotes() throws IOException {
        String path1 = folder.newFile("file1.txt").getPath();
        java.nio.file.Files.write(Paths.get(path1), "line1\nline2\nsubstring1".getBytes());
        String path2 = folder.newFile("file2.txt").getPath();
        java.nio.file.Files.write(Paths.get(path2), "substring2\nline2".getBytes());
        assertEquals(
                Arrays.asList("line1", "line2", "line2"),
                SecondPartTasks.findQuotes(Arrays.asList(path1, path2), "line"));
        assertEquals(
                Arrays.asList("substring1", "substring2"),
                SecondPartTasks.findQuotes(Arrays.asList(path1, path2), "str"));
        assertEquals(
                Arrays.asList("line1", "line2", "substring1", "substring2", "line2"),
                SecondPartTasks.findQuotes(Arrays.asList(path1, path2), ""));
        assertEquals(
                Collections.emptyList(),
                SecondPartTasks.findQuotes(Collections.emptyList(), "wtf"));
    }

    @Test
    public void testPiDividedBy4() {
        assertEquals(Math.PI / 4, SecondPartTasks.piDividedBy4(), 0.01);
    }

    @Test
    public void testFindPrinter() {
        final String author1 = "author1",
                author2 = "author2",
                author3 = "author3",
                author4 = "author4";
        assertEquals(
                author2,
                SecondPartTasks.findPrinter(
                        ImmutableMap.of(
                                author1, Arrays.asList("len4", "len_5"),
                                author2, Arrays.asList("len___7", "len_____9", "len4"),
                                author3, Arrays.asList("1", "l2"),
                                author4, Collections.emptyList()
                        )));
        String test2Result = SecondPartTasks.findPrinter(
                ImmutableMap.of(
                        author1, Arrays.asList("len4", "len_5"),
                        author2, Arrays.asList("len___7", "l2"),
                        author3, Arrays.asList("1", "l2")
                ));
        //noinspection StringEquality
        assertTrue(test2Result == author1 || test2Result == author2);
    }

    @Test
    public void testCalculateGlobalOrder() {
        assertEquals(
                ImmutableMap.of(
                        "item1", 17,
                        "item2", 34,
                        "item3", 68,
                        "item4", 136
                ),
                SecondPartTasks.calculateGlobalOrder(Arrays.asList(
                        ImmutableMap.of(
                                "item1", 1,
                                "item2", 2,
                                "item3", 4,
                                "item4", 8
                        ),
                        Collections.emptyMap(),
                        ImmutableMap.of(
                                "item1", 16,
                                "item2", 32,
                                "item3", 64,
                                "item4", 128
                        )
                )));
        assertEquals(
                ImmutableMap.of(
                        "item1", 1,
                        "item2", 2,
                        "item3", 3,
                        "item4", 4
                ),
                SecondPartTasks.calculateGlobalOrder(Arrays.asList(
                        ImmutableMap.of(
                                "item1", 1,
                                "item2", 2
                        ),
                        ImmutableMap.of(
                                "item3", 3
                        ),
                        ImmutableMap.of(
                                "item4", 4
                        ),
                        ImmutableMap.of(
                                "item5", 0
                        )
                )));
        assertEquals(
                Collections.emptyMap(),
                SecondPartTasks.calculateGlobalOrder(Collections.emptyList()));
    }
}