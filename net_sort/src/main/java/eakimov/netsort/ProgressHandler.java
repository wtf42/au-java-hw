package eakimov.netsort;

public interface ProgressHandler {
    void setProgress(int value);
    void setCompleted();
    void setError(String message);
}
