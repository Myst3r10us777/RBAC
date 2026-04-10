package allfiles;

import java.util.concurrent.*;

public class BackgroundExecutor {
    private ExecutorService executor;

    public BackgroundExecutor(int poolSize) {
        this.executor = Executors.newFixedThreadPool(poolSize);
    }

    public void submit(Runnable task) {
        executor.submit(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
