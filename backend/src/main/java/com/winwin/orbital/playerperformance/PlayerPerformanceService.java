package com.winwin.orbital.playerperformance;

import com.winwin.orbital.league.League;
import com.winwin.orbital.scoringrule.ScoringRule;
import org.springframework.stereotype.Service;

@Service
public class PlayerPerformanceService {

    public int calculatePointsInLeague(PlayerPerformance performance, League league) {
        ScoringRule scoringRule = league.getScoringRule();
        int points = 0;
        String position = performance.getPlayer().getPosition();

        if (performance.getMinutesPlayed() > 0 && performance.getMinutesPlayed() < 60) {
            points += scoringRule.getFor60Mins();
        } else if (performance.getMinutesPlayed() >= 60) {
            points += scoringRule.getForOver60Mins();
        }

        if (position.equals("goalkeeper") || position.equals("defender")) {
            points += performance.getGoalsScored() * scoringRule.getForGkDfGoal();
        } else if (position.equals("midfielder")) {
            points += performance.getGoalsScored() * scoringRule.getForMdGoal();
        } else if (position.equals("forward")) {
            points += performance.getGoalsScored() * scoringRule.getForFwGoal();
        }

        points += performance.getAssists() * scoringRule.getForAssist();

        if (position.equals("goalkeeper") || position.equals("defender")) {
            if (performance.getCleanSheet() > 0) {
                points += scoringRule.getForGkDfCleanSheet();
            }
        } else if (position.equals("midfielder")) {
            if (performance.getCleanSheet() > 0) {
                points += scoringRule.getForMdCleanSheet();
            }
        }

        if (position.equals("goalkeeper")) {
            points += (performance.getSaves() / 3) * scoringRule.getFor3GkSaves();
        }

        points += performance.getPenaltiesSaved() * scoringRule.getForPkSaved();

        points += performance.getPenaltiesMissed() * scoringRule.getForPkMissed();

        if (position.equals("goalkeeper") || position.equals("defender")) {
            points += (performance.getGoalsConceded() / 2) * scoringRule.getFor2GoalsConceded();
        }

        points -= performance.getYellowCards() * scoringRule.getForYellowCard();
        points -= performance.getRedCards() * scoringRule.getForRedCard();

        points -= performance.getOwnGoals() * scoringRule.getForOwnGoal();

        return points;
    }
}