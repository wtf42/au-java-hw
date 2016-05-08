package eakimov;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class ThreadPoolImplTest {

    @Test
    public void testNumThreads() throws Exception {
        final int numThreads = 5;
        final ThreadPool threadPool = new ThreadPoolImpl(numThreads);

        final int checkThreads = 10;
        final boolean[] running = new boolean[checkThreads];

        for (int i = 0; i < checkThreads; i++) {
            running[i] = false;
            final int idx = i;

            threadPool.submit(() -> {
                running[idx] = true;
                uncheckedSleep(1000);
                return 42;
            });

            Thread.sleep(50);

            // make sure that threadPool contains exactly <numThreads> threads
            for (int j = 0; j <= i; j++) {
                assertEquals(j < numThreads, running[j]);
            }
        }
    }

    @Test
    public void testResults() throws Exception {
        final int numThreads = 2;
        final ThreadPool threadPool = new ThreadPoolImpl(numThreads);

        final Supplier<Integer> ret42 = () -> 42;
        final LightFuture<Integer> result = threadPool.submit(ret42);
        assertEquals(42, (int)result.get());
    }

    @Test
    public void testException() throws Exception {
        final int numThreads = 2;
        final ThreadPool threadPool = new ThreadPoolImpl(numThreads);

        final Supplier<Integer> retException = () -> {
            throw new IllegalArgumentException();
        };
        final LightFuture<Integer> result = threadPool.submit(retException);
        try {
            result.get();
            fail("LightExecutionException not thrown");
        } catch (LightExecutionException exception) {
            assertEquals(
                    IllegalArgumentException.class,
                    exception.getThrowable().getClass());
        }
    }

    @Test
    public void testChaining() throws Exception {
        final int numThreads = 4;
        final ThreadPool threadPool = new ThreadPoolImpl(numThreads);

        final Supplier<Integer> ret0 = () -> {
            uncheckedSleep(10);
            return 0;
        };
        final Function<Integer, Integer> add = (Integer val) -> {
            uncheckedSleep(10);
            return val + 1;
        };

        final LightFuture<Integer> val0 = threadPool.submit(ret0);
        final int addCnt = 10;

        LightFuture<Integer> val = val0;
        for (int i = 0; i < addCnt; ++i) {
            val = val.thenApply(add);
        }
        assertEquals(addCnt, (int)val.get());
    }

    @Test
    public void testReadyQuery() throws Exception {
        final int numThreads = 2;
        final ThreadPool threadPool = new ThreadPoolImpl(numThreads);

        final Supplier<Integer> waiter = () -> {
            uncheckedSleep(100);
            return 0;
        };
        final LightFuture<Integer> lightFuture = threadPool.submit(waiter);
        assertEquals(false, lightFuture.isReady());
    }

    @Test
    public void testManyDependencies() throws Exception {
        final int numThreads = 5;
        final ThreadPool threadPool = new ThreadPoolImpl(numThreads);

        final int sleepFirstTime = 100;
        final int sleepOtherTime = 100;

        final Supplier<Integer> waiter = () -> {
            uncheckedSleep(sleepFirstTime);
            return 0;
        };

        // [0] = current #threads, [1] = maximum #threads
        final Integer[] counter = new Integer[] { 0, 0 };
        final Function<Integer, Integer> dependent = (Integer val) -> {
            synchronized (counter) {
                counter[0]++;
                counter[1] = Math.max(counter[1], counter[0]);
            }
            uncheckedSleep(sleepOtherTime);
            synchronized (counter) {
                counter[0]--;
            }
            return val + 1;
        };

        final int dependencies = 10;
        final LightFuture<Integer> lightFuture = threadPool.submit(waiter);
        for (int i = 0; i < dependencies; i++) {
            lightFuture.thenApply(dependent);
        }

        Thread.sleep(sleepFirstTime + 2 * sleepOtherTime * dependencies / numThreads);

        assertEquals(0, (int)counter[0]);
        // check that dependent tasks are processed by all threads, not by single one
        assertTrue(counter[1] > 1);
    }

    @Test
    public void testShutdown() throws Exception {
        final int numThreads = 1;
        final ThreadPool threadPool = new ThreadPoolImpl(numThreads);

        final int waiterSleep = 100;
        final Supplier<Integer> waiter = () -> {
            uncheckedSleep(waiterSleep);
            return 0;
        };

        final List<LightFuture> tasks = new ArrayList<>();

        final int tasksCnt = 10;
        for (int i = 0; i < tasksCnt; i++) {
            tasks.add(threadPool.submit(waiter));
        }

        Thread.sleep(waiterSleep * 2);
        threadPool.shutdown();
        Thread.sleep(waiterSleep * 2);

        int cancelledTasks = 0;
        for (LightFuture task : tasks) {
            assertTrue(task.isReady());
            if (isCancelledTask(task)) {
                cancelledTasks++;
            }
        }
        // check that cancelled state is set after shutdown
        assertTrue(cancelledTasks > 1);
    }

    @Test
    public void testShutdownDependents() throws Exception {
        final int numThreads = 1;
        final ThreadPool threadPool = new ThreadPoolImpl(numThreads);

        final int sleepFirstTime = 100;
        final int sleepOtherTime = 10;

        final Supplier<Integer> ret0 = () -> {
            uncheckedSleep(sleepFirstTime);
            return 0;
        };
        final Function<Integer, Integer> add = (Integer val) -> {
            uncheckedSleep(sleepOtherTime);
            return val + 1;
        };

        final int dependencies = 42;
        final List<LightFuture> dependentTasks = new ArrayList<>();
        final LightFuture<Integer> val0 = threadPool.submit(ret0);

        LightFuture<Integer> val = val0;
        for (int i = 0; i < dependencies; ++i) {
            val = val.thenApply(add);
            dependentTasks.add(val);
        }

        val0.get(); // wait for first result
        Thread.sleep(sleepOtherTime * dependencies / 2); // wait for ~half tasks to get ready
        threadPool.shutdown();
        Thread.sleep(sleepOtherTime * 2); // wait for thread interrupts

        boolean hasFinished = false;
        boolean hasCancelled = false;
        for (LightFuture task : dependentTasks) {
            if (isCancelledTask(task)) {
                hasCancelled = true;
            } else {
                hasFinished = true;
            }
        }
        // check that dependent tasks are also set to cancelled state
        assertTrue(hasCancelled);
        assertTrue(hasFinished);
    }

    @Test
    public void testManyShutdowns() throws Exception {
        final int numThreads = 2;
        final ThreadPool threadPool = new ThreadPoolImpl(numThreads);
        final Supplier<Integer> waiter = () -> {
            uncheckedSleep(100);
            return 0;
        };
        threadPool.submit(waiter);
        Thread.sleep(100);
        threadPool.shutdown();
        threadPool.shutdown();
        Thread.sleep(10);
        threadPool.shutdown();
    }

    @Test
    public void testTasksAfterShutdown() throws Exception {
        final int numThreads = 1;
        final ThreadPool threadPool = new ThreadPoolImpl(numThreads);

        final int sleepTime = 10;
        final Supplier<Integer> ret0 = () -> {
            uncheckedSleep(sleepTime);
            return 0;
        };
        final Function<Integer, Integer> add = (Integer val) -> {
            uncheckedSleep(sleepTime);
            return val + 1;
        };
        final LightFuture<Integer> val0 = threadPool.submit(ret0);
        threadPool.shutdown();
        Thread.sleep(sleepTime * 2);

        final int dependencies = 10;
        final List<LightFuture> dependentTasks = new ArrayList<>();
        LightFuture<Integer> val = val0;
        for (int i = 0; i < dependencies; i++) {
            val = val.thenApply(add);
            dependentTasks.add(val);
        }

        final LightFuture<Integer> val0AfterShutdown = threadPool.submit(ret0);

        Thread.sleep(sleepTime * 2);

        for (LightFuture lightFuture : dependentTasks) {
            assertTrue(isCancelledTask(lightFuture));
        }
        assertTrue(isCancelledTask(val0AfterShutdown));
    }

    private static void uncheckedSleep(int time) {
        try {
            Thread.sleep(time);
        } catch(InterruptedException ignored) {}
    }

    private static boolean isCancelledTask(LightFuture task) throws InterruptedException {
        try {
            task.get();
            return false;
        } catch (LightExecutionException exception) {
            assertEquals(
                    InterruptedException.class,
                    exception.getThrowable().getClass());
            return true;
        }
    }
}
