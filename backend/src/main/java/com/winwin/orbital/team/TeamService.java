package com.winwin.orbital.team;

import com.winwin.orbital.exception.LeagueNotFoundException;
import com.winwin.orbital.exception.TeamNotFoundException;
import com.winwin.orbital.exception.UserNotFoundException;
import com.winwin.orbital.gameweek.GameweekComponent;
import com.winwin.orbital.league.League;
import com.winwin.orbital.league.LeagueRepository;
import com.winwin.orbital.lineup.Lineup;
import com.winwin.orbital.lineup.LineupService;
import com.winwin.orbital.manager.Manager;
import com.winwin.orbital.manager.ManagerRepository;
import com.winwin.orbital.player.Player;
import com.winwin.orbital.player.PlayerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final ManagerRepository managerRepository;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final LineupService lineupService;
    private final GameweekComponent gameweekComponent;

    @Autowired
    public TeamService(ManagerRepository managerRepository,
                       TeamRepository teamRepository,
                       LeagueRepository leagueRepository,
                       LineupService lineupService,
                       GameweekComponent gameweekComponent) {
        this.managerRepository = managerRepository;
        this.teamRepository = teamRepository;
        this.leagueRepository = leagueRepository;
        this.lineupService = lineupService;
        this.gameweekComponent = gameweekComponent;
    }

    public List<PlayerDto> getCurrentPlayers(UserDetails userDetails, Long leagueId) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        Team team = teamRepository.findByManagerAndLeague(currentUserManager, league)
                .orElseThrow(() -> new AccessDeniedException("No team found for the current manager in the specified league."));

        Set<Player> currentPlayers = team.getCurrentPlayers();

        return currentPlayers.stream()
                .map(PlayerDto::new)
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getCurrentTeamRemainingPowerups(UserDetails userDetails, Long leagueId) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        Team team = teamRepository.findByManagerAndLeague(currentUserManager, league)
                .orElseThrow(() -> new AccessDeniedException("No team found for the current manager in the specified league."));

        return getRemainingPowerups(team);
    }

    public int getPoints(long teamId, UserDetails userDetails) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team not found."));

        if (!userCanViewTeam(team, userDetails)) {
            throw new AccessDeniedException("Manager is not in the team's league.");
        }

        return calculatePoints(team);
    }

    public List<TeamDataDto> getAllTeamsData(long leagueId, UserDetails userDetails) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        teamRepository.findByManagerAndLeague(currentUserManager, league)
                .orElseThrow(() -> new AccessDeniedException("Manager is not in league."));

        return league.getTeams().stream().map(team -> {
            TeamDataDto teamDataDto = new TeamDataDto();
            teamDataDto.setId(team.getId());
            teamDataDto.setTeamName(team.getName());
            teamDataDto.setManagerId(team.getManager().getId());
            teamDataDto.setManagerUsername(team.getManager().getUser().getUsername());
            teamDataDto.setLeagueId(team.getLeague().getId());
            teamDataDto.setLeagueName(team.getLeague().getName());
            teamDataDto.setPoints(calculatePoints(team));
            teamDataDto.setCurrentPlayers(team.getCurrentPlayers().stream()
                    .map(PlayerDto::new)
                    .collect(Collectors.toSet()));
            teamDataDto.setPastLineups(lineupService.getPastLineups(team));
            return teamDataDto;
        }).toList();

    }

    private int calculatePoints(Team team) {
        int currentGameweek = gameweekComponent.getCurrentGameweek();
        String currentSeason = gameweekComponent.getCurrentSeason();

        int points = 0;
        for (int gameweek = 1; gameweek < currentGameweek; gameweek++) {
            Optional<Lineup> lineupOpt = getLineupByTeamandGameweekAndSeason(team, gameweek, currentSeason);
            if (lineupOpt.isPresent()) {
                points += lineupService.calculatePoints(lineupOpt.get());
            }
        }

        return points;
    }

    private Map<String, Integer> getRemainingPowerups(Team team) {
        Map<String, Integer> availablePowerups = team.getLeague().getPowerUps();
        int currentGameweek = gameweekComponent.getCurrentGameweek();
        String currentSeason = gameweekComponent.getCurrentSeason();

        Map<Integer, Lineup> latestLineupsByGameweek = team.getLineupHistory().stream()
                .filter(lineup -> lineup.getSeason().equals(currentSeason) && lineup.getGameweek() < currentGameweek)
                .collect(Collectors.toMap(
                        Lineup::getGameweek,
                        lineup -> lineup,
                        (lineup1, lineup2) -> lineup1.getSubmittedAt().isAfter(lineup2.getSubmittedAt()) ? lineup1 : lineup2
                ));

        long powerupsUsedBBoost = latestLineupsByGameweek.values().stream()
                .filter(lineup -> "bboost".equals(lineup.getPowerup()))
                .count();

        long powerupsUsedCx3 = latestLineupsByGameweek.values().stream()
                .filter(lineup -> "cx3".equals(lineup.getPowerup()))
                .count();

        Map<String, Integer> remainingPowerups = new HashMap<>();
        remainingPowerups.put("bboost", availablePowerups.getOrDefault("bboost", 0) - (int) powerupsUsedBBoost);
        remainingPowerups.put("cx3", availablePowerups.getOrDefault("cx3", 0) - (int) powerupsUsedCx3);

        return remainingPowerups;
    }

    private Optional<Lineup> getLineupByTeamandGameweekAndSeason(Team team, int gameweek, String season) {
        Set<Lineup> lineupHistory = team.getLineupHistory();
        List<Lineup> filteredLineups = lineupHistory.stream()
                .filter(lineup -> lineup.getSeason().equals(season) && lineup.getGameweek() == gameweek)
                .toList();

        return filteredLineups.stream()
                .max(Comparator.comparing(Lineup::getSubmittedAt));
    }

    private boolean userCanViewTeam(Team team, UserDetails userDetails) {
        String username = userDetails.getUsername();
        return managerRepository.findByUserUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."))
                .getTeams().stream()
                .map(Team::getLeague)
                .anyMatch(league -> league.equals(team.getLeague()));
    }

}
