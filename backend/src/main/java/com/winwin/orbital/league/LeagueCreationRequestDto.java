package com.winwin.orbital.league;

import com.winwin.orbital.scoringrule.ScoringRuleDto;
import lombok.*;

import java.util.Map;

@ToString
@Getter
@Setter
@EqualsAndHashCode
public class LeagueCreationRequestDto {
    private String name;
    private ScoringRuleDto scoringRule;
    private Map<String, Integer> powerUps;
    private long maxPlayersFromSameClub;
    private String teamName;
}
