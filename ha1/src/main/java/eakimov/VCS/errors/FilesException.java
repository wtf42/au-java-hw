package eakimov.VCS.errors;

public class FilesException extends UnrecoverableRepositoryException {
    public FilesException(String directory, String what, String message) {
        super("failed to " + what + " directory " + directory + ": " + message);
    }
}
