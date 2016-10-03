package eakimov.VCS.errors;

public class StageException extends RecoverableRepositoryException {
    public StageException(String message) {
        super(message);
    }
}
