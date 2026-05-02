package ru.practicum.event.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

@Component
public class EventMapper {

    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    public EventMapper(CategoryMapper categoryMapper, UserMapper userMapper) {
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
    }

    public Event toEvent(NewEventDto newEventDto, Category category, User initiator) {
        if (newEventDto == null) {
            return null;
        }

        Location location = newEventDto.getLocation();
        if (location == null) {
            location = Location.builder()
                    .lat(0.0f)
                    .lon(0.0f)
                    .build();
        }

        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .initiator(initiator)
                .lat(location.getLat())
                .lon(location.getLon())
                .paid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false)
                .participantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0)
                .requestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true)
                .title(newEventDto.getTitle())
                .build();
    }

    public EventFullDto toEventFullDto(Event event) {
        if (event == null) {
            return null;
        }

        Location location = Location.builder()
                .lat(event.getLat())
                .lon(event.getLon())
                .build();

        CategoryDto categoryDto = categoryMapper.toCategoryDto(event.getCategory());
        UserShortDto initiatorDto = userMapper.toUserShortDto(event.getInitiator());

        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryDto)
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(initiatorDto)
                .location(location)
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public EventShortDto toEventShortDto(Event event) {
        if (event == null) {
            return null;
        }

        CategoryDto categoryDto = categoryMapper.toCategoryDto(event.getCategory());
        UserShortDto initiatorDto = userMapper.toUserShortDto(event.getInitiator());

        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryDto)
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .initiator(initiatorDto)
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }
}