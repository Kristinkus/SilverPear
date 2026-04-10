package com.example.silverpear.concurrency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RaceConditionResultResponse {
    private int threads;
    private int incrementsPerThread;
    private int expected;
    private int unsafeActual;
    private int synchronizedActual;
    private int atomicActual;
    private int unsafeLostUpdates;
    private int synchronizedLostUpdates;
    private int atomicLostUpdates;
    private long durationMs;
}
