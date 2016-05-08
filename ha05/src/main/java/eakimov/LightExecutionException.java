package eakimov;

/**
 * Holds exception that was thrown during task execution
 */
public class LightExecutionException extends Exception {
    private final Throwable throwable;

    public LightExecutionException(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
