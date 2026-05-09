package ru.practicum.event.repository;

import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomEventRepository {

    List<Event> findByPublicCriteria(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            List<EventState> states,
            int from,
            int size,
            String sort);


    List<Event> findByAdminCriteria(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size);
}
