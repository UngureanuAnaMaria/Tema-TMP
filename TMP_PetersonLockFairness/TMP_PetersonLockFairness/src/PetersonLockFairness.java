import java.util.concurrent.atomic.AtomicInteger;

public class PetersonLockFairness {
    private final AtomicInteger[] level;
    private final AtomicInteger[] victim;
    private volatile int counter = 0;
    private final int n;
    private final int limit = 1500;
    private final int[] accessCount;
    private final AtomicInteger fairAccessIndex = new AtomicInteger(0);

    public PetersonLockFairness(int n) {
        this.n = n;
        level = new AtomicInteger[n];
        victim = new AtomicInteger[n];
        accessCount = new int[n];
        for (int i = 0; i < n; i++) {
            level[i] = new AtomicInteger(0);
            victim[i] = new AtomicInteger(0);
        }
    }

    public void lock(int i) {
        while (fairAccessIndex.get() % n != i) {

        }

        for (int L = 1; L < n; L++) {
            level[i].set(L);
            victim[L].set(i);
            boolean exists;
            do {
                exists = false;
                for (int k = 0; k < n; k++) {
                    if (k != i && level[k].get() >= L) {
                        exists = true;
                        break;
                    }
                }
            } while (exists && victim[L].get() == i);
        }
    }

    public void unlock(int i) {
        level[i].set(0);
        fairAccessIndex.incrementAndGet();
    }

    public static void main(String[] args) {
        final int numThreads = 3;
        PetersonLockFairness lock = new PetersonLockFairness(numThreads);

        Runnable task = () -> {
            int id = Integer.parseInt(Thread.currentThread().getName());

            while (lock.counter < lock.limit) {
                while (lock.fairAccessIndex.get() % numThreads != id) {

                }
                lock.lock(id);
                try {
                    if (lock.counter < lock.limit) {
                        lock.counter++;
                        lock.accessCount[id]++;
                        System.out.println("Thread " + id + " accessed the critical section. Counter: " + lock.counter);
                    }
                } finally {
                    lock.unlock(id);
                }
            }
        };

        Thread[] threads = new Thread[numThreads];
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(task, String.valueOf(i));
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Final counter value: " + lock.counter);
        System.out.println("Execution time: " + (endTime - startTime) + "ms");
        System.out.println("Access count per thread:");
        for (int i = 0; i < numThreads; i++) {
            System.out.println("Thread " + i + ": " + lock.accessCount[i] + " accesses");
        }
    }
}