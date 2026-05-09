package ru.practicum.statsservice.repository;

import model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface VisitRepository extends JpaRepository<Visit, UUID> {

    @Query("SELECT v.app AS app, v.uri AS uri, COUNT(v) AS hits " +
           "FROM Visit v " +
           "WHERE v.timestamp BETWEEN :start AND :end " +
           "AND (:uris IS NULL OR v.uri IN :uris) " +
           "GROUP BY v.app, v.uri " +
           "ORDER BY hits DESC")
    List<Object[]> findStats(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             List<String> uris);

    @Query("SELECT v.app AS app, v.uri AS uri, COUNT(DISTINCT v.ip) AS hits " +
           "FROM Visit v " +
           "WHERE v.timestamp BETWEEN :start AND :end " +
           "AND (:uris IS NULL OR v.uri IN :uris) " +
           "GROUP BY v.app, v.uri " +
           "ORDER BY hits DESC")
    List<Object[]> findStatsUnique(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end, List<String> uris);
}
