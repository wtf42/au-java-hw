package eakimov.ftp;

import eakimov.ftp.common.GetCommandAnswer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class GetCommandAnswerTest {
    @Rule
    public TemporaryFolder workDir = new TemporaryFolder();

    @Test
    public void fileTransfer() throws Exception {
        final String fileContents = "contents1";
        final File inputFile = workDir.newFile();
        final File outputFile = workDir.newFile();

        FileUtils.write(inputFile, fileContents, Charset.defaultCharset());

        final ByteArrayOutputStream srcStream = new ByteArrayOutputStream();

        final DataOutputStream dataOutputStream = new DataOutputStream(srcStream);
        new GetCommandAnswer(inputFile).writeTo(dataOutputStream);

        final DataInputStream dataInputStream = new DataInputStream(srcStream.toInputStream());
        new GetCommandAnswer(dataInputStream, outputFile);

        assertEquals(fileContents, FileUtils.readFileToString(outputFile, Charset.defaultCharset()));
    }

    @Test
    public void notExistingFileTransfer() throws Exception {
        final File inputFile = workDir.getRoot().toPath().resolve("wtf.txt").toFile();
        final File outputFile = workDir.newFile();

        final ByteArrayOutputStream srcStream = new ByteArrayOutputStream();

        final DataOutputStream dataOutputStream = new DataOutputStream(srcStream);
        new GetCommandAnswer(inputFile).writeTo(dataOutputStream);

        final DataInputStream dataInputStream = new DataInputStream(srcStream.toInputStream());
        new GetCommandAnswer(dataInputStream, outputFile);

        assertTrue(outputFile.exists());
        assertEquals(0, outputFile.length());
    }
}
