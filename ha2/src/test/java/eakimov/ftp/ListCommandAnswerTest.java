package eakimov.ftp;

import eakimov.ftp.common.ListCommandAnswer;
import eakimov.ftp.common.ListCommandAnswer.DirectoryContent;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class ListCommandAnswerTest {
    @Rule
    public TemporaryFolder workDir = new TemporaryFolder();

    @Test
    public void dirContentsTransfer() throws Exception {
        final List<String> files = Arrays.asList("file1.txt", "file2.txt", "file3.txt");
        final List<String> dirs = Arrays.asList("dir1", "dir2");
        final List<DirectoryContent> contents =
                Stream.concat(
                        files.stream().map(f -> new DirectoryContent(f, false)),
                        dirs.stream().map(d -> new DirectoryContent(d, true))
                ).collect(Collectors.toList());

        final ByteArrayOutputStream srcStream = new ByteArrayOutputStream();

        final DataOutputStream dataOutputStream = new DataOutputStream(srcStream);
        new ListCommandAnswer(contents).writeTo(dataOutputStream);

        final DataInputStream dataInputStream = new DataInputStream(srcStream.toInputStream());
        final ListCommandAnswer answer = new ListCommandAnswer(dataInputStream);

        assertEquals(new HashSet<>(contents), new HashSet<>(answer.getContents()));
    }
}
