import java.util.HashMap;

public class TrieImpl implements Trie {
    private class TrieNode {
        private boolean leaf;
        private int count;
        private HashMap<Character, TrieNode> children;

        public TrieNode() {
            leaf = false;
            count = 0;
            children = new HashMap<>();
        }

        public boolean isLeaf() {
            return leaf;
        }
        public void setLeaf(boolean isLeaf) {
            leaf = isLeaf;
        }
        public int getCount() {
            return count;
        }
        public void incCount() {
            count++;
        }
        public void decCount() {
            count--;
        }
        public boolean hasChildren(char c) {
            return children.containsKey(c);
        }
        public TrieNode go(char c) {
            if (!children.containsKey(c)) {
                children.put(c, new TrieNode());
            }
            return children.get(c);
        }
    }

    private TrieNode rootNode;

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
            curNode = curNode.go(c);
            curNode.incCount();
        }
        curNode.setLeaf(true);
        rootNode.incCount();
        return true;
    }

    @Override
    public boolean contains(String element) {
        TrieNode curNode = rootNode;
        for (char c : element.toCharArray()) {
            if (!curNode.hasChildren(c)) {
                return false;
            }
            curNode = curNode.go(c);
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
            curNode = curNode.go(c);
            curNode.decCount();
        }
        curNode.setLeaf(false);
        rootNode.decCount();
        return true;
    }

    @Override
    public int size() {
        return rootNode.getCount();
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        TrieNode curNode = rootNode;
        for (char c : prefix.toCharArray()) {
            if (!curNode.hasChildren(c)) {
                return 0;
            }
            curNode = curNode.go(c);
        }
        return curNode.getCount();
    }
}


