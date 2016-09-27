package eakimov.VCS.errors;

public class BranchManagementException extends RecoverableRepositoryException {
    public BranchManagementException(String message) {
        super(message);
    }
    public BranchManagementException(String branchName, String message) {
        super("branch " + branchName + ": " + message);
    }
}
