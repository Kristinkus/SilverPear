package com.example.silverpear.concurrency.dto;

import lombok.Data;

@Data
public class AsyncTaskStatusResponse {
    private volatile Long taskId;
    private volatile AsyncTaskState state;
    private volatile int progressPercent;
    private volatile String result;
    private volatile String error;
}
