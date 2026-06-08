package com.amalitech.todoApi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;

    private Long categoryId;
    private String tags; // Comma separated tags
}
