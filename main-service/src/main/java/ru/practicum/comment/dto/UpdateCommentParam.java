package ru.practicum.comment.dto;

public record UpdateCommentParam(
        Long userId,
        Long commentId,
        String comment
) {
}
