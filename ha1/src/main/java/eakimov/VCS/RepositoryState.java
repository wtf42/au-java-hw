package eakimov.VCS;

import java.io.Serializable;
import java.util.*;

public class RepositoryState implements Serializable {
    private final Map<String, Branch> allBranches = new HashMap<>();
    private final Set<String> stageFiles = new HashSet<>();
    private Branch currentBranch;
    private Revision currentRevision;

    public Revision getCurrentRevision() {
        return currentRevision;
    }

    public void setCurrentRevision(Revision currentRevision) {
        this.currentRevision = currentRevision;
    }

    public Branch getCurrentBranch() {
        return currentBranch;
    }

    public void setCurrentBranch(Branch currentBranch) {
        this.currentBranch = currentBranch;
        if (currentBranch != null) {
            setCurrentRevision(currentBranch.getHeadRevision());
        }
    }

    public Branch findBranchByName(String name) {
        return allBranches.getOrDefault(name, null);
    }

    public void addBranch(Branch branch) {
        allBranches.put(branch.getName(), branch);
    }

    public Branch deleteBranch(Branch branch) {
        if (currentBranch == branch) {
            setCurrentBranch(null);
            setCurrentRevision(null);
        }
        return allBranches.remove(branch.getName());
    }

    public List<String> getAllBranchNames() {
        return new ArrayList<>(allBranches.keySet());
    }

    public void addStageFile(String filename) {
        stageFiles.add(filename);
    }

    public void deleteStageFile(String filename) {
        stageFiles.remove(filename);
    }

    public void clearStageFiles() {
        stageFiles.clear();
    }

    public boolean isStageFile(String filename) {
        return stageFiles.contains(filename);
    }

    public Set<String> getStageFiles() {
        return stageFiles;
    }
}
