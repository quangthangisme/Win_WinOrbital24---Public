package com.winwin.orbital.league;

import com.winwin.orbital.exception.*;
import com.winwin.orbital.gameweek.GameweekComponent;
import com.winwin.orbital.manager.Manager;
import com.winwin.orbital.manager.ManagerDto;
import com.winwin.orbital.manager.ManagerRepository;
import com.winwin.orbital.scoringrule.ScoringRule;
import com.winwin.orbital.scoringrule.ScoringRuleDto;
import com.winwin.orbital.team.Team;
import com.winwin.orbital.team.TeamDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final ManagerRepository managerRepository;
    private final GameweekComponent gameweekComponent;

    @Autowired
    public LeagueService(LeagueRepository leagueRepository,
                         ManagerRepository managerRepository,
                         GameweekComponent gameweekComponent) {
        this.leagueRepository = leagueRepository;
        this.managerRepository = managerRepository;
        this.gameweekComponent = gameweekComponent;
    }

    public String getLeagueName(Long leagueId, UserDetails userDetails) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        if (!isManagerInLeague(currentUserManager, league)) {
            throw new AccessDeniedException("Current manager is not in the league.");
        }

        return league.getName();
    }

    public ScoringRuleDto getScoringRule(Long leagueId, UserDetails userDetails) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        if (!isManagerInLeague(currentUserManager, league)) {
            throw new AccessDeniedException("Current manager is not in the league.");
        }

        return new ScoringRuleDto(league.getScoringRule());
    }

    public Map<String, Integer> getPowerUps(Long leagueId, UserDetails userDetails) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        if (!isManagerInLeague(currentUserManager, league)) {
            throw new AccessDeniedException("Current manager is not in the league.");
        }

        return league.getPowerUps();
    }

    public long getMaxPlayersSameClub(Long leagueId, UserDetails userDetails) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        if (!isManagerInLeague(currentUserManager, league)) {
            throw new AccessDeniedException("Current manager is not in the league.");
        }

        return league.getMaxNumberOfPlayersFromAClub();
    }

    public DraftInfoDto getDraftInfo(Long leagueId, UserDetails userDetails) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        if (!isManagerInLeague(currentUserManager, league)) {
            throw new AccessDeniedException("Current manager is not in the league.");
        }

        return new DraftInfoDto(league.getStatus(), league.getDraftStartTime(), new ManagerDto(league.getAdmin()));
    }

    public List<TeamDto> getTeamsInLeague(Long leagueId, UserDetails userDetails) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        if (!isManagerInLeague(currentUserManager, league)) {
            throw new AccessDeniedException("Current manager is not in the league.");
        }

        return league.getTeams().stream()
                .map(TeamDto::new)
                .collect(Collectors.toList());
    }

    public LeagueStatusDto getStatus(Long leagueId, UserDetails userDetails) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        if (!isManagerInLeague(currentUserManager, league)) {
            throw new AccessDeniedException("Current manager is not in the league.");
        }

        return new LeagueStatusDto(leagueId, league.getStatus()
                , gameweekComponent.getCurrentGameweek(), gameweekComponent.getCurrentSeason());
    }

    @Transactional
    public League createLeague(LeagueCreationRequestDto leagueCreationRequestDto, UserDetails userDetails) {
        Manager manager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        if (leagueCreationRequestDto.getTeamName().trim().isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be blank.");
        }

        if (leagueCreationRequestDto.getMaxPlayersFromSameClub() < 1) {
            throw new IllegalArgumentException("Maximum number of players from the same club must be at least 1.");
        }


        String code = generateUniqueCode();
        League league = new League(leagueCreationRequestDto.getName(), code);

        Map<String, Integer> powerUps = leagueCreationRequestDto.getPowerUps();
        Set<String> validKeys = Set.of("bboost", "cx3");

        for (String key : validKeys) {
            if (!powerUps.containsKey(key)) {
                throw new IllegalArgumentException("Missing powerup: " + key);
            }
            int value = powerUps.get(key);
            if (value < 0 || value > 10) {
                throw new IllegalArgumentException("Powerup count must be between 0 and 10 for " + key);
            }
        }

        league.setPowerUps(powerUps);
        Team team = createTeamForManager(manager, league);
        team.setName(leagueCreationRequestDto.getTeamName());
        league.getTeams().add(team);

        validateScoringRule(leagueCreationRequestDto.getScoringRule());
        ScoringRule scoringRule = new ScoringRule(leagueCreationRequestDto.getScoringRule(), league);
        league.setScoringRule(scoringRule);
        league.setStatus("created");
        league.setAdmin(manager);
        league.setMaxNumberOfPlayersFromAClub(leagueCreationRequestDto.getMaxPlayersFromSameClub());

        return leagueRepository.save(league);
    }

    @Transactional
    public League joinLeague(String code, String teamName, UserDetails userDetails) {
        Manager manager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findByCode(code)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        if (isManagerInLeague(manager, league)) {
            throw new ManagerAlreadyInLeagueException("Manager is already part of this league.");
        }

        if (!Arrays.asList("created", "waiting for draft").contains(league.getStatus().toLowerCase())) {
            throw new IllegalStateException("Cannot join league with status " + league.getStatus());
        }

        if (teamName == null || teamName.trim().isEmpty()) {
            throw new IllegalArgumentException("Team name is required.");
        }

        if (league.getTeams().size() >= 16) {
            throw new IllegalStateException("League has reached maximum capacity of 16 teams.");
        }

        Team team = createTeamForManager(manager, league);
        team.setName(teamName);
        league.getTeams().add(team);

        return leagueRepository.save(league);
    }

    @Transactional
    public void submitDraftSettings(Long leagueId, DraftSettingsDto draftSettings, UserDetails userDetails) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        if (!league.getAdmin().equals(currentUserManager)) {
            throw new UnauthorizedException("You are not authorized to submit draft settings for this league.");
        }

        if (draftSettings.getDraftStartTime().isBefore(LocalDateTime.now())) {
            throw new InvalidDraftSettingsException("Draft start time must be in the future.");
        }

        if (draftSettings.getTurnDuration() < 5) {
            throw new InvalidDraftSettingsException("Turn duration must be at least 5 seconds.");
        }

        System.out.println(league.getStatus());
        if (!"created".equals(league.getStatus())) {
            throw new InvalidDraftSettingsException("You are not authorized to submit draft settings for this league.");
        }

        league.setDraftStartTime(draftSettings.getDraftStartTime());
        league.setDraftTurnDurationMilliseconds(draftSettings.getTurnDuration() * 1000);
        league.setStatus("waiting for draft");
        leagueRepository.save(league);
    }

    private boolean isManagerInLeague(Manager manager, League league) {
        return manager.getTeams().stream().anyMatch(team -> team.getLeague().equals(league));
    }

    private Team createTeamForManager(Manager manager, League league) {
        return new Team(manager, league);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = League.generateRandomCode();
        } while (leagueRepository.findByCode(code).isPresent());
        return code;
    }

    private void validateScoringRule(ScoringRuleDto scoringRuleDto) {
        if (scoringRuleDto.getFor60Mins() < -10 || scoringRuleDto.getFor60Mins() > 10 ||
                scoringRuleDto.getForOver60Mins() < -10 || scoringRuleDto.getForOver60Mins() > 10 ||
                scoringRuleDto.getForGkDfGoal() < -10 || scoringRuleDto.getForGkDfGoal() > 10 ||
                scoringRuleDto.getForMdGoal() < -10 || scoringRuleDto.getForMdGoal() > 10 ||
                scoringRuleDto.getForFwGoal() < -10 || scoringRuleDto.getForFwGoal() > 10 ||
                scoringRuleDto.getForAssist() < -10 || scoringRuleDto.getForAssist() > 10 ||
                scoringRuleDto.getForGkDfCleanSheet() < -10 || scoringRuleDto.getForGkDfCleanSheet() > 10 ||
                scoringRuleDto.getForMdCleanSheet() < -10 || scoringRuleDto.getForMdCleanSheet() > 10 ||
                scoringRuleDto.getFor3GkSaves() < -10 || scoringRuleDto.getFor3GkSaves() > 10 ||
                scoringRuleDto.getForPkSaved() < -10 || scoringRuleDto.getForPkSaved() > 10 ||
                scoringRuleDto.getForPkMissed() < -10 || scoringRuleDto.getForPkMissed() > 10 ||
                scoringRuleDto.getFor2GoalsConceded() < -10 || scoringRuleDto.getFor2GoalsConceded() > 10 ||
                scoringRuleDto.getForYellowCard() < -10 || scoringRuleDto.getForYellowCard() > 10 ||
                scoringRuleDto.getForRedCard() < -10 || scoringRuleDto.getForRedCard() > 10 ||
                scoringRuleDto.getForOwnGoal() < -10 || scoringRuleDto.getForOwnGoal() > 10) {
            throw new IllegalArgumentException("All scoring rule values must be between -10 and 10.");
        }
    }
}
