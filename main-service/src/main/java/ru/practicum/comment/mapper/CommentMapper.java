package ru.practicum.comment.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.user.mapper.UserMapper;

@Component
public class CommentMapper {

    private final UserMapper userMapper;

    public CommentMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public CommentDto toDto(Comment comment) {
        if (comment == null) return null;
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(userMapper.toUserShortDto(comment.getUser()))
                .eventId(comment.getEvent().getId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .status(comment.getStatus())
                .build();
    }
}
