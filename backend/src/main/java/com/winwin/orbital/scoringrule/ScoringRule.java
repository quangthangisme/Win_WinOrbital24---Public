package com.winwin.orbital.scoringrule;

import com.winwin.orbital.league.League;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ScoringRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

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

    public ScoringRule(ScoringRuleDto scoringRuleDto, League league) {
        this.league = league;
        this.for60Mins = scoringRuleDto.getFor60Mins();
        this.forOver60Mins = scoringRuleDto.getForOver60Mins();
        this.forGkDfGoal = scoringRuleDto.getForGkDfGoal();
        this.forMdGoal = scoringRuleDto.getForMdGoal();
        this.forFwGoal = scoringRuleDto.getForFwGoal();
        this.forAssist = scoringRuleDto.getForAssist();
        this.forGkDfCleanSheet = scoringRuleDto.getForGkDfCleanSheet();
        this.forMdCleanSheet = scoringRuleDto.getForMdCleanSheet();
        this.for3GkSaves = scoringRuleDto.getFor3GkSaves();
        this.forPkSaved = scoringRuleDto.getForPkSaved();
        this.forPkMissed = scoringRuleDto.getForPkMissed();
        this.for2GoalsConceded = scoringRuleDto.getFor2GoalsConceded();
        this.forYellowCard = scoringRuleDto.getForYellowCard();
        this.forRedCard = scoringRuleDto.getForRedCard();
        this.forOwnGoal = scoringRuleDto.getForOwnGoal();
    }

}
