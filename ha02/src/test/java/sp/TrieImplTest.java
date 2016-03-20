package sp;

import org.junit.Test;

import static org.junit.Assert.*;

public class TrieImplTest {
    private String[] words = new String[] {"he", "she", "his", "hers"};

    private Trie genWordsTrie() {
        Trie trie = new TrieImpl();
        for (String s : words) {
            assertTrue(trie.add(s));
        }
        return trie;
    }

    @Test
    public void testAdd() throws Exception {
        Trie trie = new TrieImpl();
        assertFalse(trie.contains("he"));
        assertTrue(trie.add("he"));
        assertTrue(trie.contains("he"));
        assertFalse(trie.contains("h"));
        assertFalse(trie.contains("her"));
        assertEquals(1, trie.size());
        assertFalse(trie.contains("his"));
        assertTrue(trie.add("his"));
        assertTrue(trie.contains("his"));
        assertFalse(trie.contains("hi"));
        assertFalse(trie.contains("his1"));
        assertFalse(trie.add("his"));
        assertEquals(2, trie.size());
    }

    @Test
    public void testContains() throws Exception {
        Trie trie = genWordsTrie();
        for (String s : words) {
            assertTrue(trie.contains(s));
        }
        assertFalse(trie.contains("wtf"));
        assertFalse(trie.contains("her"));
    }

    @Test
    public void testRemove() throws Exception {
        Trie trie = genWordsTrie();
        int size = words.length;

        assertFalse(trie.remove("wtf"));
        for (String s : words) {
            assertEquals(size, trie.size());
            assertTrue(trie.remove(s));
            size--;
        }
        assertEquals(0, trie.size());
        for (String s : words) {
            assertFalse(trie.contains(s));
        }
    }

    @Test
    public void testSize() throws Exception {
        assertEquals(0, new TrieImpl().size());
        assertEquals(4, genWordsTrie().size());
    }

    @Test
    public void testHowManyStartsWithPrefix() throws Exception {
        Trie trie = genWordsTrie();
        assertEquals(3, trie.howManyStartsWithPrefix("h"));
        assertEquals(2, trie.howManyStartsWithPrefix("he"));
        assertEquals(1, trie.howManyStartsWithPrefix("her"));
        assertEquals(4, trie.howManyStartsWithPrefix(""));
        assertTrue(trie.remove("hers"));
        assertEquals(2, trie.howManyStartsWithPrefix("h"));
        assertEquals(0, trie.howManyStartsWithPrefix("her"));
    }
}
