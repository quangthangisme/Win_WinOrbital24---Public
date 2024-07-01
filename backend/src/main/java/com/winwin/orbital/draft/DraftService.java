package com.winwin.orbital.draft;

import com.winwin.orbital.exception.LeagueNotFoundException;
import com.winwin.orbital.exception.UserNotFoundException;
import com.winwin.orbital.league.League;
import com.winwin.orbital.league.LeagueRepository;
import com.winwin.orbital.manager.Manager;
import com.winwin.orbital.manager.ManagerRepository;
import com.winwin.orbital.player.Player;
import com.winwin.orbital.player.PlayerRepository;
import com.winwin.orbital.team.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DraftService {

    private final DraftSessionManager draftSessionManager;
    private final DraftWebSocketHandler draftWebSocketHandler;
    private final LeagueRepository leagueRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final ManagerRepository managerRepository;

    @Autowired
    public DraftService(DraftSessionManager draftSessionManager, DraftWebSocketHandler draftWebSocketHandler,
                        LeagueRepository leagueRepository, PlayerRepository playerRepository,
                        TeamRepository teamRepository, ManagerRepository managerRepository) {
        this.draftSessionManager = draftSessionManager;
        this.draftWebSocketHandler = draftWebSocketHandler;
        this.leagueRepository = leagueRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.managerRepository = managerRepository;
    }

    @Transactional
    public void startScheduledDrafts() {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime previousTime = currentTime.minusMinutes(1);

        List<League> leagues = leagueRepository.findLeaguesWithDraftStarting(currentTime, previousTime);
        for (League league : leagues) {
            startDraft(league);
        }
    }

    @Transactional
    public void startDraft(League league) {
        List<Player> draftPool = playerRepository.findByIsAvailableTrue();
        DraftSession draftSession = new DraftSession(league,
                draftPool,
                this,
                draftWebSocketHandler,
                draftSessionManager,
                leagueRepository,
                managerRepository,
                teamRepository);
        draftSessionManager.addDraftSession(draftSession);

        league.setStatus("drafting");
        leagueRepository.save(league);

        System.out.println("Draft session started for league " + league.getName() + " (ID: " + league.getId() + ")");
        draftSession.startDraft();
    }

    @Transactional
    public void pickPlayer(long leagueId, long playerId, UserDetails userDetails) {
        DraftSession draftSession = draftSessionManager.getDraftSession(leagueId);
        if (draftSession == null || draftSession.isDraftComplete()) {
            System.out.println("Invalid draft session for league " + leagueId);
            return;
        }

        Manager manager = managerRepository.findByUserUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Cannot authenticate current user."));

        Player player = playerRepository.findById(playerId).orElse(null);
        if (player != null) {
            System.out.println("User " + userDetails.getUsername() + " (Manager ID: " + manager.getId() + ") is picking player ID " + playerId + " for league " + draftSession.getLeague().getName());
            draftSession.pickPlayer(manager.getId(), player);
        }
    }

    public void sendDraftState(long leagueId, UserDetails userDetails) {
        String username = userDetails.getUsername();
        if (username == null) {
            return;
        }

        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException("League not found."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime draftStartTime = league.getDraftStartTime();
        long millisecondsToDraftStart = ChronoUnit.MILLIS.between(now, draftStartTime);

        if (millisecondsToDraftStart > 0 && millisecondsToDraftStart <= 300000) {
            draftWebSocketHandler.sendDraftStartSoonToUser(leagueId, millisecondsToDraftStart, username);
            return;
        }

        DraftSession draftSession = draftSessionManager.getDraftSession(leagueId);
        if (draftSession == null || draftSession.isDraftComplete()) {
            System.out.println("Invalid draft session for league " + leagueId);
            return;
        }

        draftWebSocketHandler.sendDraftStateUpdateToUser(leagueId, draftSession.getDraftState(), username);
    }

}

