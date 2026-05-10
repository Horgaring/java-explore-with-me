package ru.practicum.comment.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentSearchParams {
    private Long eventId;
    @PositiveOrZero
    private Integer from = 0;
    @Positive
    private Integer size = 10;
}
