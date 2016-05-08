package eakimov;

import java.util.function.Function;

public interface LightFuture<R> {
    boolean isReady();

    /**
     * Returns immediately if task is ready, or waits until it's done
     * @return future result
     * @throws LightExecutionException if exception was thrown during task
     * execution
     */
    R get() throws LightExecutionException, InterruptedException;

    <U> LightFuture<U> thenApply(Function<? super R, ? extends U> f);
}
