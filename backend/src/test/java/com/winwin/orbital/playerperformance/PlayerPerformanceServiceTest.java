package com.winwin.orbital.playerperformance;

import com.winwin.orbital.league.League;
import com.winwin.orbital.player.Player;
import com.winwin.orbital.scoringrule.ScoringRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlayerPerformanceServiceTest {

    private PlayerPerformanceService playerPerformanceService;
    private ScoringRule scoringRule;

    @BeforeEach
    public void setup() {
        playerPerformanceService = new PlayerPerformanceService();
        scoringRule = mock(ScoringRule.class);
    }

    @Test
    public void testCalculatePointsInLeague_Goalkeeper() {
        Player player = new Player();
        player.setPosition("goalkeeper");
        PlayerPerformance performance = new PlayerPerformance();
        performance.setPlayer(player);
        performance.setMinutesPlayed(90);
        performance.setGoalsScored(0);
        performance.setAssists(0);
        performance.setCleanSheet(1);
        performance.setSaves(6);
        performance.setPenaltiesSaved(1);
        performance.setPenaltiesMissed(0);
        performance.setGoalsConceded(1);
        performance.setYellowCards(0);
        performance.setRedCards(0);
        performance.setOwnGoals(0);

        when(scoringRule.getFor60Mins()).thenReturn(1);
        when(scoringRule.getForOver60Mins()).thenReturn(2);
        when(scoringRule.getForGkDfGoal()).thenReturn(5);
        when(scoringRule.getForAssist()).thenReturn(3);
        when(scoringRule.getForGkDfCleanSheet()).thenReturn(4);
        when(scoringRule.getFor3GkSaves()).thenReturn(1);
        when(scoringRule.getForPkSaved()).thenReturn(5);
        when(scoringRule.getForPkMissed()).thenReturn(-2);
        when(scoringRule.getFor2GoalsConceded()).thenReturn(-1);
        when(scoringRule.getForYellowCard()).thenReturn(-1);
        when(scoringRule.getForRedCard()).thenReturn(-3);
        when(scoringRule.getForOwnGoal()).thenReturn(-4);

        League league = new League();
        league.setScoringRule(scoringRule);

        int points = playerPerformanceService.calculatePointsInLeague(performance, league);

        int expectedPoints = 2
                + 4
                + (6 / 3) * 1
                + 5;

        assertEquals(expectedPoints, points);
    }

}

