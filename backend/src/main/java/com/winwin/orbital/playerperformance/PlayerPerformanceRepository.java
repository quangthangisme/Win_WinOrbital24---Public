package com.winwin.orbital.playerperformance;

import com.winwin.orbital.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerPerformanceRepository extends JpaRepository<PlayerPerformance, Long> {
    Optional<PlayerPerformance> findByPlayerAndGameweekAndSeason(Player player, int gameweek, String season);
}
