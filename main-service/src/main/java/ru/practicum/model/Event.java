package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "annotation", nullable = false, length = 2000)
    private String annotation;
    
    @Column(name = "description", nullable = false, length = 7000)
    private String description;
    
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;
    
    @Embedded
    private Location location;
    
    @Column(name = "paid", nullable = false)
    private Boolean paid;
    
    @Column(name = "participant_limit")
    private Integer participantLimit;
    
    @Column(name = "request_moderation")
    private Boolean requestModeration;
    
    @Column(name = "title", nullable = false, length = 120)
    private String title;
    
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private EventState state;
    
    public enum EventState {
        PENDING, PUBLISHED, CANCELED
    }
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        @Column(name = "lat")
        private Float lat;
        
        @Column(name = "lon")
        private Float lon;
    }
}