package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.comment.model.CommentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentStatusRequest {
    @NotNull
    private CommentStatus status;
}
