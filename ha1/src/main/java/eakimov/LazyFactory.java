package eakimov;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class LazyFactory {
    public static <T> Lazy<T> createLazySingleThreaded(Supplier<T> supplier) {
        return new SingleThreadedLazyImpl<T>(supplier);
    }
    public static <T> Lazy<T> createLazyMultiThreaded(Supplier<T> supplier) {
        return new MultiThreadedLazyImpl<T>(supplier);
    }
    public static <T> Lazy<T> createLazyLockFree(Supplier<T> supplier) {
        return new LockFreeLazyImpl<T>(supplier);
    }

    private static class SingleThreadedLazyImpl<T> implements Lazy<T> {
        private Supplier<T> supplier;
        private T object;

        public SingleThreadedLazyImpl(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (supplier != null) {
                object = supplier.get();
                supplier = null;
            }
            return object;
        }
    }

    private static class MultiThreadedLazyImpl<T> implements Lazy<T> {
        private Supplier<T> supplier;
        private T object;

        public MultiThreadedLazyImpl(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public synchronized T get() {
            if (supplier != null) {
                object = supplier.get();
                supplier = null;
            }
            return object;
        }
    }

    private static class LockFreeLazyImpl<T> implements Lazy<T> {
        private volatile Supplier<T> supplier;
        private final AtomicReference<T> object = new AtomicReference<T>();

        public LockFreeLazyImpl(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            final Supplier<T> currentSupplier = supplier;
            if (currentSupplier == null) {
                return object.get();
            }
            final T newObject = currentSupplier.get();
            if (object.compareAndSet(null, newObject)) {
                supplier = null;
                return newObject;
            }
            return object.get();
        }
    }
}
