package com.winwin.orbital.league;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {

    Optional<League> findByCode(String code);

    @Query("SELECT l FROM League l " +
            "WHERE l.draftStartTime <= :currentTime " +
            "AND l.draftStartTime > :previousTime " +
            "AND l.status = 'waiting for draft'")
    List<League> findLeaguesWithDraftStarting(@Param("currentTime") LocalDateTime currentTime,
                                              @Param("previousTime") LocalDateTime previousTime);
}