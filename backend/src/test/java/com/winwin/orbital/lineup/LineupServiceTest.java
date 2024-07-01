package com.winwin.orbital.lineup;

import com.winwin.orbital.league.League;
import com.winwin.orbital.player.Player;
import com.winwin.orbital.player.PlayerRepository;
import com.winwin.orbital.playerperformance.PlayerPerformance;
import com.winwin.orbital.playerperformance.PlayerPerformanceRepository;
import com.winwin.orbital.playerperformance.PlayerPerformanceService;
import com.winwin.orbital.team.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LineupServiceTest {

    @Mock
    private PlayerPerformanceRepository playerPerformanceRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerPerformanceService playerPerformanceService;

    @InjectMocks
    private LineupService lineupService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPlayedInGameweek_PlayerDidNotPlay() {
        Player player = new Player();
        when(playerPerformanceRepository.findByPlayerAndGameweekAndSeason(player, 1, "2024"))
                .thenReturn(Optional.empty());

        assertFalse(lineupService.playedInGameweek(player, 1, "2024"));
    }

    @Test
    public void testPlayedInGameweek_PlayerPlayed() {
        Player player = new Player();
        PlayerPerformance playerPerformance = new PlayerPerformance();
        playerPerformance.setMinutesPlayed(90);
        when(playerPerformanceRepository.findByPlayerAndGameweekAndSeason(player, 1, "2024"))
                .thenReturn(Optional.of(playerPerformance));

        assertTrue(lineupService.playedInGameweek(player, 1, "2024"));
    }

    @Test
    public void testFindSubstituteWithPosition_SubstituteFound() {
        Map<Integer, Long> substitutes = new HashMap<>();
        substitutes.put(1, 100L);

        Player substitutePlayer = new Player();
        substitutePlayer.setId(100L);
        substitutePlayer.setPosition("goalkeeper");

        when(playerRepository.findById(100L)).thenReturn(Optional.of(substitutePlayer));

        Optional<Player> substitute = lineupService.findSubstituteWithPosition("goalkeeper", substitutes);
        assertTrue(substitute.isPresent());
        assertEquals("goalkeeper", substitute.get().getPosition());
    }

    @Test
    public void testFindSubstituteWithPosition_NoSubstituteFound() {
        Map<Integer, Long> substitutes = new HashMap<>();
        substitutes.put(1, 100L);

        when(playerRepository.findById(100L)).thenReturn(Optional.empty());

        Optional<Player> substitute = lineupService.findSubstituteWithPosition("goalkeeper", substitutes);
        assertFalse(substitute.isPresent());
    }

    @Test
    public void testCalculateFinalLineup() {
        Lineup lineup = new Lineup();
        lineup.setStartingPlayers(new HashSet<>());
        lineup.setSubstitutes(new HashMap<>());
        lineup.setGameweek(1);
        lineup.setSeason("2024");

        Player player1 = new Player();
        player1.setId(1L);
        player1.setPosition("goalkeeper");
        lineup.getStartingPlayers().add(player1);

        Player substitutePlayer = new Player();
        substitutePlayer.setId(2L);
        substitutePlayer.setPosition("defender");
        lineup.getSubstitutes().put(1, 2L);

        when(playerPerformanceRepository.findByPlayerAndGameweekAndSeason(player1, 1, "2024"))
                .thenReturn(Optional.empty());

        when(playerPerformanceRepository.findByPlayerAndGameweekAndSeason(substitutePlayer, 1, "2024"))
                .thenReturn(Optional.of(new PlayerPerformance()));

        when(playerRepository.findById(2L)).thenReturn(Optional.of(substitutePlayer));

        Set<Player> finalLineup = lineupService.calculateFinalLineup(lineup);
        assertEquals(1, finalLineup.size());
    }

    @Test
    public void testCalculatePointsForPlayers() {
        Lineup lineup = new Lineup();
        lineup.setStartingPlayers(new HashSet<>());
        lineup.setSubstitutes(new HashMap<>());
        lineup.setGameweek(1);
        lineup.setSeason("2024");
        lineup.setPowerup("cx3");

        Player player1 = new Player();
        player1.setId(1L);
        player1.setPosition("goalkeeper");
        lineup.getStartingPlayers().add(player1);
        lineup.setCaptain(player1);

        Player player2 = new Player();
        player2.setId(2L);
        player2.setPosition("defender");
        lineup.getStartingPlayers().add(player2);
        lineup.setViceCaptain(player2);

        PlayerPerformance playerPerformance1 = new PlayerPerformance();
        playerPerformance1.setMinutesPlayed(90);
        when(playerPerformanceRepository.findByPlayerAndGameweekAndSeason(player1, 1, "2024"))
                .thenReturn(Optional.of(playerPerformance1));

        PlayerPerformance playerPerformance2 = new PlayerPerformance();
        playerPerformance2.setMinutesPlayed(90);
        when(playerPerformanceRepository.findByPlayerAndGameweekAndSeason(player2, 1, "2024"))
                .thenReturn(Optional.of(playerPerformance2));

        when(playerPerformanceService.calculatePointsInLeague(any(), any()))
                .thenReturn(10);

        when(playerRepository.findById(2L)).thenReturn(Optional.of(player2));

        League league = new League();
        lineup.setTeam(new Team());
        lineup.getTeam().setLeague(league);

        when(playerRepository.findById(anyLong())).thenReturn(Optional.of(new Player()));

        Map<Long, Integer> pointsMap = lineupService.calculatePointsForPlayers(lineup);
        assertEquals(30, pointsMap.get(1L));
        assertEquals(10, pointsMap.get(2L));
    }
}

