package com.winwin.orbital.manager;

import com.winwin.orbital.exception.UserNotFoundException;
import com.winwin.orbital.league.LeagueDto;
import com.winwin.orbital.team.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManagerService {

    private final ManagerRepository managerRepository;

    @Autowired
    public ManagerService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    public List<LeagueDto> getCurrentUserLeagues(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return managerRepository.findByUserUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."))
                .getTeams().stream()
                .map(Team::getLeague)
                .map(LeagueDto::new)
                .collect(Collectors.toList());
    }

}