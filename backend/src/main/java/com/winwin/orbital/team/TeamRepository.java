package com.winwin.orbital.team;

import com.winwin.orbital.league.League;
import com.winwin.orbital.manager.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByManagerAndLeague(Manager manager, League league);
}
