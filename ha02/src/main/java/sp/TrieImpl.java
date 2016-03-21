package sp;

import java.io.*;
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
        rootNode.serialize(new DataOutputStream(out));
    }

    @Override
    public void deserialize(InputStream in) throws IOException {
        rootNode.deserialize(new DataInputStream(in));
    }

    private static class TrieNode {
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

        public void serialize(DataOutputStream out) throws IOException {
            out.writeBoolean(leaf);
            out.writeInt(prefixCount);
            out.writeInt(children.size());
            for (Map.Entry<Character,TrieNode> entry : children.entrySet()) {
                out.writeChar(entry.getKey());
                entry.getValue().serialize(out);
            }
        }

        public void deserialize(DataInputStream in) throws IOException {
            leaf = in.readBoolean();
            prefixCount = in.readInt();
            children.clear();
            int childCount = in.readInt();
            for (int i = 0; i < childCount; i++) {
                char key = in.readChar();
                TrieNode node = new TrieNode();
                node.deserialize(in);
                children.put(key, node);
            }
        }
    }
}


