package ru.practicum.comment.dto;

public record PostCommentParam(
        Long userId,
        Long eventId,
        String comment
) {
}
