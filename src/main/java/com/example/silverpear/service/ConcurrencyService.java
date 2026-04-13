package com.example.silverpear.service;

import com.example.silverpear.concurrency.dto.AsyncTaskState;
import com.example.silverpear.concurrency.dto.AsyncTaskStatusResponse;
import com.example.silverpear.concurrency.dto.RaceConditionResultResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class ConcurrencyService {

    private final ConcurrentHashMap<Long, AsyncTaskStatusResponse> taskStatuses = new ConcurrentHashMap<>();
    private final AtomicLong taskIdGenerator = new AtomicLong(1);
    private final AtomicInteger atomicCounter = new AtomicInteger(0);
    private final ConcurrencyService self;
    private int synchronizedCounter = 0;
    private int unsafeCounter = 0;

    public ConcurrencyService(@Lazy ConcurrencyService self) {
        this.self = self != null ? self : this;
    }

    public Long startAsyncBusinessOperation() {
        Long taskId = taskIdGenerator.getAndIncrement();
        AsyncTaskStatusResponse status = new AsyncTaskStatusResponse();
        status.setTaskId(taskId);
        status.setState(AsyncTaskState.ACCEPTED);
        status.setProgressPercent(0);

        taskStatuses.put(taskId, status);

        self.processTaskAsync(taskId);
        return taskId;
    }

    @Async
    public CompletableFuture<Void> processTaskAsync(Long taskId) {
        AsyncTaskStatusResponse status = taskStatuses.get(taskId);
        if (status == null) {
            return CompletableFuture.completedFuture(null);
        }

        try {
            status.setState(AsyncTaskState.IN_PROGRESS);
            for (int progress = 10; progress <= 100; progress += 10) {
                TimeUnit.SECONDS.sleep(1);
                status.setProgressPercent(progress);
                if (progress == 50) {
                    status.setState(AsyncTaskState.SAVED);
                }
            }
            status.setResult("Задача выполнена успешно.");
            status.setState(AsyncTaskState.COMPLETED);
            log.info("Async task completed: {}", taskId);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            status.setState(AsyncTaskState.FAILED);
            status.setError("Task interrupted");
            log.warn("Async task interrupted: {}", taskId, ex);
        } catch (Exception ex) {
            status.setState(AsyncTaskState.FAILED);
            status.setError(ex.getMessage());
            log.error("Async task failed: {}", taskId, ex);
        }
        return CompletableFuture.completedFuture(null);
    }

    public AsyncTaskStatusResponse getTaskStatus(Long taskId) {
        return taskStatuses.get(taskId);
    }

    public RaceConditionResultResponse runRaceCondition(int threads, int incrementsPerThread) {
        synchronized (this) {
            synchronizedCounter = 0;
            unsafeCounter = 0;
        }
        atomicCounter.set(0);

        int expected = threads * incrementsPerThread;
        long startedAt = System.nanoTime();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finishGate = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    for (int i = 0; i < incrementsPerThread; i++) {
                        unsafeCounter++;
                        synchronized (this) {
                            synchronizedCounter++;
                        }
                        atomicCounter.incrementAndGet();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishGate.countDown();
                }
            });
        }

        startGate.countDown();
        try {
            finishGate.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        int unsafeActual = unsafeCounter;
        int synchronizedActual;
        synchronized (this) {
            synchronizedActual = synchronizedCounter;
        }
        int atomicActual = atomicCounter.get();

        return new RaceConditionResultResponse(
                threads,
                incrementsPerThread,
                expected,
                unsafeActual,
                synchronizedActual,
                atomicActual,
                expected - unsafeActual,
                expected - synchronizedActual,
                expected - atomicActual,
                durationMs
        );
    }
}
