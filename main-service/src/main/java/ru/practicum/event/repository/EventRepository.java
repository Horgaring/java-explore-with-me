package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    @Query("SELECT e FROM Event e WHERE " +
            "(:users IS NULL OR e.initiator.id IN :users) AND " +
            "(:states IS NULL OR e.state IN :states) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(:rangeStart IS NULL OR e.eventDate >= :rangeStart) AND " +
            "(:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)")
    Page<Event> findByAdminCriteria(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    @Query("SELECT e FROM Event e WHERE " +
            "(:onlyAvailable = false OR " +
            "(e.participantLimit = 0 OR e.confirmedRequests < e.participantLimit)) AND " +
            "(:states IS NULL OR e.state IN :states) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(:paid IS NULL OR e.paid = :paid) AND " +
            "(:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) AND " +
            "((:rangeStart IS NULL AND :rangeEnd IS NULL AND e.eventDate > CURRENT_TIMESTAMP) OR " +
            "(:rangeStart IS NOT NULL AND e.eventDate >= :rangeStart) OR " +
            "(:rangeEnd IS NOT NULL AND e.eventDate <= :rangeEnd))")
    Page<Event> findByPublicCriteria(
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable,
            @Param("states") List<EventState> states,
            Pageable pageable);

    List<Event> findByIdIn(List<Long> eventIds);

    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.id = :eventId AND e.initiator.id = :userId")
    boolean existsByIdAndInitiatorId(@Param("eventId") Long eventId, @Param("userId") Long userId);

    @Query("SELECT e.views FROM Event e WHERE e.id = :eventId")
    Long findViewsById(@Param("eventId") Long eventId);

    @Query("SELECT e.confirmedRequests FROM Event e WHERE e.id = :eventId")
    Integer findConfirmedRequestsById(@Param("eventId") Long eventId);
}