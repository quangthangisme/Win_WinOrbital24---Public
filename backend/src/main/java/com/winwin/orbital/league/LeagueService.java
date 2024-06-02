package com.winwin.orbital.league;

import com.winwin.orbital.exception.LeagueNotFoundException;
import com.winwin.orbital.exception.ManagerAlreadyInLeagueException;
import com.winwin.orbital.exception.UserNotFoundException;
import com.winwin.orbital.manager.Manager;
import com.winwin.orbital.team.Team;
import com.winwin.orbital.team.TeamDto;
import com.winwin.orbital.user.User;
import com.winwin.orbital.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final UserRepository userRepository;

    @Autowired
    public LeagueService(LeagueRepository leagueRepository, UserRepository userRepository) {
        this.leagueRepository = leagueRepository;
        this.userRepository = userRepository;
    }

    public List<TeamDto> getTeamsInLeague(Long leagueId, UserDetails userDetails) {
        Optional<League> leagueOptional = leagueRepository.findById(leagueId);
        if (leagueOptional.isEmpty()) {
            throw new LeagueNotFoundException("League with id " + leagueId + " not found");
        }
        League league = leagueOptional.get();

        Optional<User> currentUserOptional = userRepository.findByUsername(userDetails.getUsername());
        if (currentUserOptional.isEmpty()) {
            throw new UserNotFoundException("Cannot authenticate current user.");
        }
        User currentUser = currentUserOptional.get();
        Manager currentUserManager = currentUser.getManager();

        if (!isManagerInLeague(currentUserManager, league)) {
            throw new AccessDeniedException("Current manager is not in the league.");
        }

        List<TeamDto> teamsInLeague = league.getTeams().stream()
                .map(TeamDto::new)
                .collect(Collectors.toList());

        return teamsInLeague;
    }

    @Transactional
    public League createLeague(String name, UserDetails userDetails) {
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());

        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("Cannot authenticate current user.");
        }

        Manager manager = userOpt.get().getManager();
        String code = generateUniqueCode();
        League league = new League(name, code);
        league.getTeams().add(createTeamForManager(manager, league));
        return leagueRepository.save(league);
    }

    @Transactional
    public League joinLeague(String code, UserDetails userDetails) {
        Optional<League> leagueOpt = leagueRepository.findByCode(code);
        Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());

        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("Cannot authenticate current user.");
        }

        if (leagueOpt.isEmpty()) {
            throw new LeagueNotFoundException("League not found.");
        }

        Manager manager = userOpt.get().getManager();
        League league = leagueOpt.get();

        if (isManagerInLeague(manager, league)) {
            throw new ManagerAlreadyInLeagueException("Manager is already part of this league.");
        }

        league.getTeams().add(createTeamForManager(manager, league));
        return leagueRepository.save(league);
    }

    private boolean isManagerInLeague(Manager manager, League league) {
        return manager.getTeams().stream().anyMatch(team -> team.getLeague().equals(league));
    }

    private Team createTeamForManager(Manager manager, League league) {
        Team team = new Team(manager, league);
        return team;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = League.generateRandomCode();
        } while (leagueRepository.findByCode(code).isPresent());
        return code;
    }

}
