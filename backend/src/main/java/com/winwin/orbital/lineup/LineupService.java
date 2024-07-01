package com.winwin.orbital.lineup;

import com.winwin.orbital.exception.*;
import com.winwin.orbital.gameweek.GameweekComponent;
import com.winwin.orbital.league.League;
import com.winwin.orbital.league.LeagueRepository;
import com.winwin.orbital.manager.Manager;
import com.winwin.orbital.player.Player;
import com.winwin.orbital.player.PlayerDto;
import com.winwin.orbital.player.PlayerRepository;
import com.winwin.orbital.playerperformance.PlayerPerformance;
import com.winwin.orbital.playerperformance.PlayerPerformanceRepository;
import com.winwin.orbital.playerperformance.PlayerPerformanceService;
import com.winwin.orbital.team.Team;
import com.winwin.orbital.team.TeamRepository;
import com.winwin.orbital.manager.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LineupService {

    private final ManagerRepository managerRepository;
    private final TeamRepository teamRepository;
    private final LineupRepository lineupRepository;
    private final LeagueRepository leagueRepository;
    private final PlayerRepository playerRepository;
    private final PlayerPerformanceRepository playerPerformanceRepository;
    private final PlayerPerformanceService playerPerformanceService;
    private final LineupConverter lineupConverter;
    private final GameweekComponent gameweekComponent;

    @Autowired
    public LineupService(ManagerRepository managerRepository,
                         TeamRepository teamRepository,
                         LineupRepository lineupRepository,
                         LeagueRepository leagueRepository,
                         PlayerRepository playerRepository,
                         PlayerPerformanceRepository playerPerformanceRepository,
                         PlayerPerformanceService playerPerformanceService,
                         LineupConverter lineupConverter,
                         GameweekComponent gameweekComponent) {
        this.managerRepository = managerRepository;
        this.teamRepository = teamRepository;
        this.lineupRepository = lineupRepository;
        this.leagueRepository = leagueRepository;
        this.playerRepository = playerRepository;
        this.playerPerformanceRepository = playerPerformanceRepository;
        this.playerPerformanceService = playerPerformanceService;
        this.lineupConverter = lineupConverter;
        this.gameweekComponent = gameweekComponent;
    }

    public LineupDto getMyLineupHistory(Long leagueId, int requestedGameweek, UserDetails userDetails) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        Team team = teamRepository.findByManagerAndLeague(currentUserManager, league)
                .orElseThrow(() -> new AccessDeniedException("No team found for the current manager in the specified league."));

        Set<Lineup> lineupHistory = team.getLineupHistory();

        int currentGameweek = gameweekComponent.getCurrentGameweek();
        String currentSeason = gameweekComponent.getCurrentSeason();

        if (requestedGameweek >= currentGameweek) {
            throw new LineupNotFoundException("Lineup not found for given team and gameweek");
        }

        List<Lineup> filteredLineups = lineupHistory.stream()
                .filter(lineup -> lineup.getSeason().equals(currentSeason) && lineup.getGameweek() == requestedGameweek)
                .toList();

        Optional<Lineup> finalLineupOpt = filteredLineups.stream()
                .max(Comparator.comparing(Lineup::getSubmittedAt));

        if (finalLineupOpt.isEmpty()) {
            throw new LineupNotFoundException("Lineup not found for given team and gameweek");
        }

        return lineupConverter.toDto(finalLineupOpt.get());

    }

    public LineupDto getCurrentLineup(Long leagueId, UserDetails userDetails) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        Team team = teamRepository.findByManagerAndLeague(currentUserManager, league)
                .orElseThrow(() -> new AccessDeniedException("No team found for the current manager in the specified league."));

        int currentGameweek = gameweekComponent.getCurrentGameweek();
        String currentSeason = gameweekComponent.getCurrentSeason();

        Optional<Lineup> currentLineupOpt = team.getLineupHistory().stream()
                .filter(lineup -> lineup.getSeason().equals(currentSeason) && lineup.getGameweek() == currentGameweek)
                .max(Comparator.comparing(Lineup::getSubmittedAt));

        if (currentLineupOpt.isPresent()) {
            return lineupConverter.toDto(currentLineupOpt.get());
        } else {
            return new LineupDto();
        }
    }

    @Transactional
    public Lineup submitLineup(Long leagueId, LineupDto lineupDto, UserDetails userDetails) {
        Manager currentUserManager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        Team team = teamRepository.findByManagerAndLeague(currentUserManager, league)
                .orElseThrow(() -> new AccessDeniedException("No team found for the current manager in the specified league."));

        if (!"in season".equals(league.getStatus())) {
            throw new AccessDeniedException("Season has not started.");
        }

        validatePowerups(team, league, lineupDto);
        validateLineup(team, lineupDto);

        int currentGameweek = gameweekComponent.getCurrentGameweek();
        String currentSeason = gameweekComponent.getCurrentSeason();

        Lineup lineup = lineupConverter.toEntity(lineupDto);
        lineup.setSubmittedAt(LocalDateTime.now());
        lineup.setSeason(currentSeason);
        lineup.setGameweek(currentGameweek);
        lineup.setTeam(team);

        return lineupRepository.save(lineup);
    }

    public int calculatePoints(Lineup lineup) {
        return calculatePointsForPlayers(lineup).values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public boolean playedInGameweek(Player player, int gameweek, String season) {
        Optional<PlayerPerformance> performanceOpt =
                playerPerformanceRepository.findByPlayerAndGameweekAndSeason(player, gameweek, season);
        if (performanceOpt.isPresent()) {
            PlayerPerformance performance = performanceOpt.get();
            return performance.getMinutesPlayed() != 0
                    || performance.getRedCards() != 0
                    || performance.getYellowCards() != 0;
        } else {
            return false;
        }
    }

    public Optional<Player> findSubstituteWithPosition(String position, Map<Integer, Long> substitutes) {

        return substitutes.entrySet().stream()
                .filter(entry -> {
                    Optional<Player> playerOpt = playerRepository.findById(entry.getValue());
                    return playerOpt.isPresent() && position.equals(playerOpt.get().getPosition());
                })
                .max(Map.Entry.comparingByKey())
                .flatMap(entry -> playerRepository.findById(entry.getValue()));
    }

    private void validatePowerups(Team team, League league, LineupDto lineupDto) {
        Map<String, Integer> availablePowerups = league.getPowerUps();
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

        String powerup = lineupDto.getPowerup();
        if (powerup != null && !"bboost".equals(powerup) && !"cx3".equals(powerup)) {
            throw new InvalidLineupException("Invalid powerup.");
        }
        if (powerup != null) {
            int countUsed;
            if ("bboost".equals(powerup)) {
                countUsed = (int) powerupsUsedBBoost + 1;
            } else {
                countUsed = (int) powerupsUsedCx3 + 1;
            }

            int availableCount = availablePowerups.getOrDefault(powerup, 0);
            if (countUsed > availableCount) {
                throw new InvalidLineupException("You have used up all available '" + powerup
                        + "' powerups for this league.");
            }
        }
    }

    private void validateLineup(Team team, LineupDto lineupDto) {
        Set<Long> currentPlayerIds = team.getCurrentPlayers().stream()
                .map(Player::getId)
                .collect(Collectors.toSet());

        Set<Long> allSubmittedPlayers = new HashSet<>(lineupDto.getStartingPlayerIds());
        allSubmittedPlayers.addAll(lineupDto.getSubstitutes().values());
        if (!allSubmittedPlayers.equals(currentPlayerIds)) {
            throw new InvalidLineupException("The starting players and substitute players " +
                    "must form the whole current players of the team.");
        }

        if (lineupDto.getCaptainId() == lineupDto.getViceCaptainId()) {
            throw new InvalidLineupException("The captain and vice captain must be different.");
        }

        if (!lineupDto.getStartingPlayerIds().contains(lineupDto.getCaptainId()) ||
                !lineupDto.getStartingPlayerIds().contains(lineupDto.getViceCaptainId())) {
            throw new InvalidLineupException("The captain and vice captain must be in the starting players.");
        }

        if (lineupDto.getStartingPlayerIds().size() != 11) {
            throw new InvalidLineupException("There must be 11 starting players.");
        }

        Map<String, Long> positionCounts = lineupDto.getStartingPlayerIds().stream()
                .map(playerId -> findPlayerById(playerId, team.getCurrentPlayers()))
                .collect(Collectors.groupingBy(Player::getPosition, Collectors.counting()));

        if (positionCounts.getOrDefault("goalkeeper", 0L) != 1 ||
                positionCounts.getOrDefault("defender", 0L) < 3 ||
                positionCounts.getOrDefault("forward", 0L) < 1) {
            throw new InvalidLineupException("The lineup must contain " +
                    "1 goalkeeper, at least 3 defenders, and at least 1 forward.");
        }
    }

    private Player findPlayerById(Long playerId, Set<Player> players) {
        return players.stream()
                .filter(player -> player.getId() == playerId)
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with ID: " + playerId));
    }

    public Set<Player> calculateFinalLineup(Lineup lineup) {
        Set<Player> finalLineup = new HashSet<>(lineup.getStartingPlayers());
        Map<Integer, Long> substitutes = lineup.getSubstitutes();
        int gameweek = lineup.getGameweek();
        String season = lineup.getSeason();

        Set<Player> playersWhoDidNotPlay = finalLineup.stream()
                .filter(player -> !playedInGameweek(player, gameweek, season))
                .collect(Collectors.toSet());

        finalLineup.removeAll(playersWhoDidNotPlay);

        // Attempt to substitute goalkeeper if not playing
        if (finalLineup.stream().noneMatch(player -> "goalkeeper".equals(player.getPosition()))) {
            Optional<Player> substituteGoalkeeper = findSubstituteWithPosition("goalkeeper", substitutes);
            substituteGoalkeeper.ifPresent(substitute -> {
                finalLineup.add(substitute);
                substitutes.remove(substitutes.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(substitute.getId()))
                        .findFirst().map(Map.Entry::getKey).orElse(null));
                playersWhoDidNotPlay.removeIf(player -> "goalkeeper".equals(player.getPosition()));
            });
        }

        // Attempt to substitute defenders if less than 3 are playing
        long defendersPlaying = finalLineup.stream().filter(player -> "defender".equals(player.getPosition())).count();
        if (defendersPlaying < 3) {
            int neededDefenders = (int) (3 - defendersPlaying);
            for (int i = 0; i < neededDefenders; i++) {
                Optional<Player> substituteDefender = findSubstituteWithPosition("defender", substitutes);
                substituteDefender.ifPresent(substitute -> {
                    finalLineup.add(substitute);
                    substitutes.remove(substitutes.entrySet().stream()
                            .filter(entry -> entry.getValue().equals(substitute.getId()))
                            .findFirst().map(Map.Entry::getKey).orElse(null));
                    playersWhoDidNotPlay.removeIf(player -> "defender".equals(player.getPosition()));
                });
            }
        }

        // Attempt to substitute forward if less than 1 is playing
        long forwardsPlaying = finalLineup.stream().filter(player -> "forward".equals(player.getPosition())).count();
        if (forwardsPlaying < 1) {
            Optional<Player> substituteForward = findSubstituteWithPosition("forward", substitutes);
            substituteForward.ifPresent(substitute -> {
                finalLineup.add(substitute);
                substitutes.remove(substitutes.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(substitute.getId()))
                        .findFirst().map(Map.Entry::getKey).orElse(null));
                playersWhoDidNotPlay.removeIf(player -> "forward".equals(player.getPosition()));
            });
        }

        // Attempt to substitute remaining substitutes if needed
        for (Map.Entry<Integer, Long> entry : substitutes.entrySet()) {
            if (playersWhoDidNotPlay.isEmpty() || finalLineup.size() >= 11) {
                break;
            }

            playerRepository.findById(entry.getValue()).ifPresent(substitute -> {
                if (playedInGameweek(substitute, gameweek, season)) {
                    finalLineup.add(substitute);
                    playersWhoDidNotPlay.remove(playersWhoDidNotPlay.iterator().next());
                }
            });
        }

        return finalLineup;
    }

    public Map<Long, Integer> calculatePointsForPlayers(Lineup lineup) {
        Map<Long, Integer> playerPointsMap = new HashMap<>();

        Set<Player> allPlayers = new HashSet<>(lineup.getStartingPlayers());
        allPlayers.forEach(player -> playerPointsMap.put(player.getId(), 0));
        lineup.getSubstitutes().values().forEach(substituteId -> {
            Optional<Player> substituteOpt = playerRepository.findById(substituteId);
            substituteOpt.ifPresent(substitute -> {
                playerPointsMap.put(substitute.getId(), 0);
                allPlayers.add(substitute);
            });
        });

        League league = lineup.getTeam().getLeague();
        int gameweek = lineup.getGameweek();
        String season = lineup.getSeason();
        String powerup = lineup.getPowerup();

        Set<Player> finalLineup = calculateFinalLineup(lineup);
        finalLineup.forEach(player -> {
            long playerId = player.getId();
            Optional<PlayerPerformance> performanceOpt =
                    playerPerformanceRepository.findByPlayerAndGameweekAndSeason(player, gameweek, season);
            performanceOpt.ifPresent(performance -> {
                int playerPoints = playerPerformanceService.calculatePointsInLeague(performance, league);
                playerPointsMap.put(playerId, playerPointsMap.get(playerId) + playerPoints);
            });
        });

        Player captain = lineup.getCaptain();
        Player viceCaptain = lineup.getViceCaptain();
        if ("cx3".equals(powerup)) {
            if (finalLineup.contains(captain)) {
                playerPointsMap.computeIfPresent(captain.getId(), (key, value) -> value * 3);
            } else if (finalLineup.contains(viceCaptain)) {
                playerPointsMap.computeIfPresent(viceCaptain.getId(), (key, value) -> value * 3);
            }
        } else {
            if (finalLineup.contains(captain)) {
                playerPointsMap.computeIfPresent(captain.getId(), (key, value) -> value * 2);
            } else if (finalLineup.contains(viceCaptain) && !finalLineup.contains(captain)) {
                playerPointsMap.computeIfPresent(viceCaptain.getId(), (key, value) -> value * 2);
            }
        }

        if ("bboost".equals(powerup)) {
            allPlayers.stream()
                    .filter(player -> !finalLineup.contains(player))
                    .forEach(player -> {
                        if (playedInGameweek(player, gameweek, season)) {
                            Optional<PlayerPerformance> performanceOpt =
                                    playerPerformanceRepository.findByPlayerAndGameweekAndSeason(player, gameweek, season);
                            performanceOpt.ifPresent(performance -> {
                                int playerPoints = playerPerformanceService.calculatePointsInLeague(performance, league);
                                playerPointsMap.put(player.getId(), playerPoints);
                            });
                        }
                    });
        }

        return playerPointsMap;
    }

    public List<PastLineupDto> getPastLineups(Team team) {
        int currentGameweek = gameweekComponent.getCurrentGameweek();
        String currentSeason = gameweekComponent.getCurrentSeason();

        List<PastLineupDto> pastLineups = new ArrayList<>();
        for (int gw = 1; gw < currentGameweek; gw++) {
            int requestedGameweek = gw;
            Optional<Lineup> finalLineupOpt = team.getLineupHistory().stream()
                    .filter(lineup -> lineup.getSeason().equals(currentSeason) && lineup.getGameweek() == requestedGameweek)
                    .max(Comparator.comparing(Lineup::getSubmittedAt));

            finalLineupOpt.ifPresent(lineup -> {
                PastLineupDto dto = new PastLineupDto();
                dto.setId(lineup.getId());
                dto.setTeamId(team.getId());
                dto.setGameweek(lineup.getGameweek());
                dto.setSeason(lineup.getSeason());
                dto.setStartingPlayers(lineup.getStartingPlayers().stream()
                        .map(PlayerDto::new)
                        .collect(Collectors.toSet()));
                dto.setCaptainId(lineup.getCaptain().getId());
                dto.setViceCaptainId(lineup.getViceCaptain().getId());
                dto.setSubstitutes(lineup.getSubstitutes().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> new PlayerDto(playerRepository.findById(entry.getValue())
                                        .orElseThrow(() -> new PlayerNotFoundException("Player not found.")))
                        )));
                dto.setPowerup(lineup.getPowerup());
                dto.setPoints(calculatePoints(lineup));

                Map<Long, Integer> playerPointsMap = calculatePointsForPlayers(lineup);
                dto.setPlayerPoints(playerPointsMap);

                Map<Long, Boolean> playerToPlayedOrNotMap = new HashMap<>();
                playerPointsMap.forEach((playerId, points) -> {
                    boolean played = playedInGameweek(
                            playerRepository.findById(playerId)
                                    .orElseThrow(() -> new PlayerNotFoundException("Player not found.")),
                            lineup.getGameweek(),
                            lineup.getSeason()
                    );
                    playerToPlayedOrNotMap.put(playerId, played);
                });
                dto.setPlayerToPlayedOrNot(playerToPlayedOrNotMap);

                pastLineups.add(dto);
            });
        }

        return pastLineups;
    }

}
