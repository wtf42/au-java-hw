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
        assertTrue(!trie.contains("he"));
        assertTrue(trie.add("he"));
        assertTrue(trie.contains("he"));
        assertTrue(!trie.contains("his"));
        assertTrue(trie.add("his"));
        assertTrue(trie.contains("his"));
        assertTrue(!trie.add("his"));
    }

    @Test
    public void testContains() throws Exception {
        Trie trie = genWordsTrie();
        for (String s : words) {
            assertTrue(trie.contains(s));
        }
        assertTrue(!trie.contains("wtf"));
        assertTrue(!trie.contains("her"));
    }

    @Test
    public void testRemove() throws Exception {
        Trie trie = genWordsTrie();
        assertTrue(!trie.remove("wtf"));
        for (String s : words) {
            assertTrue(trie.remove(s));
        }
        for (String s : words) {
            assertTrue(!trie.contains(s));
        }
        assertEquals(trie.size(), 0);
    }

    @Test
    public void testSize() throws Exception {
        assertEquals(new TrieImpl().size(), 0);
        assertEquals(genWordsTrie().size(), 4);
    }

    @Test
    public void testHowManyStartsWithPrefix() throws Exception {
        Trie trie = genWordsTrie();
        assertEquals(trie.howManyStartsWithPrefix("h"), 3);
        assertEquals(trie.howManyStartsWithPrefix("he"), 2);
        assertEquals(trie.howManyStartsWithPrefix("her"), 1);
        assertEquals(trie.howManyStartsWithPrefix(""), 4);
        assertTrue(trie.remove("hers"));
        assertEquals(trie.howManyStartsWithPrefix("h"), 2);
        assertEquals(trie.howManyStartsWithPrefix("her"), 0);
    }
}
