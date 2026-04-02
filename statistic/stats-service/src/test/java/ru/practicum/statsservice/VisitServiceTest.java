package ru.practicum.statsservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.statsservice.repository.VisitRepository;
import ru.practicum.statsservice.service.impl.StatsServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VisitServiceTest {
    @Mock
    private VisitRepository repository;

    @InjectMocks
    private StatsServiceImpl service;

    @Test
    void shouldCallFindStatsUniqueWhenGetStats() {
        when(repository.findStatsUnique(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(List.class)
        )).thenReturn(List.of());

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();
        List<String> params = List.of("param1");

        service.getStats(start, end, params, true);

        verify(repository).findStatsUnique(
                eq(start),
                eq(end),
                eq(params)
        );
    }

    @Test
    void shouldCallFindStatsWhenGetStats() {
        when(repository.findStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(List.class)
        )).thenReturn(List.of());

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now();
        List<String> params = List.of("param1");

        service.getStats(start, end, params, false);

        verify(repository).findStats(
                eq(start),
                eq(end),
                eq(params)
        );
    }
}
