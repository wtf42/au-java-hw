package eakimov.VCS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RepositoryState implements Serializable {
    private final HashMap<String, Branch> allBranches = new HashMap<>();
    private Branch currentBranch;
    private Revision currentRevision;

    public Revision getCurrentRevision() {
        return currentRevision;
    }

    public Branch getCurrentBranch() {
        return currentBranch;
    }

    public void setCurrentRevision(Revision currentRevision) {
        this.currentRevision = currentRevision;
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
}
