package com.example.silverpear.controller;

import com.example.silverpear.api.ConcurrencyApi;
import com.example.silverpear.concurrency.dto.AsyncTaskStatusResponse;
import com.example.silverpear.concurrency.dto.RaceConditionResultResponse;
import com.example.silverpear.concurrency.dto.StartAsyncTaskResponse;
import com.example.silverpear.service.ConcurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class ConcurrencyController implements ConcurrencyApi {

    private final ConcurrencyService concurrencyService;

    @Override
    public ResponseEntity<StartAsyncTaskResponse> startAsyncTask() {
        Long taskId = concurrencyService.startAsyncBusinessOperation();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new StartAsyncTaskResponse(taskId));
    }

    @Override
    public ResponseEntity<AsyncTaskStatusResponse> getTaskStatus(Long taskId) {
        AsyncTaskStatusResponse status = concurrencyService.getTaskStatus(taskId);
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        return ResponseEntity.ok(status);
    }

    @Override
    public ResponseEntity<RaceConditionResultResponse> raceConditionRun(int threads, int incrementsPerThread) {
        if (threads < 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "threads must be >= 50");
        }
        if (incrementsPerThread < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "incrementsPerThread must be >= 1");
        }
        RaceConditionResultResponse result = concurrencyService.runRaceCondition(threads, incrementsPerThread);
        return ResponseEntity.ok(result);
    }
}
