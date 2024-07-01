package com.winwin.orbital.draft;

import com.winwin.orbital.club.Club;
import com.winwin.orbital.exception.ManagerNotFoundException;
import com.winwin.orbital.exception.TeamNotFoundException;
import com.winwin.orbital.league.League;
import com.winwin.orbital.league.LeagueRepository;
import com.winwin.orbital.manager.Manager;
import com.winwin.orbital.manager.ManagerDto;
import com.winwin.orbital.manager.ManagerRepository;
import com.winwin.orbital.player.Player;
import com.winwin.orbital.player.PlayerDto;
import com.winwin.orbital.team.Team;
import com.winwin.orbital.team.TeamRepository;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
public class DraftSession {
    private final League league;
    private final List<Manager> managers;
    private final List<Player> draftPool;
    private final Map<Long, List<Player>> selectedPlayers;
    private int currentPickIndex;
    private boolean isDraftComplete;
    private final Timer timer;
    private TimerTask currentTask;
    private final DraftService draftService;
    private final DraftWebSocketHandler draftWebSocketHandler;
    private final DraftSessionManager draftSessionManager;
    private final LeagueRepository leagueRepository;
    private final ManagerRepository managerRepository;
    private final TeamRepository teamRepository;
    private final Random random = new Random();
    private String lastPickMessage;

    public DraftSession(League league,
                        List<Player> draftPool,
                        DraftService draftService,
                        DraftWebSocketHandler draftWebSocketHandler,
                        DraftSessionManager draftSessionManager,
                        LeagueRepository leagueRepository,
                        ManagerRepository managerRepository,
                        TeamRepository teamRepository) {
        this.league = league;
        this.managers = new ArrayList<>(league.getTeams().stream()
                .map(Team::getManager)
                .distinct()
                .toList());
        this.draftPool = new ArrayList<>(draftPool);
        this.selectedPlayers = new ConcurrentHashMap<>();
        this.currentPickIndex = 0;
        this.isDraftComplete = false;
        this.timer = new Timer();
        managers.forEach(manager -> selectedPlayers.put(manager.getId(), new ArrayList<>()));
        this.draftService = draftService;
        this.draftWebSocketHandler = draftWebSocketHandler;
        this.draftSessionManager = draftSessionManager;
        this.leagueRepository = leagueRepository;
        this.managerRepository = managerRepository;
        this.teamRepository = teamRepository;
    }

    public synchronized void pickPlayer(long managerId, Player player) {
        if (isDraftComplete || !draftPool.contains(player) || currentManager().getId() != managerId) {
            System.out.println("Invalid pick attempt by Manager ID " + managerId + " in league " + league.getName() + " (ID: " + league.getId() + ")");
            return;
        }

        if (!isValidPick(managerId, player)) {
            System.out.println("VIVUVUVUVUVU");
            return;
        }

        System.out.println("Manager " + managerId + " picked player " + player.getFirstName() + " " + player.getLastName() + " in league " + league.getName() + " (ID: " + league.getId() + ")");
        String managerName = "Unknown";
        if (managerRepository.findById(managerId).isPresent()) {
            managerName = managerRepository.findById(managerId).get().getUsername();
        }
        String playerName = "";
        if (!player.getFirstName().isBlank()) {
            playerName = player.getFirstName() + " ";
        }
        lastPickMessage = managerName + " picked " + playerName + player.getLastName() + " (" + player.getClub().getShortName() + ")";
        selectedPlayers.get(managerId).add(player);
        draftPool.remove(player);

        if (allTeamsCompleted()) {
            notifyDraftCompleted();
        } else {
            moveToNextPick();
        }
    }

    private void moveToNextPick() {
        currentPickIndex = (currentPickIndex + 1) % managers.size();
        if (currentPickIndex == 0) {
            Collections.reverse(managers);
        }
        System.out.println("Next pick: Manager " + currentManager().getId() + " in league " + league.getName() + " (ID: " + league.getId() + ")");
        scheduleNextPick();
    }

    private void scheduleNextPick() {
        if (currentTask != null) {
            currentTask.cancel();
        }
        currentTask = new TimerTask() {
            @Override
            public void run() {
                autoPickPlayer();
            }
        };
        timer.schedule(currentTask, league.getDraftTurnDurationMilliseconds());

        sendDraftStateUpdate();
    }

    private void autoPickPlayer() {
        List<Player> currentTeamPlayers = selectedPlayers.get(currentManager().getId());
        long goalkeepers = currentTeamPlayers.stream().filter(p -> p.getPosition().equals("goalkeeper")).count();
        long defenders = currentTeamPlayers.stream().filter(p -> p.getPosition().equals("defender")).count();
        long midfielders = currentTeamPlayers.stream().filter(p -> p.getPosition().equals("midfielder")).count();
        long forwards = currentTeamPlayers.stream().filter(p -> p.getPosition().equals("forward")).count();

        Set<String> validPositions = new HashSet<>();

        if (goalkeepers < 2) {
            validPositions.add("goalkeeper");
        }
        if (defenders < 5) {
            validPositions.add("defender");
        }
        if (midfielders < 5) {
            validPositions.add("midfielder");
        }
        if (forwards < 3) {
            validPositions.add("forward");
        }

        Set<Club> excludedClubs = currentTeamPlayers.stream()
                .collect(Collectors.groupingBy(Player::getClub, Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() >= league.getMaxNumberOfPlayersFromAClub())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        List<Player> filteredPlayers = draftPool.stream()
                .filter(player -> !excludedClubs.contains(player.getClub()))
                .filter(player -> validPositions.contains(player.getPosition()))
                .toList();

        int randomIndex = random.nextInt(filteredPlayers.size());

        Optional<Player> randomPlayer = Optional.of(filteredPlayers.get(randomIndex));

        randomPlayer.ifPresent(player -> {
            System.out.println("Auto-picking player " + player.getFirstName() + " " + player.getLastName() + " for Manager " + currentManager().getId() + " in league " + league.getName() + " (ID: " + league.getId() + ")");
            pickPlayer(currentManager().getId(), player);
        });
    }

    private Manager currentManager() {
        return managers.get(currentPickIndex);
    }

    private Manager nextManager() {
        int nextPickIndex = (currentPickIndex + 1) % managers.size();
        if (nextPickIndex == 0) {
            nextPickIndex = managers.size() - 1;
            if (selectedPlayers.get(managers.get(nextPickIndex).getId()).size() >= 14) {
                return null;
            }
        }
        return managers.get(nextPickIndex);
    }

    private boolean allTeamsCompleted() {
        return selectedPlayers.values().stream().allMatch(players -> players.size() == 15);
    }

    public void startDraft() {
        System.out.println("Starting draft for league " + league.getName() + " (ID: " + league.getId() + ")");
        scheduleNextPick();
    }

    @Transactional
    private void notifyDraftCompleted() {
        isDraftComplete = true;
        if (currentTask != null) {
            currentTask.cancel();
        }
        timer.cancel();

        sendDraftStateUpdate();

        for (Map.Entry<Long, List<Player>> entry : selectedPlayers.entrySet()) {
            Long managerId = entry.getKey();
            List<Player> players = entry.getValue();

            Manager manager = managerRepository.findById(managerId)
                    .orElseThrow(() -> new ManagerNotFoundException("Manager not found for ID: " + managerId));

            Optional<Team> optionalTeam = teamRepository.findByManagerAndLeague(manager, league);
            Team team = optionalTeam.orElseThrow(() -> new TeamNotFoundException("Team not found for manager ID: " + managerId + " in league " + league.getName()));

            team.setCurrentPlayers(new HashSet<>(players));
            teamRepository.save(team);
        }

        league.setStatus("in season");
        leagueRepository.save(league);

        draftSessionManager.removeDraftSession(league.getId());
        draftWebSocketHandler.sendDraftComplete(league.getId());
        System.out.println("Draft complete for league " + league.getName() + " (ID: " + league.getId() + ")");
    }

    public boolean isDraftComplete() {
        return isDraftComplete;
    }

    public DraftState getDraftState() {
        ManagerDto currentManagerDto = new ManagerDto(this.currentManager());
        ManagerDto nextManagerDto = new ManagerDto();
        Manager nextManager = this.nextManager();
        if (nextManager != null) {
            nextManagerDto = new ManagerDto(nextManager);
        }

        Map<ManagerDto, List<PlayerDto>> selectedPlayerDtos = selectedPlayers.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> new ManagerDto(managerRepository.findById(entry.getKey())
                                .orElseThrow(() -> new ManagerNotFoundException("Manager not found for ID: " + entry.getKey()))),
                        entry -> entry.getValue().stream().map(PlayerDto::new).collect(Collectors.toList())
                ));
        List<PlayerDto> draftPoolDtos = draftPool.stream().map(PlayerDto::new).collect(Collectors.toList());

        return new DraftState(currentManagerDto,
                nextManagerDto,
                selectedPlayerDtos,
                draftPoolDtos,
                lastPickMessage,
                calculateRemainingTime()
        );
    }

    private long calculateRemainingTime() {
        if (currentTask != null) {
            long remainingTime = currentTask.scheduledExecutionTime() - System.currentTimeMillis();;
            return remainingTime > 0 ? remainingTime : 0;
        } else {
            return 0;
        }
    }

    private void sendDraftStateUpdate() {
        draftWebSocketHandler.sendDraftStateUpdate(league.getId(), this.getDraftState());
    }

    private boolean isValidPick(long managerId, Player player) {
        List<Player> players = selectedPlayers.get(managerId);

        long clubCount = players.stream().filter(p -> p.getClub().equals(player.getClub())).count();
        if (clubCount >= league.getMaxNumberOfPlayersFromAClub()) {
            return false;
        }

        String position = player.getPosition();

        long samePositions = players.stream().filter(p -> p.getPosition().equals(position)).count();

        if ("goalkeeper".equals(position)) {
            return samePositions < 2;
        } else if ("defender".equals(position)) {
            return samePositions < 5;
        } else if ("midfielder".equals(position)) {
            return samePositions < 5;
        } else if ("forward".equals(position)) {
            return samePositions < 3;
        }

        return false;
    }
}