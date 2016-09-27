package eakimov.VCS;

import java.io.Serializable;

public class Revision implements Serializable {
    private final Branch branch;
    private final int id;
    private final Revision parent;
    private final Revision mergeParent;
    private final String commitMessage;

    // new revision with parent
    public Revision(Branch branch, Revision revision, String commitMessage) {
        this.branch = branch;
        this.id = branch.nextRevisionId();
        this.parent = revision;
        this.mergeParent = null;
        this.commitMessage = commitMessage;
    }

    // copy revision to new branch
    public Revision(Branch branch, Revision other) {
        this.branch = branch;
        this.id = branch.nextRevisionId();
        this.parent = other.getParent();
        this.mergeParent = other.getMergeParent();
        this.commitMessage = other.getCommitMessage();
    }

    // merge
    public Revision(Branch branch,
                    Revision parent,
                    Revision mergeParent,
                    String commitMessage) {
        this.branch = branch;
        this.id = branch.nextRevisionId();
        this.parent = parent;
        this.mergeParent = mergeParent;
        this.commitMessage = commitMessage;
    }

    public Branch getBranch() {
        return branch;
    }

    public int getId() {
        return id;
    }

    public Revision getParent() {
        return parent;
    }

    public Revision getMergeParent() {
        return mergeParent;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public String getDirectory() {
        return branch.getDirectoryPrefix() + Integer.toString(id);
    }
}
