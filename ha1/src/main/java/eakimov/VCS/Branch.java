package eakimov.VCS;

import java.io.Serializable;

public class Branch implements Serializable {
    private final String name;
    private final String directoryPrefix;
    private int revisionCounter;
    private Revision headRevision;

    public Branch(String name, Revision revision) {
        this.name = name;
        this.directoryPrefix = generateDirectoryPrefix(name);
        this.revisionCounter = 1;
        this.headRevision = revision;
    }

    public String getName() {
        return name;
    }

    public String getDirectoryPrefix() {
        return directoryPrefix;
    }

    public Revision getHeadRevision() {
        return headRevision;
    }

    public void setHeadRevision(Revision revision) {
        headRevision = revision;
    }

    public int nextRevisionId() {
        return revisionCounter++;
    }

    private static String generateDirectoryPrefix(String name) {
        return Integer.toString(name.hashCode(), 16) + "_";
    }
}
