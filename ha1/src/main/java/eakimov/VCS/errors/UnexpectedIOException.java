package eakimov.VCS.errors;

import java.io.IOException;

public class UnexpectedIOException extends RepositoryException {
    public UnexpectedIOException(IOException e) {
        super("unexpected i/o excpetion: " + e.getMessage());
    }
}
