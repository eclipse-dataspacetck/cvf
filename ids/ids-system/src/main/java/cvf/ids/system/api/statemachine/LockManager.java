package cvf.ids.system.api.statemachine;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Manages read and write operations, allowing for re-entrant lock acquisition.
 */
public class LockManager {
    private static final int TIMEOUT = 5000;

    private final ReadWriteLock lock;

    public LockManager() {
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Obtains a read-lock or times out if unable to do so by the {@link #TIMEOUT}.
     */
    public <T> T readLock(Supplier<T> work) {
        try {
            if (!lock.readLock().tryLock(TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Timeout acquiring read lock");
            }
            try {
                return work.get();
            } finally {
                lock.readLock().unlock();
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new IllegalStateException(e);
        }
    }

    /**
     * Obtains a write-lock or times out if unable to do so by the {@link #TIMEOUT}.
     */
    public <T> T writeLock(Supplier<T> work) {
        try {
            if (!lock.writeLock().tryLock(TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Timeout acquiring write lock");
            }
            try {
                return work.get();
            } finally {
                lock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new IllegalStateException(e);
        }
    }
}
