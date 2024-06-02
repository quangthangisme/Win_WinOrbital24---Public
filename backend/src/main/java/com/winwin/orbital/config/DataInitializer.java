package com.winwin.orbital.config;

import com.winwin.orbital.user.User;
import com.winwin.orbital.user.UserRepository;
import com.winwin.orbital.manager.Manager;
import com.winwin.orbital.manager.ManagerRepository;
import com.winwin.orbital.league.League;
import com.winwin.orbital.league.LeagueRepository;
import com.winwin.orbital.team.Team;
import com.winwin.orbital.team.TeamRepository;
import com.winwin.orbital.player.Player;
import com.winwin.orbital.player.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create users
        User userA = new User();
        userA.setUsername("userA");
        userA.setEmail("userA@example.com");
        userA.setPassword(passwordEncoder.encode("admin"));
        userA.setRole("USER");
        userA = userRepository.save(userA);

        User userB = new User();
        userB.setUsername("userB");
        userB.setEmail("userB@example.com");
        userB.setPassword(passwordEncoder.encode("admin"));
        userB.setRole("USER");
        userB = userRepository.save(userB);

        User userC = new User();
        userC.setUsername("userC");
        userC.setEmail("userC@example.com");
        userC.setPassword(passwordEncoder.encode("admin"));
        userC.setRole("USER");
        userC = userRepository.save(userC);

        // Create managers
        Manager managerA = new Manager();
        managerA.setUser(userA);
        managerA = managerRepository.save(managerA);

        Manager managerB = new Manager();
        managerB.setUser(userB);
        managerB = managerRepository.save(managerB);

        Manager managerC = new Manager();
        managerC.setUser(userC);
        managerC = managerRepository.save(managerC);

        // Create leagues
        League leagueX = new League();
        leagueX.setName("League X");
        leagueX.setCode("XXXXXXXX");
        leagueX = leagueRepository.save(leagueX);

        League leagueY = new League();
        leagueY.setName("League Y");
        leagueY.setCode("YYYYYYYY");
        leagueY = leagueRepository.save(leagueY);

        League leagueZ = new League();
        leagueZ.setName("League Z");
        leagueZ.setCode("ZZZZZZZZ");
        leagueZ = leagueRepository.save(leagueZ);

        // Create player K
        Player playerK = new Player();
        playerK.setFirstName("Player");
        playerK.setLastName("K");
        playerK = playerRepository.save(playerK);

        // Create teams and assign to managers and leagues
        Team teamA1 = new Team();
        teamA1.setName("Team A1");
        teamA1.setManager(managerA);
        teamA1.setLeague(leagueX);
        teamA1.setPlayers(new HashSet<>(Arrays.asList(playerK)));
        teamA1 = teamRepository.save(teamA1);

        Team teamA2 = new Team();
        teamA2.setName("Team A2");
        teamA2.setManager(managerA);
        teamA2.setLeague(leagueY);
        teamA2.setPlayers(new HashSet<>(Arrays.asList(playerK)));
        teamA2 = teamRepository.save(teamA2);

        Team teamB1 = new Team();
        teamB1.setName("Team B1");
        teamB1.setManager(managerB);
        teamB1.setLeague(leagueX);
        teamB1.setPlayers(new HashSet<>(Arrays.asList(playerK)));
        teamB1 = teamRepository.save(teamB1);

        Team teamB2 = new Team();
        teamB2.setName("Team B2");
        teamB2.setManager(managerB);
        teamB2.setLeague(leagueY);
        teamB2.setPlayers(new HashSet<>(Arrays.asList(playerK)));
        teamB2 = teamRepository.save(teamB2);

        Team teamB3 = new Team();
        teamB3.setName("Team B3");
        teamB3.setManager(managerB);
        teamB3.setLeague(leagueZ);
        teamB3.setPlayers(new HashSet<>(Arrays.asList(playerK)));
        teamB3 = teamRepository.save(teamB3);

        Team teamC1 = new Team();
        teamC1.setName("Team C1");
        teamC1.setManager(managerC);
        teamC1.setLeague(leagueY);
        teamC1.setPlayers(new HashSet<>(Arrays.asList(playerK)));
        teamC1 = teamRepository.save(teamC1);

        Team teamC2 = new Team();
        teamC2.setName("Team C2");
        teamC2.setManager(managerC);
        teamC2.setLeague(leagueZ);
        teamC2.setPlayers(new HashSet<>(Arrays.asList(playerK)));
        teamC2 = teamRepository.save(teamC2);
    }
}
