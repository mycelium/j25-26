package com.httpserver;

import java.util.concurrent.*;

public class ThreadPool {
    private final ExecutorService executor;
    
    public ThreadPool(int threadCount, boolean useVirtualThreads) {
        if (useVirtualThreads && isVirtualThreadsSupported()) {
            this.executor = (ExecutorService) createVirtualThreadExecutor();
        } else {
            this.executor = Executors.newFixedThreadPool(threadCount);
        }
    }
    
    private boolean isVirtualThreadsSupported() {
        try {
            Class.forName("java.lang.Thread.Builder$VirtualThreadFactory");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    private ExecutorService createVirtualThreadExecutor() {
        try {
            // Java 21+ via reflection
            return (ExecutorService) Executors.class
                .getMethod("newVirtualThreadPerTaskExecutor")
                .invoke(null);
        } catch (Exception e) {
            // Fallback to fixed thread pool
            return Executors.newFixedThreadPool(10);
        }
    }
    
    public void execute(Runnable task) {
        executor.execute(task);
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}