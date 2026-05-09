package ru.practicum.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.model.CommentStatus;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>,
        QuerydslPredicateExecutor<Comment>, JpaSpecificationExecutor<Comment> {

    List<Comment> findByUserId(Long userId);

    Optional<Comment> findByIdAndUserId(Long id, Long userId);

    List<Comment> findByEventId(Long eventId);

    List<Comment> findByEventIdAndUserId(Long eventId, Long userId);

    List<Comment> findByStatusAndEventId(CommentStatus status, Long eventId, Pageable pageable);

    List<Comment> findByStatus(CommentStatus status, Pageable pageable);
}