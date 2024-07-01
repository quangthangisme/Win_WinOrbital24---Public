package com.winwin.orbital.scoringrule;

import lombok.*;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class ScoringRuleDto {

    private int for60Mins;
    private int forOver60Mins;
    private int forGkDfGoal;
    private int forMdGoal;
    private int forFwGoal;
    private int forAssist;
    private int forGkDfCleanSheet;
    private int forMdCleanSheet;
    private int for3GkSaves;
    private int forPkSaved;
    private int forPkMissed;
    private int for2GoalsConceded;
    private int forYellowCard;
    private int forRedCard;
    private int forOwnGoal;

    public ScoringRuleDto(ScoringRule scoringRule) {
        this.for60Mins = scoringRule.getFor60Mins();
        this.forOver60Mins = scoringRule.getForOver60Mins();
        this.forGkDfGoal = scoringRule.getForGkDfGoal();
        this.forMdGoal = scoringRule.getForMdGoal();
        this.forFwGoal = scoringRule.getForFwGoal();
        this.forAssist = scoringRule.getForAssist();
        this.forGkDfCleanSheet = scoringRule.getForGkDfCleanSheet();
        this.forMdCleanSheet = scoringRule.getForMdCleanSheet();
        this.for3GkSaves = scoringRule.getFor3GkSaves();
        this.forPkSaved = scoringRule.getForPkSaved();
        this.forPkMissed = scoringRule.getForPkMissed();
        this.for2GoalsConceded = scoringRule.getFor2GoalsConceded();
        this.forYellowCard = scoringRule.getForYellowCard();
        this.forRedCard = scoringRule.getForRedCard();
        this.forOwnGoal = scoringRule.getForOwnGoal();
    }
}

