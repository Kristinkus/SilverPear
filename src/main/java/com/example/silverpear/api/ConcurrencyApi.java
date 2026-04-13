package com.example.silverpear.api;

import com.example.silverpear.concurrency.dto.AsyncTaskStatusResponse;
import com.example.silverpear.concurrency.dto.RaceConditionResultResponse;
import com.example.silverpear.concurrency.dto.StartAsyncTaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/concurrency")
@Tag(name = "Конкурентность", description = "Асинхронные операции, счетчики и race condition")
public interface ConcurrencyApi {

    @PostMapping("/tasks")
    @Operation(summary = "Запустить асинхронную бизнес-операцию")
    ResponseEntity<StartAsyncTaskResponse> startAsyncTask();

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Проверить статус асинхронной задачи")
    ResponseEntity<AsyncTaskStatusResponse> getTaskStatus(@PathVariable Long taskId);

    @GetMapping("/race-condition")
    @Operation(summary = "Демонстрация race condition и решений")
    ResponseEntity<RaceConditionResultResponse> raceConditionRun(
            @RequestParam(defaultValue = "64") int threads,
            @RequestParam(defaultValue = "10000") int incrementsPerThread);
}
