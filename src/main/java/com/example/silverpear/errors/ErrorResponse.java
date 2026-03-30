package com.example.silverpear.errors;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "ErrorResponse", description = "Единый формат ошибки API")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    @Schema(description = "Краткое сообщение")
    private String message;

    @Schema(description = "Детали (в т.ч. поля валидации)")
    private List<String> errors;

    @Schema(description = "HTTP-код")
    private int status;

    @Schema(description = "URL запроса")
    private String path;

    @Schema(description = "Время ошибки")
    private LocalDateTime timestamp;
}
