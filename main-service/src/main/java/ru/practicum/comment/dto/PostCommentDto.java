package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCommentDto(
        @NotBlank @Size(min = 1, max = 2000) String comment
) {
}
