package ru.practicum.event.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + newEventDto.getCategory() + " was not found"));

        if (newEventDto.getEventDate() != null &&
                newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours from now");
        }

        Event event = eventMapper.toEvent(newEventDto, category, initiator);
        event.setState(EventState.PENDING);

        try {
            Event savedEvent = eventRepository.save(event);
            return eventMapper.toEventFullDto(savedEvent);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Failed to create event");
        }
    }

    @Override
    public List<EventShortDto> getEventsByInitiator(Long userId, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").descending());
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByInitiator(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        return eventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto updateEventByInitiator(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));


        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }


        if (updateRequest.getEventDate() != null &&
                updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least 2 hours from now");
        }

        updateEventFields(event, updateRequest);


        if (updateRequest.getStateAction() == UpdateEventUserRequest.StateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        } else if (updateRequest.getStateAction() == UpdateEventUserRequest.StateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }

        try {
            Event updatedEvent = eventRepository.save(event);
            return eventMapper.toEventFullDto(updatedEvent);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Failed to update event");
        }
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new IllegalArgumentException("Range start must be before range end");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").descending());
        List<Event> events = eventRepository.findByAdminCriteria(users, states, categories, rangeStart, rangeEnd, pageable)
                .getContent();

        return events.stream()
                .map(eventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));


        if (updateRequest.getEventDate() != null && event.getPublishedOn() != null) {
            if (updateRequest.getEventDate().isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConflictException("Event date must be at least 1 hour after publication");
            }
        }


        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == UpdateEventAdminRequest.StateAction.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish event because it's not in PENDING state");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateRequest.getStateAction() == UpdateEventAdminRequest.StateAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject published event");
                }
                event.setState(EventState.CANCELED);
            }
        }

        updateEventFields(event, updateRequest);

        try {
            Event updatedEvent = eventRepository.save(event);
            return eventMapper.toEventFullDto(updatedEvent);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Failed to update event");
        }
    }

    @Override
    public List<EventShortDto> getEventsByPublic(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            String sort,
            Integer from,
            Integer size) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new IllegalArgumentException("Range start must be before range end");
        }

        Sort sorting = Sort.by("eventDate").descending();
        if ("VIEWS".equals(sort)) {
            sorting = Sort.by("views").descending();
        }

        Pageable pageable = PageRequest.of(from / size, size, sorting);
        List<EventState> publishedState = List.of(EventState.PUBLISHED);

        List<Event> events = eventRepository.findByPublicCriteria(
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, publishedState, pageable
        ).getContent();

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByPublic(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }


        event.setViews(event.getViews() + 1);
        eventRepository.save(event);

        return eventMapper.toEventFullDto(event);
    }

    @Override
    public void incrementViews(Long eventId, int increment) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        event.setViews(event.getViews() + increment);
        eventRepository.save(event);
    }

    @Override
    public void incrementConfirmedRequests(Long eventId, int increment) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        event.setConfirmedRequests(event.getConfirmedRequests() + increment);
        eventRepository.save(event);
    }

    private void updateEventFields(Event event, UpdateEventAdminRequest updateRequest) {
        updateEventFieldsCommon(event, updateRequest);
    }

    private void updateEventFields(Event event, UpdateEventUserRequest updateRequest) {
        updateEventFieldsCommon(event, updateRequest);
    }

    private void updateEventFieldsCommon(Event event, Object updateRequest) {
        if (updateRequest instanceof UpdateEventAdminRequest adminRequest) {
            updateFromAdminRequest(event, adminRequest);
        } else if (updateRequest instanceof UpdateEventUserRequest userRequest) {
            updateFromUserRequest(event, userRequest);
        }
    }

    private void updateFromAdminRequest(Event event, UpdateEventAdminRequest request) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + request.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLat(request.getLocation().getLat());
            event.setLon(request.getLocation().getLon());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }

    private void updateFromUserRequest(Event event, UpdateEventUserRequest request) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + request.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLat(request.getLocation().getLat());
            event.setLon(request.getLocation().getLon());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }
}