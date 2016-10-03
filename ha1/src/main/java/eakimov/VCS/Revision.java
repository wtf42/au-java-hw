package eakimov.VCS;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class Revision implements Serializable {
    private final String commitMessage;
    private final Map<String, Revision> files;

    private final int id;
    private final Branch branch;
    private final Revision parent;
    private final Revision mergeParent;

    // new revision with parent
    public Revision(Branch branch,
                    Revision revision,
                    String commitMessage,
                    Map<String, Revision> files) {
        this.commitMessage = commitMessage;
        this.files = fixNewFiles(files);
        this.id = branch.nextRevisionId();
        this.branch = branch;
        this.parent = revision;
        this.mergeParent = null;
    }

    // merge
    public Revision(Branch branch,
                    Revision parent,
                    Revision mergeParent,
                    String commitMessage,
                    Map<String, Revision> files) {
        this.commitMessage = commitMessage;
        this.files = fixNewFiles(files);
        this.id = branch.nextRevisionId();
        this.branch = branch;
        this.parent = parent;
        this.mergeParent = mergeParent;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public Collection<String> getFiles() {
        return files.keySet();
    }

    public Revision getFileRevision(String file) {
        return files.get(file);
    }

    public Map<String, Revision> getAllFileRevisions() {
        return files;
    }

    public String getRevisionDirectory() {
        return branch.getDirectoryPrefix() + Integer.toString(id);
    }

    public int getId() {
        return id;
    }

    public Branch getBranch() {
        return branch;
    }

    public Revision getParent() {
        return parent;
    }

    public Revision getMergeParent() {
        return mergeParent;
    }

    private Map<String, Revision> fixNewFiles(Map<String, Revision> files) {
        return files.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue() != null ? entry.getValue() : this));
    }
}
