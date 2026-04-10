package com.example.silverpear.concurrency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CounterSnapshotResponse {
    private int synchronizedCounter;
    private int atomicCounter;
}
