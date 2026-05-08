package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, CustomEventRepository {

    Page<Event> findByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    List<Event> findByIdIn(List<Long> eventIds);

    boolean existsByCategoryId(Long categoryId);

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.id = :eventId AND e.initiator.id = :userId")
    boolean existsByIdAndInitiatorId(@Param("eventId") Long eventId, @Param("userId") Long userId);

    @Query("SELECT e.views FROM Event e WHERE e.id = :eventId")
    Long findViewsById(@Param("eventId") Long eventId);

    @Query("SELECT e.confirmedRequests FROM Event e WHERE e.id = :eventId")
    Integer findConfirmedRequestsById(@Param("eventId") Long eventId);

    List<Event> findByCategoryId(Long categoryId);
}