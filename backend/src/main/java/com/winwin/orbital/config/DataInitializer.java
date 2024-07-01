package com.winwin.orbital.config;

import com.winwin.orbital.club.Club;
import com.winwin.orbital.club.ClubRepository;
import com.winwin.orbital.league.League;
import com.winwin.orbital.league.LeagueRepository;
import com.winwin.orbital.lineup.Lineup;
import com.winwin.orbital.lineup.LineupRepository;
import com.winwin.orbital.manager.Manager;
import com.winwin.orbital.manager.ManagerRepository;
import com.winwin.orbital.player.Player;
import com.winwin.orbital.player.PlayerRepository;
import com.winwin.orbital.playerperformance.PlayerPerformance;
import com.winwin.orbital.playerperformance.PlayerPerformanceRepository;
import com.winwin.orbital.scoringrule.ScoringRule;
import com.winwin.orbital.scoringrule.ScoringRuleRepository;
import com.winwin.orbital.team.Team;
import com.winwin.orbital.team.TeamRepository;
import com.winwin.orbital.user.User;
import com.winwin.orbital.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private LineupRepository lineupRepository;

    @Autowired
    private ScoringRuleRepository scoringRuleRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private PlayerPerformanceRepository playerPerformanceRepository;

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

        LocalDateTime draftStartTime = LocalDateTime.now().plusMinutes(5);

        League leagueX = new League();
        leagueX.setName("League X");
        leagueX.setCode("XXXXXXXX");
        leagueX.setPowerUps(new HashMap<>());
        leagueX.getPowerUps().put("bboost", 1);
        leagueX.getPowerUps().put("cx3", 2);
        leagueX.setAdmin(managerA);
        leagueX.setStatus("in season");
        leagueX.setDraftStartTime(draftStartTime);
        leagueX.setMaxNumberOfPlayersFromAClub(3);
        leagueX.setDraftTurnDurationMilliseconds(5000);

        leagueX = leagueRepository.save(leagueX);

        ScoringRule scoringRuleX = new ScoringRule();
        scoringRuleX.setLeague(leagueX);
        scoringRuleX.setFor60Mins(1);
        scoringRuleX.setForOver60Mins(2);
        scoringRuleX.setForGkDfGoal(6);
        scoringRuleX.setForMdGoal(5);
        scoringRuleX.setForFwGoal(4);
        scoringRuleX.setForAssist(3);
        scoringRuleX.setForGkDfCleanSheet(4);
        scoringRuleX.setForMdCleanSheet(1);
        scoringRuleX.setFor3GkSaves(1);
        scoringRuleX.setForPkSaved(5);
        scoringRuleX.setForPkMissed(-2);
        scoringRuleX.setFor2GoalsConceded(-1);
        scoringRuleX.setForYellowCard(-1);
        scoringRuleX.setForRedCard(-3);
        scoringRuleX.setForOwnGoal(-2);
        scoringRuleX = scoringRuleRepository.save(scoringRuleX);

        leagueX.setScoringRule(scoringRuleX);
        leagueRepository.save(leagueX);

        League leagueY = new League();
        leagueY.setName("League Y");
        leagueY.setCode("YYYYYYYY");
        leagueY.setPowerUps(new HashMap<>());
        leagueY.getPowerUps().put("bboost", 1);
        leagueY.getPowerUps().put("cx3", 2);
        leagueY.setAdmin(managerB);
        leagueY.setStatus("created");
//        leagueY.setDraftStartTime(draftStartTime);
        leagueY.setMaxNumberOfPlayersFromAClub(3);
        leagueY = leagueRepository.save(leagueY);

        ScoringRule scoringRuleY = new ScoringRule();
        scoringRuleY.setLeague(leagueY);
        scoringRuleY.setFor60Mins(1);
        scoringRuleY.setForOver60Mins(2);
        scoringRuleY.setForGkDfGoal(6);
        scoringRuleY.setForMdGoal(5);
        scoringRuleY.setForFwGoal(4);
        scoringRuleY.setForAssist(3);
        scoringRuleY.setForGkDfCleanSheet(4);
        scoringRuleY.setForMdCleanSheet(1);
        scoringRuleY.setFor3GkSaves(1);
        scoringRuleY.setForPkSaved(5);
        scoringRuleY.setForPkMissed(-2);
        scoringRuleY.setFor2GoalsConceded(-1);
        scoringRuleY.setForYellowCard(-1);
        scoringRuleY.setForRedCard(-3);
        scoringRuleY.setForOwnGoal(-2);
        scoringRuleY = scoringRuleRepository.save(scoringRuleY);

        leagueY.setScoringRule(scoringRuleY);
        leagueRepository.save(leagueY);

        League leagueZ = new League();
        leagueZ.setName("League Z");
        leagueZ.setCode("ZZZZZZZZ");
        leagueZ.setPowerUps(new HashMap<>());
        leagueZ.getPowerUps().put("bboost", 1);
        leagueZ.getPowerUps().put("cx3", 2);
        leagueZ.setAdmin(managerC);
        leagueZ.setStatus("waiting for draft");
        leagueZ.setDraftStartTime(draftStartTime);
        leagueZ.setMaxNumberOfPlayersFromAClub(3);
        leagueZ.setDraftTurnDurationMilliseconds(5000);
        leagueZ = leagueRepository.save(leagueZ);

        ScoringRule scoringRuleZ = new ScoringRule();
        scoringRuleZ.setLeague(leagueZ);
        scoringRuleZ.setFor60Mins(1);
        scoringRuleZ.setForOver60Mins(2);
        scoringRuleZ.setForGkDfGoal(6);
        scoringRuleZ.setForMdGoal(5);
        scoringRuleZ.setForFwGoal(4);
        scoringRuleZ.setForAssist(3);
        scoringRuleZ.setForGkDfCleanSheet(4);
        scoringRuleZ.setForMdCleanSheet(1);
        scoringRuleZ.setFor3GkSaves(1);
        scoringRuleZ.setForPkSaved(5);
        scoringRuleZ.setForPkMissed(-2);
        scoringRuleZ.setFor2GoalsConceded(-1);
        scoringRuleZ.setForYellowCard(-1);
        scoringRuleZ.setForRedCard(-3);
        scoringRuleZ.setForOwnGoal(-2);
        scoringRuleZ = scoringRuleRepository.save(scoringRuleZ);

        leagueZ.setScoringRule(scoringRuleZ);
        leagueRepository.save(leagueZ);

        // Create teams and assign to managers and leagues
        Team teamA1 = new Team();
        teamA1.setName("Team A1");
        teamA1.setManager(managerA);
        teamA1.setLeague(leagueX);
        teamA1 = teamRepository.save(teamA1);

        Team teamA2 = new Team();
        teamA2.setName("Team A2");
        teamA2.setManager(managerA);
        teamA2.setLeague(leagueY);
        teamA2 = teamRepository.save(teamA2);

        Team teamB1 = new Team();
        teamB1.setName("Team B1");
        teamB1.setManager(managerB);
        teamB1.setLeague(leagueX);
        teamB1 = teamRepository.save(teamB1);

        Team teamB2 = new Team();
        teamB2.setName("Team B2");
        teamB2.setManager(managerB);
        teamB2.setLeague(leagueY);
        teamB2 = teamRepository.save(teamB2);

        Team teamB3 = new Team();
        teamB3.setName("Team B3");
        teamB3.setManager(managerB);
        teamB3.setLeague(leagueZ);
        teamB3 = teamRepository.save(teamB3);

        Team teamC1 = new Team();
        teamC1.setName("Team C1");
        teamC1.setManager(managerC);
        teamC1.setLeague(leagueY);
        teamC1 = teamRepository.save(teamC1);

        Team teamC2 = new Team();
        teamC2.setName("Team C2");
        teamC2.setManager(managerC);
        teamC2.setLeague(leagueZ);
        teamC2 = teamRepository.save(teamC2);

        initializeClubsAndPlayers();

        List<Team> teams = Arrays.asList(teamA1, teamB1); //, teamB1, teamB2, teamB3, teamC1, teamC2

        int playerCounter = 1;
        Map<Team, Set<Player>> teamPlayersMap = new HashMap<>();

        Club testLineupClub = new Club();
        testLineupClub.setName("Test club");
        testLineupClub.setShortName("TST");
        testLineupClub.setAvailable(false);
        clubRepository.save(testLineupClub);

        for (Team team : teams) {
            Set<Player> players = new HashSet<>();
            List<Player> goalkeepers = new ArrayList<>();
            List<Player> defenders = new ArrayList<>();
            List<Player> midfielders = new ArrayList<>();
            List<Player> forwards = new ArrayList<>();

            // Create goalkeepers
            for (int i = 0; i < 2; i++) {
                Player player = new Player("First" + playerCounter, "Last" + playerCounter, "goalkeeper");
                player.setClub(testLineupClub);
                players.add(player);
                goalkeepers.add(player);
                playerCounter++;
            }

            // Create defenders
            for (int i = 0; i < 5; i++) {
                Player player = new Player("First" + playerCounter, "Last" + playerCounter, "defender");
                player.setClub(testLineupClub);
                players.add(player);
                defenders.add(player);
                playerCounter++;
            }

            // Create midfielders
            for (int i = 0; i < 4; i++) {
                Player player = new Player("First" + playerCounter, "Last" + playerCounter, "midfielder");
                player.setClub(testLineupClub);
                players.add(player);
                midfielders.add(player);
                playerCounter++;
            }

            // Create forwards
            for (int i = 0; i < 4; i++) {
                Player player = new Player("First" + playerCounter, "Last" + playerCounter, "forward");
                player.setClub(testLineupClub);
                players.add(player);
                forwards.add(player);
                playerCounter++;
            }

            team.setCurrentPlayers(players);
            teamPlayersMap.put(team, players);
            playerRepository.saveAll(players);

            // Initialize performance history for each player
            List<Player> allPlayers = new ArrayList<>();
            allPlayers.addAll(goalkeepers);
            allPlayers.addAll(defenders);
            allPlayers.addAll(midfielders);
            allPlayers.addAll(forwards);

            for (Player player : allPlayers) {
                Set<PlayerPerformance> performanceHistory = new HashSet<>();
                for (int gameweek = 1; gameweek <= 4; gameweek++) {
                    PlayerPerformance performance = new PlayerPerformance();
                    performance.setPlayer(player);
                    performance.setGameweek(gameweek);
                    performance.setSeason("24/25");

                    if (player.equals(goalkeepers.getFirst()) || player.equals(defenders.getFirst()) ||
                            player.equals(midfielders.getFirst()) || player.equals(forwards.getFirst())) {
                        performance.setMinutesPlayed(0);
                        performance.setRedCards(0);
                        performance.setYellowCards(0);
                    } else {
                        performance.setMinutesPlayed(90);  // Set specific non-zero playing minutes
                        performance.setRedCards(0);
                        performance.setYellowCards(0);
                    }

                    // Set other fields to zero for simplification
                    performance.setGoalsScored(0);
                    performance.setAssists(0);
                    performance.setCleanSheet(0);
                    performance.setSaves(0);
                    performance.setPenaltiesSaved(0);
                    performance.setPenaltiesMissed(0);
                    performance.setGoalsConceded(0);
                    performance.setOwnGoals(0);

                    performanceHistory.add(performance);
                }
                player.setPerformanceHistory(performanceHistory);
                playerPerformanceRepository.saveAll(performanceHistory);
            }
        }

        List<Lineup> lineups = new ArrayList<>();

        for (int gameweek = 1; gameweek <= 4; gameweek++) {
            for (Team team : teams) {
                Set<Player> players = teamPlayersMap.get(team);

                List<Player> goalkeepers = players.stream().filter(p -> p.getPosition().equals("goalkeeper")).toList();
                List<Player> defenders = players.stream().filter(p -> p.getPosition().equals("defender")).toList();
                List<Player> midfielders = players.stream().filter(p -> p.getPosition().equals("midfielder")).toList();
                List<Player> forwards = players.stream().filter(p -> p.getPosition().equals("forward")).toList();

                if (goalkeepers.isEmpty() || defenders.size() < 4 || midfielders.size() < 3 || forwards.size() < 4) {
                    throw new IllegalStateException("Team does not have sufficient players in required positions");
                }

                Set<Player> startingPlayers = new HashSet<>();
                startingPlayers.add(goalkeepers.getFirst());
                startingPlayers.addAll(defenders.subList(0, 4));
                startingPlayers.addAll(midfielders.subList(1, 4));
                startingPlayers.addAll(forwards.subList(1, 4));

                List<Player> remainingPlayers = new ArrayList<>(players);
                remainingPlayers.removeAll(startingPlayers);
                Map<Integer, Long> substitutes = new HashMap<>();
                for (int i = 0; i < remainingPlayers.size(); i++) {
                    substitutes.put(i + 1, remainingPlayers.get(i).getId());
                }

                LocalDateTime submissionTime1 = LocalDateTime.now().minusDays(1).plusHours(gameweek);
                LocalDateTime submissionTime2 = LocalDateTime.now().plusHours(gameweek);

                String powerup = gameweek == 1 ? "bboost" : null;

                List<Player> startingPlayersList = new ArrayList<>(startingPlayers);
                Lineup lineup1 = new Lineup(gameweek, "24/25", submissionTime1,
                        startingPlayers, startingPlayersList.get(0), startingPlayersList.get(1), substitutes, powerup, team);
                Lineup lineup2 = new Lineup(gameweek, "24/25", submissionTime2,
                        startingPlayers, startingPlayersList.get(2), startingPlayersList.get(3), substitutes, powerup, team);

                lineups.add(lineup1);
                lineups.add(lineup2);

                team.getLineupHistory().add(lineup1);
                team.getLineupHistory().add(lineup2);
            }
        }

        lineupRepository.saveAll(lineups);
        teamRepository.saveAll(teams);
    }

    public void initializeClubsAndPlayers() {
        List<String> clubNames = Arrays.asList(
                "Arsenal", "Aston Villa", "Bournemouth", "Brentford", "Brighton",
                "Burnley", "Chelsea", "Crystal Palace", "Everton", "Fulham",
                "Liverpool", "Luton", "Man City", "Man Utd", "Newcastle",
                "Nott'm Forest", "Sheffield Utd", "Spurs", "West Ham", "Wolves"
        );

        List<Club> clubs = new ArrayList<>();
        clubNames.forEach(name -> {
            Club club = new Club();
            club.setName(name);
            club.setShortName(getShortName(name));
            club.setAvailable(true);

            clubs.add(clubRepository.save(club));
        });

        clubs.forEach(this::createPlayers);
    }

    private void createPlayers(Club club) {
        // Create 3 goalkeepers
        for (int i = 1; i <= 3; i++) {
            Player goalkeeper = new Player("goalkeeper_" + i, club.getShortName(), "goalkeeper");
            goalkeeper.setClub(club);
            goalkeeper.setAvailable(true);
            playerRepository.save(goalkeeper);
        }

        // Create 9 midfielders
        for (int i = 1; i <= 9; i++) {
            Player midfielder = new Player("midfielder_" + i, club.getShortName(), "midfielder");
            midfielder.setClub(club);
            midfielder.setAvailable(true);
            playerRepository.save(midfielder);
        }

        // Create 9 defenders
        for (int i = 1; i <= 9; i++) {
            Player defender = new Player("defender_" + i, club.getShortName(), "defender");
            defender.setClub(club);
            defender.setAvailable(true);
            playerRepository.save(defender);
        }

        // Create 4 forwards
        for (int i = 1; i <= 4; i++) {
            Player forward = new Player("forward_" + i, club.getShortName(), "forward");
            forward.setClub(club);
            forward.setAvailable(true);
            playerRepository.save(forward);
        }
    }

    private String getShortName(String fullName) {
        return switch (fullName) {
            case "Arsenal" -> "ARS";
            case "Aston Villa" -> "AVL";
            case "Bournemouth" -> "BOU";
            case "Brentford" -> "BRE";
            case "Brighton" -> "BHA";
            case "Burnley" -> "BUR";
            case "Chelsea" -> "CHE";
            case "Crystal Palace" -> "CRY";
            case "Everton" -> "EVE";
            case "Fulham" -> "FUL";
            case "Liverpool" -> "LIV";
            case "Luton" -> "LUT";
            case "Man City" -> "MCI";
            case "Man Utd" -> "MUN";
            case "Newcastle" -> "NEW";
            case "Nott'm Forest" -> "NFO";
            case "Sheffield Utd" -> "SHU";
            case "Spurs" -> "TOT";
            case "West Ham" -> "WHU";
            case "Wolves" -> "WOL";
            default -> "";
        };
    }
}
