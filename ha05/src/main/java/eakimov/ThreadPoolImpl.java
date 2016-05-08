package eakimov;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadPoolImpl implements ThreadPool {
    private final Queue<LightFutureImpl> tasks;
    private final List<Thread> threads;
    private boolean terminated;

    public ThreadPoolImpl(int numThreads) {
        tasks = new LinkedList<>();
        threads = new ArrayList<>();
        terminated = false;

        Runnable poolWorker = new PoolWorker();
        for (int i = 0; i < numThreads; i++) {
            Thread thread = new Thread(poolWorker);
            thread.start();
            threads.add(thread);
        }
    }

    @Override
    public <R> LightFuture<R> submit(Supplier<R> supplier) {
        LightFutureImpl<R> lightFuture = new LightFutureImpl<>(supplier);
        addTask(lightFuture);
        return lightFuture;
    }

    @Override
    public synchronized void shutdown() {
        terminated = true;
        synchronized (tasks) {
            tasks.forEach(LightFutureImpl::setCancelled);
            tasks.clear();
        }
        threads.forEach(Thread::interrupt);
        threads.clear();
    }

    private synchronized <R> void addTask(LightFutureImpl<R> task) {
        if (terminated) {
            task.setCancelled();
        } else {
            synchronized (tasks) {
                tasks.add(task);
                tasks.notifyAll();
            }
        }
    }

    private class PoolWorker implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                Runnable task;
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                        } catch (InterruptedException ignored) {
                            return;
                        }
                    }
                    task = tasks.poll();
                }
                task.run();
            }
        }
    }

    private class LightFutureFunctionImpl<R, U> extends LightFutureImpl<U> {
        private final Function<? super R, ? extends U> function;

        public LightFutureFunctionImpl(Function<? super R, ? extends U> function) {
            super(null);
            this.function = function;
        }

        public synchronized void setArgument(R argument) {
            supplier = () -> function.apply(argument);
        }
    }

    private class LightFutureImpl<R> implements LightFuture<R>, Runnable {
        protected Supplier<R> supplier;
        private boolean ready;
        private R result;
        private boolean isCancelled;
        private LightExecutionException executionException;
        private final List<LightFutureFunctionImpl<R, ?>> pendingTasks;

        public LightFutureImpl(Supplier<R> supplier) {
            this.supplier = supplier;
            ready = false;
            result = null;
            isCancelled = false;
            executionException = null;
            pendingTasks = new ArrayList<>();
        }

        @Override
        public synchronized boolean isReady() {
            return ready;
        }

        @Override
        public synchronized R get() throws LightExecutionException, InterruptedException {
            while (!ready) {
                wait();
            }
            if (isCancelled) {
                // thread pool shut down
                throw new LightExecutionException(new InterruptedException());
            }
            if (executionException != null) {
                throw executionException;
            }
            return result;
        }

        @Override
        public synchronized <U> LightFuture<U> thenApply(
                    Function<? super R, ? extends U> function) {
            LightFutureFunctionImpl<R, U> lightFuture =
                    new LightFutureFunctionImpl<R, U>(function);
            if (ready) {
                updatePending(lightFuture);
            } else {
                pendingTasks.add(lightFuture);
            }
            return lightFuture;
        }

        @Override
        public void run() {
            try {
                setResult(supplier.get());
            } catch (Throwable throwable) {
                setException(new LightExecutionException(throwable));
            }
        }

        public synchronized void setResult(R supplierResult) {
            ready = true;
            result = supplierResult;
            updateResults();
        }

        public synchronized void setException(LightExecutionException exception) {
            ready = true;
            executionException = exception;
            updateResults();
        }

        public synchronized void setCancelled() {
            ready = true;
            isCancelled = true;
            updateResults();
        }

        private synchronized void updateResults() {
            pendingTasks.forEach(this::updatePending);
            pendingTasks.clear();
            notifyAll();
        }

        private synchronized void updatePending(
                    LightFutureFunctionImpl<R, ?> pendingLightFuture) {
            if (isCancelled) {
                pendingLightFuture.setCancelled();
            } else if (executionException != null) {
                pendingLightFuture.setException(executionException);
            } else {
                pendingLightFuture.setArgument(result);
                ThreadPoolImpl.this.addTask(pendingLightFuture);
            }
        }
    }
}
