package eakimov;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class LazyFactoryTest {
    @Test
    public void testReturnValue() throws Exception {
        assertEquals(42, (int)LazyFactory.createLazySingleThreaded(() -> 42).get());
        assertEquals(42, (int)LazyFactory.createLazyMultiThreaded(() -> 42).get());
        assertEquals(42, (int)LazyFactory.createLazyLockFree(() -> 42).get());
    }

    @Test
    public void testSameReturns() throws Exception {
        final int runCount = 100;
        final int[] returnValue = new int[1];
        final Supplier<Integer> supplier = () -> returnValue[0]++;

        final Lazy<Integer> lazy = LazyFactory.createLazySingleThreaded(supplier);
        assertEquals(1, Stream.generate(lazy::get)
                .limit(runCount)
                .distinct()
                .count());

        final Lazy<Integer> lazy2 = LazyFactory.createLazyMultiThreaded(supplier);
        assertEquals(1, Stream.generate(lazy2::get)
                .limit(runCount)
                .distinct()
                .count());

        final Lazy<Integer> lazy3 = LazyFactory.createLazyLockFree(supplier);
        assertEquals(1, Stream.generate(lazy3::get)
                .limit(runCount)
                .distinct()
                .count());
    }

    @Test
    public void testSingleThreadedNullReturns() throws Exception {
        final int[] runCount = new int[]{0};
        Supplier<Integer> updateCountSupplier = () -> {
            runCount[0]++;
            return null;
        };
        final Lazy<Integer> lazy=LazyFactory.createLazySingleThreaded(updateCountSupplier);
        assertEquals(null, lazy.get());
        assertEquals(null, lazy.get());
        assertEquals(1, runCount[0]);
    }

    @Test
    public void testMultiThreadedNullReturns() throws Exception {
        final AtomicInteger runCount = new AtomicInteger(0);
        final Supplier<Integer> updateCountSupplier = () -> {
            runCount.incrementAndGet();
            return null;
        };
        final Lazy<Integer> lazy=LazyFactory.createLazyMultiThreaded(updateCountSupplier);
        final int numThreads = 100;
        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
                lazy.get();
            }).start();
        }
        Thread.sleep(100);
        assertEquals(1, runCount.get());
    }

}