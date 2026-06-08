package com.amalitech.todoApi.dto.response;
import java.time.LocalDateTime;

public record TodoResponse(
     Long id,
     String title,
     String content,
     LocalDateTime createdAt,
     LocalDateTime updatedAt
) {
}
