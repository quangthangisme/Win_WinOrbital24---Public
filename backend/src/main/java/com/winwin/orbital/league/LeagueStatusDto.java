package com.winwin.orbital.league;

import lombok.*;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class LeagueStatusDto {
    private long leagueId;
    private String leagueStatus;
    private int currentGameweek;
    private String currentSeason;
}
