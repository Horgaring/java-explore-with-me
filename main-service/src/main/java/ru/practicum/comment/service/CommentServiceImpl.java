package ru.practicum.comment.service;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.*;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;
import ru.practicum.comment.model.QComment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Override
    public List<CommentDto> searchComments(AdminCommentSearchFilter filter) {
        if (filter.getRangeStart() != null && filter.getRangeEnd() != null
                && filter.getRangeStart().isAfter(filter.getRangeEnd())) {
            throw new BadRequestException("Range start must be before range end");
        }

        QComment comment = QComment.comment;
        BooleanBuilder where = new BooleanBuilder();

        if (filter.getText() != null && !filter.getText().isBlank()) {
            where.and(comment.content.lower().like("%" + filter.getText().toLowerCase() + "%"));
        }
        if (filter.getStatus() != null) {
            where.and(comment.status.eq(filter.getStatus()));
        }
        if (filter.getEventId() != null) {
            where.and(comment.event.id.eq(filter.getEventId()));
        }
        if (filter.getUserId() != null) {
            where.and(comment.user.id.eq(filter.getUserId()));
        }
        if (filter.getRangeStart() != null) {
            where.and(comment.createdAt.goe(filter.getRangeStart()));
        }
        if (filter.getRangeEnd() != null) {
            where.and(comment.createdAt.loe(filter.getRangeEnd()));
        }

        Pageable pageable = PageRequest.of(filter.getFrom() / filter.getSize(), filter.getSize(),
                Sort.by("createdAt").descending());

        return commentRepository.findAll(where, pageable).getContent().stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto findCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
        return commentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateStatusComment(Long commentId, UpdateCommentStatusRequest updateStatus) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));

        if (updateStatus.getStatus() == CommentStatus.PUBLISHED
                && comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Only pending comments can be published");
        }

        comment.setStatus(updateStatus.getStatus());
        return commentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment with id=" + commentId + " was not found");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> findAllByAuthor(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        return commentRepository.findByUserId(userId).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto findByIdAndAuthor(Long userId, Long commentId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));

        return commentMapper.toDto(comment);
    }

    @Override
    public List<CommentDto> findAllByEventAndAuthor(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        return commentRepository.findByEventIdAndUserId(eventId, userId).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto create(PostCommentParam param) {
        User user = userRepository.findById(param.userId())
                .orElseThrow(() -> new NotFoundException("User with id=" + param.userId() + " was not found"));

        Event event = eventRepository.findById(param.eventId())
                .orElseThrow(() -> new NotFoundException("Event with id=" + param.eventId() + " was not found"));

        Comment comment = Comment.builder()
                .content(param.comment())
                .user(user)
                .event(event)
                .status(CommentStatus.PENDING)
                .build();

        Comment saved = commentRepository.save(comment);
        return commentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CommentDto update(UpdateCommentParam param) {
        userRepository.findById(param.userId())
                .orElseThrow(() -> new NotFoundException("User with id=" + param.userId() + " was not found"));

        Comment comment = commentRepository.findByIdAndUserId(param.commentId(), param.userId())
                .orElseThrow(() -> new NotFoundException("Comment with id=" + param.commentId() + " was not found"));

        if (comment.getStatus() == CommentStatus.PUBLISHED) {
            throw new ConflictException("Cannot update a published comment");
        }

        comment.setContent(param.comment());
        return commentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long commentId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));

        commentRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getPublishedComments(CommentSearchParams params) {
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(),
                Sort.by("createdAt").descending());

        List<Comment> comments;
        if (params.getEventId() != null) {
            comments = commentRepository.findByStatusAndEventId(CommentStatus.PUBLISHED,
                    params.getEventId(), pageable);
        } else {
            comments = commentRepository.findByStatus(CommentStatus.PUBLISHED, pageable);
        }

        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto getPublishedComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));

        if (comment.getStatus() != CommentStatus.PUBLISHED) {
            throw new NotFoundException("Comment with id=" + commentId + " was not found");
        }

        return commentMapper.toDto(comment);
    }
}
