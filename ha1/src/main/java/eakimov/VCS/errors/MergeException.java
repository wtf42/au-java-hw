package eakimov.VCS.errors;

public class MergeException extends RecoverableRepositoryException {
    public MergeException(String message) {
        super("failed to merge: " + message);
    }
}
