package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByRequesterId(Long requesterId);
    
    List<ParticipationRequest> findAllByEventId(Long eventId);
    
    List<ParticipationRequest> findAllByEventIdAndIdIn(Long eventId, List<Long> requestIds);
    
    Long countByEventIdAndStatus(Long eventId, ParticipationRequest.RequestStatus status);
    
    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);
    
    List<ParticipationRequest> findAllByEventIdAndStatus(Long eventId, ParticipationRequest.RequestStatus status);
    
    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long requesterId);
}