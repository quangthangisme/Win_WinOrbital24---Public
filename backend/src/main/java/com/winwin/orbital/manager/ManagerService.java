package com.winwin.orbital.manager;

import com.winwin.orbital.exception.UserNotFoundException;
import com.winwin.orbital.league.LeagueDto;
import com.winwin.orbital.user.UserRepository;
import com.winwin.orbital.team.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManagerService {

    private final UserRepository userRepository;
    private final ManagerRepository managerRepository;

    @Autowired
    public ManagerService(UserRepository userRepository, ManagerRepository managerRepository) {
        this.userRepository = userRepository;
        this.managerRepository = managerRepository;
    }

    public List<LeagueDto> getCurrentUserLeagues(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return userRepository.findByUsername(username)
                .flatMap(user -> managerRepository.findByUserId(user.getId()))
                .map(manager -> manager.getTeams().stream()
                        .map(Team::getLeague)
                        .map(LeagueDto::new)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));
    }

}