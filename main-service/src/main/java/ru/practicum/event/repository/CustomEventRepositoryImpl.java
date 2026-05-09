package ru.practicum.event.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.QEvent;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CustomEventRepositoryImpl implements CustomEventRepository {

    private final JPAQueryFactory queryFactory;

    public CustomEventRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<Event> findByPublicCriteria(
            String text, List<Long> categories, Boolean paid,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Boolean onlyAvailable, List<EventState> states,
            int from, int size, String sort) {

        QEvent e = QEvent.event;
        BooleanBuilder where = new BooleanBuilder();

        if (onlyAvailable != null && onlyAvailable) {
            where.and(e.participantLimit.eq(0).or(e.confirmedRequests.lt(e.participantLimit)));
        }

        if (states != null && !states.isEmpty()) {
            where.and(e.state.in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            where.and(e.category.id.in(categories));
        }

        if (paid != null) {
            where.and(e.paid.eq(paid));
        }

        if (text != null && !text.isBlank()) {
            String pattern = "%" + text.toLowerCase() + "%";
            where.and(e.annotation.lower().like(pattern).or(e.description.lower().like(pattern)));
        }

        if (rangeStart != null && rangeEnd != null) {
            where.and(e.eventDate.between(rangeStart, rangeEnd));
        } else if (rangeStart != null) {
            where.and(e.eventDate.goe(rangeStart));
        } else if (rangeEnd != null) {
            where.and(e.eventDate.loe(rangeEnd));
        } else {
            where.and(e.eventDate.after(LocalDateTime.now()));
        }

        return queryFactory.selectFrom(e)
                .leftJoin(e.category).fetchJoin()
                .leftJoin(e.initiator).fetchJoin()
                .where(where)
                .orderBy("VIEWS".equals(sort) ? e.views.desc() : e.eventDate.desc())
                .offset(from)
                .limit(size)
                .fetch();
    }

    @Override
    public List<Event> findByAdminCriteria(
            List<Long> users, List<EventState> states, List<Long> categories,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            int from, int size) {

        QEvent e = QEvent.event;
        BooleanBuilder where = new BooleanBuilder();

        if (users != null && !users.isEmpty()) {
            where.and(e.initiator.id.in(users));
        }

        if (states != null && !states.isEmpty()) {
            where.and(e.state.in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            where.and(e.category.id.in(categories));
        }

        if (rangeStart != null) {
            where.and(e.eventDate.goe(rangeStart));
        }

        if (rangeEnd != null) {
            where.and(e.eventDate.loe(rangeEnd));
        }

        return queryFactory.selectFrom(e)
                .leftJoin(e.category).fetchJoin()
                .leftJoin(e.initiator).fetchJoin()
                .where(where)
                .orderBy(e.id.desc())
                .offset(from)
                .limit(size)
                .fetch();
    }
}
