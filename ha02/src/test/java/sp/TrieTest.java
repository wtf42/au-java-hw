package sp;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TrieTest {

    @Test
    public void testSimple() {
        Trie trie = instance();

        assertTrue(trie.add("abc"));
        assertTrue(trie.contains("abc"));
        assertEquals(1, trie.size());
        assertEquals(1, trie.howManyStartsWithPrefix("abc"));
    }

    @Test
    public void testSimpleSerialization() throws IOException {
        Trie trie = instance();

        assertTrue(trie.add("abc"));
        assertTrue(trie.add("cde"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ((StreamSerializable) trie).serialize(outputStream);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Trie newTrie = instance();
        ((StreamSerializable) newTrie).deserialize(inputStream);

        assertTrue(newTrie.contains("abc"));
        assertTrue(newTrie.contains("cde"));
    }


    @Test(expected = IOException.class)
    public void testSimpleSerializationFails() throws IOException {
        Trie trie = instance();

        assertTrue(trie.add("abc"));
        assertTrue(trie.add("cde"));

        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Fail");
            }
        };

        ((StreamSerializable) trie).serialize(outputStream);
    }

    @Test
    public void testManyPrefixesSerialization() throws IOException {
        Trie trie = instance();

        assertTrue(trie.add("abc"));
        assertTrue(trie.add("cde"));
        final int prefixesCount = (Byte.MAX_VALUE - Byte.MIN_VALUE + 1) + 42;
        for (char c = 0; c < prefixesCount; ++c) {
            assertTrue(trie.add("z" + String.valueOf(c)));
        }
        assertEquals(prefixesCount, trie.howManyStartsWithPrefix("z"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ((StreamSerializable) trie).serialize(outputStream);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Trie newTrie = instance();
        ((StreamSerializable) newTrie).deserialize(inputStream);

        assertTrue(newTrie.contains("abc"));
        assertTrue(newTrie.contains("cde"));
        for (char c = 0; c < prefixesCount; ++c) {
            assertTrue(trie.contains("z" + String.valueOf(c)));
        }
        assertEquals(prefixesCount, trie.howManyStartsWithPrefix("z"));
    }

    public static Trie instance() {
        try {
            return (Trie) Class.forName("sp.TrieImpl").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
