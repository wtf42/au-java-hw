package sp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class TrieImpl implements Trie, StreamSerializable {
    private final TrieNode rootNode;

    public TrieImpl() {
        rootNode = new TrieNode();
    }

    @Override
    public boolean add(String element) {
        if (contains(element)) {
            return false;
        }
        TrieNode curNode = rootNode;
        for (char c : element.toCharArray()) {
            curNode = curNode.gotoChild(c);
            curNode.incPrefixCount();
        }
        curNode.setLeaf(true);
        rootNode.incPrefixCount();
        return true;
    }

    @Override
    public boolean contains(String element) {
        TrieNode curNode = rootNode;
        for (char c : element.toCharArray()) {
            if (!curNode.hasChild(c)) {
                return false;
            }
            curNode = curNode.gotoChild(c);
        }
        return curNode.isLeaf();
    }

    @Override
    public boolean remove(String element) {
        if (!contains(element)) {
            return false;
        }
        TrieNode curNode = rootNode;
        for (char c : element.toCharArray()) {
            TrieNode nextNode = curNode.gotoChild(c);
            nextNode.decPrefixCount();
            if (nextNode.getPrefixCount() == 0) {
                curNode.removeChild(c);
            }
            curNode = nextNode;
        }
        curNode.setLeaf(false);
        rootNode.decPrefixCount();
        return true;
    }

    @Override
    public int size() {
        return rootNode.getPrefixCount();
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        TrieNode curNode = rootNode;
        for (char c : prefix.toCharArray()) {
            if (!curNode.hasChild(c)) {
                return 0;
            }
            curNode = curNode.gotoChild(c);
        }
        return curNode.getPrefixCount();
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        rootNode.serialize(out);
    }

    @Override
    public void deserialize(InputStream in) throws IOException {
        rootNode.deserialize(in);
    }

    private static class TrieNode implements StreamSerializable {
        private boolean leaf;
        private int prefixCount;
        private final HashMap<Character, TrieNode> children;

        public TrieNode() {
            leaf = false;
            prefixCount = 0;
            children = new HashMap<>();
        }

        public boolean isLeaf() {
            return leaf;
        }
        public void setLeaf(boolean isLeaf) {
            leaf = isLeaf;
        }
        public int getPrefixCount() {
            return prefixCount;
        }
        public void incPrefixCount() {
            prefixCount++;
        }
        public void decPrefixCount() {
            prefixCount--;
        }
        public boolean hasChild(char c) {
            return children.containsKey(c);
        }
        public TrieNode gotoChild(char c) {
            if (!children.containsKey(c)) {
                children.put(c, new TrieNode());
            }
            return children.get(c);
        }
        public void removeChild(char c) {
            if (children.containsKey(c)) {
                children.remove(c);
            }
        }

        @Override
        public void serialize(OutputStream out) throws IOException {
            out.write(leaf ? 1 : 0);
            out.write(prefixCount);
            out.write(children.size());
            for (Map.Entry<Character,TrieNode> entry : children.entrySet()) {
                out.write(entry.getKey());
                entry.getValue().serialize(out);
            }
        }

        @Override
        public void deserialize(InputStream in) throws IOException {
            leaf = in.read() == 1;
            prefixCount = in.read();
            children.clear();
            int childCount = in.read();
            for (int i = 0; i < childCount; i++) {
                char key = (char)in.read();
                TrieNode node = new TrieNode();
                node.deserialize(in);
                children.put(key, node);
            }
        }
    }
}


