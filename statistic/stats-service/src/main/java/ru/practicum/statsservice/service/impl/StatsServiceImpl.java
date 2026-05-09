package ru.practicum.statsservice.service.impl;

import dto.EndpointHit;
import dto.ViewStats;
import lombok.AllArgsConstructor;
import model.Visit;
import org.springframework.stereotype.Service;
import ru.practicum.statsservice.repository.VisitRepository;
import ru.practicum.statsservice.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;
import ru.practicum.statsservice.exception.BadRequestException;

@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final VisitRepository visitRepository;

    @Override
    public void saveHit(EndpointHit endpointHit) {
        Visit visit = endpointHit.toVisit();
        Visit saved = visitRepository.save(visit);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("Start must be before end");
        }
        List<Object[]> results = unique
                ? visitRepository.findStatsUnique(start, end, uris)
                : visitRepository.findStats(start, end, uris);

        return results.stream()
                .map(row -> ViewStats.builder()
                        .app((String) row[0])
                        .uri((String) row[1])
                        .hits((Long) row[2])
                        .build())
                .toList();
    }
}
